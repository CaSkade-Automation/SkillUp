package restResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import skillup.annotations.Skill;
import statemachine.Isa88StateMachine;
import states.TransitionName;

public class RestSkill {

	private static Logger logger = LoggerFactory.getLogger(RestSkill.class);

	private Isa88StateMachine stateMachine;
	private Object skillObject;

	public RestSkill(Isa88StateMachine stateMachine, Object skillObject) {
		this.stateMachine = stateMachine;
		this.skillObject = skillObject;
		logger.info("RestSkill with skillIri=" + this.getSkillIri() + " created.");
	}

	public void fireTransition(TransitionName transitionName) {
		stateMachine.invokeTransition(transitionName);
		logger.info("RestSkill with skillIri=" + this.getSkillIri() + "\": StateMachine: " + transitionName.toString());
	}

	public String getState() {
		return stateMachine.getState().getClass().getSimpleName();
	}

	public Object getSkillObject() {
		return skillObject;
	}

	public String getSkillIri() {
		return skillObject.getClass().getAnnotation(Skill.class).skillIri();
	}
}
