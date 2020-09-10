package skillGeneratorInterface;

import java.util.Enumeration;

import statemachine.Isa88StateMachine;

public interface SkillGeneratorInterface {

	public void generateSkill(Object skill, Isa88StateMachine stateMachine); 
	public String generateDescription(Object skill, Isa88StateMachine stateMachine, Enumeration<String> userFiles); 
	public void deleteSkill(Object skill); 
}
