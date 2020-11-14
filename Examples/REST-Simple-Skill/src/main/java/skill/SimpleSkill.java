package skill;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import annotations.Completing;
import annotations.Execute;
import annotations.SkillParameter;
import annotations.SkillOutput;
import annotations.Skill;
import annotations.Starting;

@Skill(type = "RestSkill", skillIri = "https://www.hsu-hh.de/aut/skills#RestSkillExample", capabilityIri = "https://www.hsu-hh.de/aut/skills#RandomGeneration", moduleIri = "https://hsu-hh.de/modules#ModuleA")
public class SimpleSkill {

	private final Logger logger = LoggerFactory.getLogger(SimpleSkill.class);

	@SkillParameter(isRequired = true, option = {"4", "3", "2"}, name = "jay", description = "hello world")
	private int j = 2;
	
	@SkillParameter(isRequired = false)
	private String i;

	@SkillOutput(isRequired = false, name = "ypsilon", description = "lorem ipsum")
	private int y;
	
	@SkillOutput(isRequired = true)
	private double z;

	@Starting
	public void starting() {
		logger.info("Starting, j = " + j);
		z = 123.45;
	}

	@Execute
	public void execute() {
		j = j * 2;
		y = j;
	}

	@Completing
	public void completing() {
		j = j * 2;
		y = j;
		logger.info("Completing, y = " + y);
	}
}
