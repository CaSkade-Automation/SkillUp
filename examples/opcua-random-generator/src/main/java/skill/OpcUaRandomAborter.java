package skill;

import skillup.annotations.Execute;
import skillup.annotations.OpcUaSkillType;
import skillup.annotations.Skill;
import skillup.annotations.SkillOutput;
import skillup.annotations.StateMachine;
import statemachine.Isa88StateMachine;

/**
 * A Skill with an OPC UA skill interface that generates a random value. If this value is less than .25, the skill stops itself. If the random value
 * is between .25 and .5, it aborts. Otherwise, it just completes normally
 */
@Skill(skillIri = "https://www.hsu-hh.de/aut/skills/examples#RandomAborter", capabilityIri = "https://www.hsu-hh.de/aut/capabilities/examples#RandomAborter", moduleIri = "https://hsu-hh.de/modules#ExampleModule", type = OpcUaSkillType.class)
public class OpcUaRandomAborter {

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
	 * When transition start is executed and skill got in state execute a random value is generated
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
