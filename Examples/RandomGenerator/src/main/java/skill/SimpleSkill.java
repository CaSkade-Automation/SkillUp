package skill;

import annotations.Execute;
import annotations.SkillOutput;
import annotations.StateMachine;
import statemachine.Isa88StateMachine;
import annotations.Skill;

@Skill(skillIri = "https://www.hsu-hh.de/aut/skills#RandomGenerator", capabilityIri = "https://www.hsu-hh.de/aut/skills#RandomGeneration", moduleIri = "https://hsu-hh.de/modules#ModuleA")
public class SimpleSkill {

	@StateMachine
	Isa88StateMachine stateMachine;

	@SkillOutput(isRequired = false)
	private double random;


	@Execute
	public void execute() {
		this.random = Math.random();
		
		if (this.random <= 0.25) {
			stateMachine.stop();
		}
		if (this.random > 0.25 && this.random <= 0.5) {
			stateMachine.abort();
		}
	}
}
