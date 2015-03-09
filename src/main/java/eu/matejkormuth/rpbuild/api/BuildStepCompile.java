package eu.matejkormuth.rpbuild.api;

import eu.matejkormuth.rpbuild.Compiler;
import eu.matejkormuth.rpbuild.LegacyBuildStep;

/**
 * {@link LegacyBuildStep} that represents <b>compile</b> build phase in which
 * the specified file types are compiled using {@link Compiler}.
 */
public interface BuildStepCompile extends BuildStep {
	public Compiler getCompiler() throws Exception;

	public String[] getFileTypes();
}
