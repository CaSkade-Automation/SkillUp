package skill;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import annotations.Capability;
import annotations.Completing;
import annotations.DIN8580;
import annotations.Execute;
import annotations.SkillInput;
import annotations.SkillOutput;
import annotations.Skill;
import annotations.Starting;

@Capability(value = DIN8580.Fräsen)
@Skill
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
