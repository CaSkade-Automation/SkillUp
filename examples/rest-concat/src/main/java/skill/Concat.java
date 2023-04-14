package skill;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import skillup.annotations.Completing;
import skillup.annotations.Execute;
import skillup.annotations.RestSkillType;
import skillup.annotations.Skill;
import skillup.annotations.SkillOutput;
import skillup.annotations.SkillParameter;
import skillup.annotations.Starting;

/**
 * A skill that concatenates two strings into one. Accessible through a RESTful skill interface
 */
@Skill(skillIri = "https://www.hsu-hh.de/aut/skills/examples#RestConcat", capabilityIri = "https://www.hsu-hh.de/aut/capabilities/examples#StringConcatination", moduleIri = "https://hsu-hh.de/modules#ExampleModule", type = RestSkillType.class)
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
