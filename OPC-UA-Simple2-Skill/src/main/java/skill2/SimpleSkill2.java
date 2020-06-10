package skill2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import annotations.Completing;
import annotations.Execute;
import annotations.Skill;
import annotations.SkillInput;
import annotations.SkillOutput;
import annotations.Starting;

@Skill(namespace = "https://www.bosch.com", capabilityIri = "https://www.bosch.com/capabilites#CrazyCapability")
public class SimpleSkill2 {
	
	private final Logger logger = LoggerFactory.getLogger(SimpleSkill2.class);
	
	@SkillInput
	private int i;

	@SkillOutput
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
