package actionGenerator;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.osgi.service.component.annotations.Component;

import enums.States;
import skillup.annotations.StateMachine;
import statemachine.Isa88StateMachine;
import statemachine.StateMachineBuilder;

/**
 * Class to generate stateMachine for a skill
 * 
 * @Component Indicates that annotated class is intended to be an OSGi
 *            component. <br>
 *            immediate=true, component configuration activates immediately.
 *            <br>
 *            After becoming satisfied component is registered as a service.
 */
@Component(immediate = true, service = ActionGenerator.class)
public class ActionGenerator {

	/**
	 * Method to generate stateMachine for a skill
	 * 
	 * @param skill Skill object for which stateMachine is generated
	 * @return StateMachine of the skill
	 */
	public Isa88StateMachine generateAction(Object skill) {

		StateMachineBuilder stateMachineBuilder = new StateMachineBuilder();

		Method[] methods = skill.getClass().getMethods();

		// iterate over every method that the skill has
		for (int i = 0; i < methods.length; i++) {

			// iterate over every possible state of an stateMachine
			for (States state : States.values()) {

				// if method has annotation of the state like @Execute then a new SkillAction is
				// build and added to the stateMachine for the corresponding state
				if (methods[i].isAnnotationPresent(state.getKey())) {

					SkillAction action = new SkillAction(methods[i], skill);
					stateMachineBuilder.withAction(action, state.getStateName());
				}
			}
		}
		Isa88StateMachine stateMachine = stateMachineBuilder.build();

		Field[] fields = skill.getClass().getDeclaredFields();

		// iterate over every field that the skill has
		for (Field field : fields) {
			field.setAccessible(true);

			// if field has annotation @StateMachine and is of type StateMachine the field
			// is set to this stateMachine so that the stateMachine of the skill is known by
			// the skill and transitions can be used (e.g. machine got too hot and has to be
			// stopped)
			if (field.isAnnotationPresent(StateMachine.class) && field.getType() == Isa88StateMachine.class) {
				try {
					field.set(skill, stateMachine);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}
		}
		return stateMachine;
	}
}
