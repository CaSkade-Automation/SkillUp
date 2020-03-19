package server;

import java.lang.reflect.Method;
import annotations.States;
import opcuaSkillRegistration.OPCUASkillRegistration;
import statemachine.StateMachine;
import statemachine.StateMachineBuilder;

public class AnnotationEvaluation {
	
	public StateMachine evaluateAnnotation(OPCUASkillRegistration skillRegistration) {

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