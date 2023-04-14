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
 * Simple skill to subtract two values with an OPC UA skill interface
 */
@Skill(skillIri = "https://hsu-hh.de/skills/examples#SubtractSkill", capabilityIri = "https://hsu-hh.de/capabilities/examples#SubtractCapability", moduleIri = "https://hsu-hh.de/modules#ExampleModule", type = OpcUaSkillType.class)
public class OpcUaSubtractSkill {

	private final Logger logger = LoggerFactory.getLogger(OpcUaSubtractSkill.class);

	/**
	 * first value
	 */
	@SkillParameter(isRequired = true)
	private int i;

	/**
	 * second value
	 */
	@SkillParameter(isRequired = true)
	private int j;

	/**
	 * result
	 */
	@SkillOutput(isRequired = true)
	private int z;

	/**
	 * When skill is in state starting after transition start the two values (skill
	 * parameter) are shown
	 */
	@Starting
	public void starting() {
		logger.info("Starting with i = " + i + " and j = " + j);
	}

	/**
	 * When skill is in state execute after state starting the second value is
	 * deducted from first value
	 */
	@Execute
	public void execute() {
		z = i - j;
	}

	/**
	 * After execute the skill is in state completing and the result is printed
	 */
	@Completing
	public void completing() {
		logger.info("Completing, z = " + z);
	}
}
