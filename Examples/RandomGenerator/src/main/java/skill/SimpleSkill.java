package skill;

import skillup.annotations.Execute;
import skillup.annotations.OpcUaSkillType;
import skillup.annotations.Skill;
import skillup.annotations.SkillOutput;
import skillup.annotations.StateMachine;
import statemachine.Isa88StateMachine;

/**
 * Skill to generate a random value (every Skill has to be provided with
 * annotation @Skill)
 */
@Skill(skillIri = "https://www.hsu-hh.de/aut/skills#RandomGenerator", capabilityIri = "https://www.hsu-hh.de/aut/skills#RandomGeneration", moduleIri = "https://hsu-hh.de/modules#ModuleA", type = OpcUaSkillType.class)
public class SimpleSkill {

	/**
	 * stateMachine to prevent forbidden situations
	 */
	@StateMachine
	Isa88StateMachine stateMachine;

	/**
	 * Output is an random double value
	 */
	@SkillOutput(isRequired = false)
	private double random;

	/**
	 * When transition start is executed and skill got in state execute a random
	 * value is generated
	 */
	@Execute
	public void execute() {
		this.random = Math.random();

		// if value to small, transition stop is called
		if (this.random <= 0.25) {
			stateMachine.stop();
		}
		// if value in this range, transition abort is called
		if (this.random > 0.25 && this.random <= 0.5) {
			stateMachine.abort();
		}
	}
}
