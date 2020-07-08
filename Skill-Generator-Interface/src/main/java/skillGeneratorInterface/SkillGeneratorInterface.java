package skillGeneratorInterface;

import java.util.Enumeration;

import statemachine.StateMachine;

public interface SkillGeneratorInterface {

	public void generateSkill(Object skill, StateMachine stateMachine); 
	public String generateDescription(Object skill, StateMachine stateMachine, Enumeration<String> userFiles); 
	public void deleteSkill(Object skill); 
}
