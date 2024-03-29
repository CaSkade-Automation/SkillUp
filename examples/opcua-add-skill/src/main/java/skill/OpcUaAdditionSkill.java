package skill;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import skillup.annotations.Completing;
import skillup.annotations.Execute;
import skillup.annotations.OpcUaSkillType;
import skillup.annotations.Skill;
import skillup.annotations.SkillOutput;
import skillup.annotations.SkillParameter;
import skillup.annotations.Starting;

/**
 * Skill to add two values with an OPC UA skill interface
 */
@Skill(skillIri = "https://hsu-hh.de/skills/examples#OpcUaAdditionSkill", capabilityIri = "https://hsu-hh.de/capabilities/examples#AdditionCapability", moduleIri = "https://hsu-hh.de/modules#ExampleModule", type = OpcUaSkillType.class)
public class OpcUaAdditionSkill {

	private final Logger logger = LoggerFactory.getLogger(OpcUaAdditionSkill.class);

	/**
	 * first value, which has optional values
	 */
	@SkillParameter(isRequired = true, option = { "4", "3", "2" })
	private int a;

	/**
	 * second value
	 */
	@SkillParameter(isRequired = true)
	private int b;

	/**
	 * result of addition
	 */
	@SkillOutput(isRequired = false)
	private int result;

	/**
	 * When skill is in state starting (after transition start) the values of the
	 * parameters are shown
	 */
	@Starting
	public void starting() {
		logger.info("Starting with a = " + a + " and b = " + b);
	}

	/**
	 * When skill is in state execute after state starting, the result is calculated
	 */
	@Execute
	public void execute() {
		result = a + b;
	}

	/**
	 * When skill is in state completing after execute state, the result is printed
	 */
	@Completing
	public void completing() {
		logger.info("Completing, result = " + result);
	}
}
