package actionGenerator;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

		// get all methods that the skill has
		Method[] methodArray = skill.getClass().getMethods();
		List<Method> methods = Arrays.asList(methodArray);

		// iterate over every possible state of an stateMachine
		for (States state : States.values()) {

			// if method has annotation of the state like @Execute then a new SkillAction is
			// build and added to the stateMachine for the corresponding state
			List<Method> skillActions = methods.stream().filter(method -> method.isAnnotationPresent(state.getKey()))
					.collect(Collectors.toList());

			for (Method method : skillActions) {
				SkillAction action = new SkillAction(method, skill);
				stateMachineBuilder.withAction(action, state.getStateName());
			}
		}

		Isa88StateMachine stateMachine = stateMachineBuilder.build();

		// get all fields that the skill has
		Field[] fieldArray = skill.getClass().getDeclaredFields();
		List<Field> fields = Arrays.asList(fieldArray);

		// if field has annotation @StateMachine and is of type StateMachine the field
		// is set to this stateMachine so that the stateMachine of the skill is known by
		// the skill and transitions can be used (e.g. machine got too hot and has to be
		// stopped)
		List<Field> stateMachineFields = fields.stream().filter(
				field -> field.isAnnotationPresent(StateMachine.class) && field.getType() == Isa88StateMachine.class)
				.collect(Collectors.toList());

		for (Field field : stateMachineFields) {
			field.setAccessible(true);
			try {
				field.set(skill, stateMachine);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return stateMachine;
	}
}
