package skill;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import annotations.Completing;
import annotations.Execute;
import annotations.SkillParameter;
import annotations.SkillOutput;
import annotations.Skill;
import annotations.Starting;

@Skill(type = "RestSkill", skillIri = "https://www.hsu-hh.de/aut/skills#RestMultiplier", capabilityIri = "https://www.hsu-hh.de/aut/skills#RandomGeneration", moduleIri = "https://hsu-hh.de/modules#ModuleA")
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
