package skill;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import annotations.Completing;
import annotations.Execute;
import annotations.SkillInput;
import annotations.Skill;
import annotations.Starting;

@Skill
public class SimpleSkill {

	private final Logger logger = LoggerFactory.getLogger(SimpleSkill.class);
	
	@SkillInput
	private int j;
	
	@Starting
	public void starting() {
		logger.info("Starting, j = " + j);
	}

	@Execute
	public void execute() {
		j = j + 9;
	}

	@Completing
	public void completing() {
		logger.info("Completing, j = " + j);
	}
}
