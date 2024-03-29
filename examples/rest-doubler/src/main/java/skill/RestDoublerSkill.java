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
 * A skill that doubles an input value during execute and then quadruples it during completing. May be used to monitor skill outputs during execution.
 * Accessible via a RESTful skill interface.
 */
@Skill(skillIri = "https://www.hsu-hh.de/aut/skills/examples#RestDoubler", capabilityIri = "https://www.hsu-hh.de/aut/capabilities/examples#Doubler", moduleIri = "https://hsu-hh.de/modules#ExampleModule", type = RestSkillType.class)
public class RestDoublerSkill {

	private final Logger logger = LoggerFactory.getLogger(RestDoublerSkill.class);

	@SkillParameter(isRequired = true, option = { "4", "3", "2" }, name = "jay", description = "hello world")
	private int j = 2;

	@SkillParameter(isRequired = false)
	private String i;

	@SkillOutput(isRequired = false, name = "ypsilon", description = "lorem ipsum")
	private int y;

	@SkillOutput(isRequired = true)
	private double z;

	@Starting
	public void starting() {
		logger.info("Starting, j = " + j);
		z = 123.45;
	}

	@Execute
	public void execute() {
		y = j * 2;
	}

	@Completing
	public void completing() {
		y = j * 4;
		logger.info("Completing, y = " + y);
	}
}
