package skillGeneratorInterface;

import server.Server;
import statemachine.StateMachine;

public interface SkillGeneratorInterface {

	public Server generateSkill(Object skill, StateMachine stateMachine); 
	public void deleteSkill(Object skill); 
}
