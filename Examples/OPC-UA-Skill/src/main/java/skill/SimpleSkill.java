package skill;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import annotations.Completing;
import annotations.Execute;
import annotations.SkillParameter;
import annotations.SkillOutput;
import annotations.Skill;
import annotations.Starting;

@Skill(skillIri = "https://hsu-hh.de/skills#OpcUaSkill", capabilityIri = "https://hsu-hh.de/capabilites#bestCapability", moduleIri = "https://hsu-hh.de/modules#ModuleA")
public class SimpleSkill {

	private final Logger logger = LoggerFactory.getLogger(SimpleSkill.class);

	@SkillParameter(isRequired = true, option = {"4", "3", "2"})
	private int a;
	
	@SkillParameter(isRequired = true)
	private int b; 

	@SkillOutput(isRequired = false)
	private int result;

	@Starting
	public void starting() {
		logger.info("Starting with a = " + a + " and b = " +b);
	}

	@Execute
	public void execute() {
		result = a + b;
	}

	@Completing
	public void completing() {
		logger.info("Completing, result = " + result);
	}
}
