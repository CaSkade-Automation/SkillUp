package skill2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import annotations.Completing;
import annotations.Execute;
import annotations.Skill;
import annotations.SkillParameter;
import annotations.SkillOutput;
import annotations.Starting;

@Skill(skillIri = "https://www.bosch.com/skills#OpcUaSkill", capabilityIri = "https://www.bosch.com/capabilites#CrazyCapability", moduleIri = "https://www.bosch.com/modules#ModuleB")
public class SimpleSkill2 {

	private final Logger logger = LoggerFactory.getLogger(SimpleSkill2.class);

	@SkillParameter(isRequired = true)
	private int i;

	@SkillOutput(isRequired = true)
	private int z;

	@Starting
	public void starting() {
		logger.info("Starting, i = " + i);
	}

	@Execute
	public void execute() {
		z = i + 5;
	}

	@Completing
	public void completing() {
		logger.info("Completing, z = " + z);
	}
}
