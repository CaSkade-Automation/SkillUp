package skillGeneratorInterface;

import statemachine.StateMachine;

public interface SkillGeneratorInterface {

	public void generateSkill(Object skill, StateMachine stateMachine); 
	public void deleteSkill(Object skill); 
}
