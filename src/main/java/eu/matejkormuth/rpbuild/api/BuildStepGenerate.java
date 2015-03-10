package eu.matejkormuth.rpbuild.api;

import eu.matejkormuth.rpbuild.Generator;
import eu.matejkormuth.rpbuild.LegacyBuildStep;

/**
 * {@link LegacyBuildStep} that represents <b>generate</b> build phase in which
 * the files are generated using {@link Generator}s.
 */
public abstract class BuildStepGenerate extends BuildStep {
	public abstract Generator getGenerator() throws Exception;
}
