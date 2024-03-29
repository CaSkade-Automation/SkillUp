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
 * Skill that multiplies four values. Accessible via a RESTful skill interface
 */
@Skill(skillIri = "https://www.hsu-hh.de/aut/skills/examples#RestMultiplier", capabilityIri = "https://www.hsu-hh.de/aut/capabilities/examples#Multiplication", moduleIri = "https://hsu-hh.de/modules#ExampleModule", type = RestSkillType.class)
public class Multiplier {

	private final Logger logger = LoggerFactory.getLogger(Multiplier.class);

	@SkillParameter(isRequired = true, name = "factor-one", description = "the first factor of the operation")
	private double i = 0;

	@SkillParameter(isRequired = true, name = "factor-two", description = "the second factor of the operation")
	private double j = 0;

	@SkillParameter(isRequired = false, name = "factor-three", description = "the third factor of the operation")
	private double k = 0;

	@SkillParameter(isRequired = false, name = "factor-four", description = "the fourth factor of the operation")
	private double l = 0;

	@SkillOutput(isRequired = true, name = "x", description = "result of the operation")
	private double x = 0;

	@Starting
	public void starting() {
		logger.info("Starting RestMultiplier");
		logger.info("i = " + i);
		logger.info("j = " + j);
		logger.info("k = " + k);
		logger.info("l = " + l);
	}

	@Execute
	// if an optional factor is 0 we ignore it / assume it is not used.
	public void execute() {
		x = i * j;
		if (k != 0) {
			x *= k;
		}
		if (l != 0) {
			x *= l;
		}
	}

	@Completing
	public void completing() {
		logger.info("Completing RestMultiplier");
		logger.info("i = " + i);
		logger.info("j = " + j);
		logger.info("k = " + k);
		logger.info("l = " + l);
		logger.info("Result = x = " + x);
	}
}
