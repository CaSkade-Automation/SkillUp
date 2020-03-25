package actionGenerator;

import java.lang.reflect.Method;

import org.osgi.service.component.annotations.Component;

import annotations.States;
import statemachine.StateMachine;
import statemachine.StateMachineBuilder;

@Component(immediate=true, service=ActionGenerator.class)
public class ActionGenerator {
	
	public StateMachine generateAction(Object skill) {

		StateMachineBuilder stateMachineBuilder = new StateMachineBuilder();
		
		Method[] methods = skill.getClass().getMethods();
		for (int i = 0; i < methods.length; i++) {

			for (States state : States.values()) {
				if (methods[i].isAnnotationPresent(state.getKey())) {

					SkillAction action = new SkillAction(methods[i], skill);
					stateMachineBuilder.withAction(action, state.getStateName()); 
				}
			}
		}
		StateMachine stateMachine = stateMachineBuilder.build();
		
		return stateMachine; 
	}
}
