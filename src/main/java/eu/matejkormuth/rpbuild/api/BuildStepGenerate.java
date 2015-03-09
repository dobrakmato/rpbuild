package eu.matejkormuth.rpbuild.api;

import eu.matejkormuth.rpbuild.Generator;
import eu.matejkormuth.rpbuild.LegacyBuildStep;

/**
 * {@link LegacyBuildStep} that represents <b>generate</b> build phase in which
 * the files are generated using {@link Generator}s.
 */
public interface BuildStepGenerate extends BuildStep {
	public Generator getGenerator() throws Exception;
}
