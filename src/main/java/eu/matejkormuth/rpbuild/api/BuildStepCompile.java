package eu.matejkormuth.rpbuild.api;

import eu.matejkormuth.rpbuild.Compiler;
import eu.matejkormuth.rpbuild.LegacyBuildStep;

/**
 * {@link LegacyBuildStep} that represents <b>compile</b> build phase in which
 * the specified file types are compiled using {@link Compiler}.
 */
public abstract class BuildStepCompile extends BuildStep {
	public abstract Compiler getCompiler() throws Exception;

	public abstract String[] getFileTypes();
}
