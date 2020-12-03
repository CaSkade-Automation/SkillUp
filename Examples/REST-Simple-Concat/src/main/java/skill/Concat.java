package skill;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import annotations.Completing;
import annotations.Execute;
import annotations.SkillParameter;
import annotations.SkillOutput;
import annotations.Skill;
import annotations.Starting;

@Skill(type = "RestSkill", skillIri = "https://www.hsu-hh.de/aut/skills#RestConcat", capabilityIri = "https://www.hsu-hh.de/aut/skills#RandomGeneration", moduleIri = "https://hsu-hh.de/modules#ModuleA")
public class Concat {

	private final Logger logger = LoggerFactory.getLogger(Concat.class);

	@SkillParameter(isRequired = true, name = "firstString", description = "the first part of the resulting string.")
	private String i = "";

	@SkillParameter(isRequired = true, name = "secondString", description = "the second part of the resulting string.")
	private String j = "";

	@SkillOutput(isRequired = true, name = "x", description = "the result of the concatenation.")
	private String x = "";

	@Starting
	public void starting() {
		logger.info("Starting RestConcatenate");
		logger.info("i = " + i);
		logger.info("j = " + j);
	}

	@Execute
	public void execute() {
		x = i + j;
	}

	@Completing
	public void completing() {
		logger.info("Completing RestConcatenate");
		logger.info("Result = x = " + x);
	}
}
