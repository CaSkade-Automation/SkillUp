package actionGenerator;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.osgi.service.component.annotations.Component;

import annotations.StateMachine;
import enums.States;
import statemachine.Isa88StateMachine;
import statemachine.StateMachineBuilder;

@Component(immediate=true, service=ActionGenerator.class)
public class ActionGenerator {
	
	public Isa88StateMachine generateAction(Object skill) {

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
		Isa88StateMachine stateMachine = stateMachineBuilder.build();
		
		Field[] fields = skill.getClass().getDeclaredFields(); 
		
		for (Field field : fields) {
			field.setAccessible(true);
			if(field.isAnnotationPresent(StateMachine.class)) {
				if(field.getType() == Isa88StateMachine.class) {
					
					try {
						field.set(skill, stateMachine);
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
//					return skillStateMachine; 
				}
			}
		}
		return stateMachine; 
	}
}
