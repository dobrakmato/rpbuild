package eu.matejkormuth.rpbuild.plugins;

import com.typesafe.config.Config;
import eu.matejkormuth.rpbuild.api.OpenedFile;
import eu.matejkormuth.rpbuild.api.Plugin;
import eu.matejkormuth.rpbuild.api.PluginType;
import eu.matejkormuth.rpbuild.exceptions.InvalidConfigurationException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Provides support for optipng application.
 */
@Slf4j
public class OptipngPlugin extends Plugin {

    private static final String NAME = "rpbuild-optipng-plugin";
    private static final String VERSION = "1.0";
    private static final String AUTHOR = "Matej Kormuth";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public String getAuthor() {
        return AUTHOR;
    }

    @Override
    public PluginType getType() {
        return PluginType.TRANSFORM_FILES;
    }

    @Override
    public void transform(Config config, OpenedFile file) {

        int optimizationLevel = 2;

        if (config.hasPath("level")) {
            optimizationLevel = config.getInt("level");
            if (optimizationLevel > 7 || optimizationLevel < 0) {
                throw new InvalidConfigurationException("Level for optipng must be between 0-7 (including).");
            }
        }

        // Start optipng process.
        try {
            new ProcessBuilder("optipng", "-o" + optimizationLevel, file.getAbsolutePath().toString()).start().waitFor();
        } catch (InterruptedException | IOException e) {
            log.error("Error while executing optipng!", e);
        }
    }
}
