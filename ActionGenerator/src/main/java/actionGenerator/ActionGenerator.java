package actionGenerator;

import java.lang.reflect.Method;

import org.osgi.service.component.annotations.Component;

import annotations.States;
import opcuaSkillRegistrationInterface.OPCUASkillRegistrationInterface;
import statemachine.StateMachine;
import statemachine.StateMachineBuilder;

@Component(immediate=true, service=ActionGenerator.class)
public class ActionGenerator {
	
	public StateMachine generateAction(OPCUASkillRegistrationInterface skillRegistration) {

		StateMachineBuilder stateMachineBuilder = new StateMachineBuilder();
		
		Method[] methods = skillRegistration.getClass().getMethods();
		for (int i = 0; i < methods.length; i++) {

			for (States state : States.values()) {
				if (methods[i].isAnnotationPresent(state.getKey())) {

					SkillAction action = new SkillAction(methods[i], skillRegistration);
					if (state.toString().equals("Starting")) {
						stateMachineBuilder.withActionInStarting(action); 
					}
					else if (state.toString().equals("Execute")) {
						stateMachineBuilder.withActionInExecute(action);
					}
					else if (state.toString().equals("Completing")) {
						stateMachineBuilder.withActionInCompleting(action);
					}
				}
			}
		}
		StateMachine stateMachine = stateMachineBuilder.build();
		
		return stateMachine; 
	}

}
