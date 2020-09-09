package skill2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import annotations.Completing;
import annotations.Execute;
import annotations.Skill;
import annotations.SkillParameter;
import annotations.SkillOutput;
import annotations.Starting;

@Skill(skillIri = "https://hsu-hh.de/skills#OpcUaSkill2", capabilityIri = "https://hsu-hh.de/capabilites#CrazyCapability", moduleIri = "https://hsu-hh.de/modules#ModuleA")
public class SimpleSkill2 {

	private final Logger logger = LoggerFactory.getLogger(SimpleSkill2.class);

	@SkillParameter(isRequired = true)
	private int i;

	@SkillParameter(isRequired = true)
	private int j; 
	
	@SkillOutput(isRequired = true)
	private int z;

	@Starting
	public void starting() {
		logger.info("Starting with i = " + i + " and j = " + j);
	}

	@Execute
	public void execute() {
		z = i - j;
	}

	@Completing
	public void completing() {
		logger.info("Completing, z = " + z);
	}
}
