package skill;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import annotations.Completing;
import annotations.Execute;
import annotations.SkillInput;
import annotations.SkillOutput;
import annotations.Skill;
import annotations.Starting;
import enums.DIN8580;

@Skill(capabilityType = DIN8580.Bohren_oder_Senken_oder_Reiben, namespace = "https://siemens.de")
public class SimpleSkill {

	private final Logger logger = LoggerFactory.getLogger(SimpleSkill.class);
	
	@SkillInput
	private int j;
	
	@SkillOutput
	private int y; 
	
	@Starting
	public void starting() {
		logger.info("Starting, j = " + j);
	}

	@Execute
	public void execute() {
		y = j + 9;
	}

	@Completing
	public void completing() {
		logger.info("Completing, y = " + y);
	}
}
