/**
 * Minecraft resource pack compiler and assembler - rpBuild - Build system for Minecraft resource packs.
 * Copyright (c) 2015, Matej Kormuth <http://www.github.com/dobrakmato>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * "Minecraft" is a trademark of Mojang AB
 */
package eu.matejkormuth.rpbuild;

import eu.matejkormuth.rpbuild.api.*;
import eu.matejkormuth.rpbuild.exceptions.BuildError;
import eu.matejkormuth.rpbuild.exceptions.InvalidComponentError;
import eu.matejkormuth.rpbuild.exceptions.InvalidSettingsError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents main part of build system. Assembles files and manages build
 * process and logging.
 */
public class Assembler {
    private static final Logger log = LoggerFactory.getLogger(Assembler.class);

    // List of all generator that should be run.
    private List<Generator> generators;
    // List of all pairs file extension - compiler(s) that should be run.
    private List<CompilerListByFileExtension> compilerLists;
    private FileFinder fileFinder;
    private SimpleDateFormat dateTimeFormat;
    private SimpleDateFormat timeSpanFormat;
    // Build descriptor.
    private Project project;

    public Assembler(Project project) {
        // Create instances.
        this.generators = new ArrayList<Generator>();
        this.compilerLists = new ArrayList<CompilerListByFileExtension>();
        this.dateTimeFormat = new SimpleDateFormat();
        this.timeSpanFormat = new SimpleDateFormat("mm:ss.SSS");

        this.project = project;

        // Add build steps.
        for (BuildStep step : this.project.getBuild()) {
            try {
                this.addBuildStep(step);
            } catch (InvalidSettingsError | InvalidComponentError e) {
                log.error("Can't initialize build object graph!");
                log.error("There is unresolved configuration error(s)!");
                log.error("Exception: ", e);

                terminate();
            }
        }

        // Initialize file finder.
        this.fileFinder = new FileFinder();
        this.fileFinder.setIgnoreGit(this.project.isIgnoreGitFolders());
    }

    public void build() {
        printBuildStart();

        // Create temp directory to store files before putting in zip.
        Path tempDirectory = null;
        try {
            tempDirectory = createTempDirectory();
            // Request folder removal on rpbuild's exit.
            tempDirectory.toFile().deleteOnExit();
        } catch (BuildError e) {
            log.error("Can't create temporary folder!", e);
            printBuildEnd(0, "FAILURE");

            terminate();
        }

        long startTime = System.currentTimeMillis();

        // First execute git pull.
        if (this.project.isGitPull()) {
            log.info("Git pull is enabled in this build! Pulling using shell commands (git stash drop & git pull).");
            try {
                log.info("Executing: git stash save --keep-index.");
                Runtime.getRuntime().exec("git stash save --keep-index")
                        .waitFor();
                log.info("Executing: git stash drop.");
                Runtime.getRuntime().exec("git stash drop").waitFor();
                log.info("Executing: git pull.");
                Process gitProcess = Runtime.getRuntime().exec("git pull");
                int exitCode = gitProcess.waitFor();
                log.info("Git process exited with exit code {}.", exitCode);

            } catch (IOException | InterruptedException e) {
                log.error("Can't complete git pull! Giving up!", e);
                printBuildEnd(System.currentTimeMillis() - startTime, "FAILURE");

                terminate();
            }
            printSeparator();
        }

        // Copy all files to temp directory.
        try {
            final Path sourcePath = this.getProject().getSrc();
            final Path targetPath = tempDirectory;
            Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(final Path dir,
                                                         final BasicFileAttributes attrs) throws IOException {

                    // Ignore .git direcoties, we do not need to copy them.
                    if (dir.getFileName().toString().equalsIgnoreCase(".git")) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }

                    Files.createDirectories(targetPath.resolve(sourcePath
                            .relativize(dir)));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(final Path file,
                                                 final BasicFileAttributes attrs) throws IOException {
                    Files.copy(file,
                            targetPath.resolve(sourcePath.relativize(file)));
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Exception e) {
            log.error("Can't copy source files to temporary folder!", e);
            printBuildEnd(System.currentTimeMillis() - startTime, "FAILURE");

            terminate();
        }

        // Set source in this instance of project to temp directory.
        // This way all components will work as supposed because they
        // are working relative to project's src path.
        try {
            Field srcField = this.project.getClass().getDeclaredField("src");
            if (!srcField.isAccessible())
                srcField.setAccessible(true);
            srcField.set(this.project, tempDirectory);
        } catch (Exception e) {
            log.error("Internal error: Can't set project src to temp folder.",
                    e);
            printBuildEnd(System.currentTimeMillis() - startTime, "FAILURE");

            // Terminate VM.
            terminate();
        }

        try {
            // Generate new files.
            this.taskGenerate();

            this.printSeparator();
            // Find new generated files and files from git.
            this.findFiles();

            // Run tasks.
            this.taskRunTasks();

            // Compiler files.
            this.taskCompile();
            // Assembly files in temporary directory (currently not used).
            this.taskAssembly();

            this.printSeparator();
            // Find new files.
            this.findFiles();

            // Archive files to ZIP.
            this.taskArchive();
        } catch (BuildError error) {
            // If some error(s) occurred, output them now.
            log.error("Build failed: ", error);
            printBuildEnd(System.currentTimeMillis() - startTime, "FAILURE");

            terminate();
        }

        // Looks like everything went normally.
        long elapsedTime = System.currentTimeMillis() - startTime;
        printBuildEnd(elapsedTime, "SUCCESS");
    }

    private void taskRunTasks() {
        String[] scripts = {"script.sh", "task.sh", "run.sh", "build.sh", "script.bat", "task.bat", "run.bat", "build.bat"};

        for (Path directory : this.fileFinder.getDirectories()) {
            Path possibleScript = null;
            for (String possibleScriptName : scripts) {
                possibleScript = directory.resolve(possibleScriptName);
                if (Files.exists(possibleScript)) {
                    break;
                }
            }
            // We have script to run.
            if (possibleScript != null) {
                runScript(possibleScript);
            }
        }
    }

    private void runScript(Path possibleScript) {

        ProcessBuilder pb = new ProcessBuilder(possibleScript.toFile().getName());
        pb.redirectErrorStream();
        pb.directory(possibleScript.getParent().toFile());
        log.info("Running script {} in directory {}...", possibleScript.toAbsolutePath(), pb.directory());
        try {
            Process p = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                log.info(" >> " + line);
            }
            reader.close();
            log.info("Done!");
        } catch (IOException e) {
            log.error("Error while running the script!", e);
        }
    }

    // This method is overridden in rpbuild-maven
    // to not make Maven exit too, when rpbuild shuts down.
    public void terminate() {
        System.exit(1);
    }

    private Path createTempDirectory() throws BuildError {
        try {
            return Files.createTempDirectory("rpbuild_"
                    + this.project.getProjectName().toLowerCase()
                    .replace(" ", "_").substring(0, 5));
        } catch (IOException e) {
            throw new BuildError("Can't create temporary directory!", e);
        }
    }

    private void printBuildEnd(long elapsedTime, String status) {
        printSeparator();
        log.info("Build of project {} had finished!", project.getProjectName());
        printSeparator();
        log.info("Status: {}", status);
        log.info("Memory: {} MB / {} MB",
                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime()
                        .freeMemory()) / 1024 / 1024, Runtime.getRuntime()
                        .totalMemory() / 1024 / 1024);
        log.info("Total time: {}",
                this.timeSpanFormat.format(new Date(elapsedTime)));
        printSeparator();
    }

    private void printBuildStart() {
        printSeparator();
        log.info("Build of project " + project.getProjectName()
                + " was started at " + this.dateTimeFormat.format(new Date()));
        log.info("Used charset/encoding: " + this.project.getEncoding());
        printSeparator();
    }

    private void printSeparator() {
        log.info("------------------------------------------------------");
    }

    private void findFiles() throws BuildError {
        log.info("Looking for files...");
        try {
            int count = this.fileFinder.find(this.project.getSrc());
            log.info("Found {} files!", count);
        } catch (IOException e) {
            throw new BuildError(e);
        }
    }

    private void taskGenerate() throws BuildError {
        printSeparator();
        int count = 0;
        // Run all generators.
        for (Generator g : this.generators) {
            log.info("Running generator: {}", g.getClass().getSimpleName());
            // Request generator to generate file.
            OpenedFile file = g.generate();
            // Check for null.
            if (file == null) {
                log.warn("Generator {} generated null file!", g.getClass()
                        .getSimpleName());
                // Skip to next generator without saving.
                continue;
            }
            // Save generated file.
            file.save();
            // Increment generated files count.
            count++;
        }
        log.info("Totally generated {} files!", count);
    }

    private void taskCompile() throws BuildError {
        printSeparator();
        log.info("Compiling files...");
        int count = 0;
        // For each extension compiler list.
        for (CompilerListByFileExtension list : this.compilerLists) {

            // Find all matching files.
            List<Path> matchingFiles = this.fileFinder.getPaths(list
                    .getFileExtension());

            OpenedFile currentFile;
            for (Path path : matchingFiles) {
                count++;
                currentFile = new OpenedFile(path);
                for (Compiler c : list) {
                    c.compile(currentFile);
                }
                currentFile.save();
            }
        }
        log.info("Totally compiled {} files!", count);
    }

    private void taskAssembly() {
        printSeparator();
        log.info("Assembling files together...");
    }

    private void taskArchive() throws BuildError {
        printSeparator();
        log.info("Archiving assebled files to zip file...");
        log.info("File name: {}", this.project.getTarget().toString());

        int count = 0;
        ZipArchive zipper = new ZipArchive(this.project.getSrc()
                .toAbsolutePath(), this.project.getTarget().toFile(),
                this.project.getCompressionLevel());
        // Add files to zip.
        try {
            for (Path path : this.fileFinder.getPaths()) {
                if (!isFiltered(path)) {
                    zipper.addFile(path);
                    count++;
                }
            }
        } catch (IOException e) {
            throw new BuildError("Can't build zip file!", e);
        }
        zipper.close();
        log.info("Created archive with {} files!", count);
    }

    private boolean isFiltered(Path path) {
        for (String endFilter : this.project.getFilters()) {
            if (path.toString().endsWith(endFilter)) {
                return true;
            }
        }
        return false;
    }

    public void addBuildStep(BuildStep buildStep) throws InvalidSettingsError,
            InvalidComponentError {
        // If-else for different build steps.
        if (buildStep instanceof BuildStepCompile) {
            // Add compile type step.
            this.addCompileStep(((BuildStepCompile) buildStep).getCompiler(),
                    ((BuildStepCompile) buildStep).getFileTypes()[0],
                    ((BuildStepCompile) buildStep).getSettings());
        } else if (buildStep instanceof BuildStepGenerate) {
            // Add generate type step.
            this.addGenerateStep(
                    ((BuildStepGenerate) buildStep).getGenerator(),
                    ((BuildStepGenerate) buildStep).getSettings());
        } else {
            // Unsupported type.
            log.warn("Tried to register unsupported build step: {}", buildStep
                    .getClass().getSimpleName());
        }
    }

    private void addCompileStep(Compiler compiler, String fileExtension,
                                Setting[] settings) throws InvalidSettingsError {
        // Acquire list for this file extension.
        CompilerListByFileExtension compilerList = getOrCreateCompilerList(fileExtension);

        // Setup compiler.
        compiler.setAssembler(this);
        compiler.setSettings(settings);
        compiler.onInit();

        // Add compiler to list.
        compilerList.add(compiler);
    }

    private void addGenerateStep(Generator generator, Setting[] settings)
            throws InvalidSettingsError {
        // Set up generator.
        generator.setAssembler(this);
        generator.setSettings(settings);
        generator.onInit();

        // Add generator to list.
        this.generators.add(generator);
    }

    private CompilerListByFileExtension getOrCreateCompilerList(
            String fileExtension) {
        // Search for list where file extension is same.
        for (CompilerListByFileExtension b : this.compilerLists) {
            if (b.getFileExtension().equalsIgnoreCase(fileExtension)) {
                return b;
            }
        }

        // We need to create new list.
        CompilerListByFileExtension list = new CompilerListByFileExtension(
                fileExtension);
        // Add created list to compiler lists.
        this.compilerLists.add(list);
        // Return newly created list.
        return list;
    }

    public Project getProject() {
        return this.project;
    }

    public Charset getCharset() {
        return this.project.getCharset();
    }

    public Path getSourcePath() {
        return this.project.getSrc();
    }
}
