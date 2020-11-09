package restResource;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import statemachine.Isa88StateMachine;

public class RestSkill {

	private static Logger logger = LoggerFactory.getLogger(RestSkill.class);

	private UUID uuid;
	private Isa88StateMachine stateMachine;
	private String skillIri; // we need this to identify our skill later (to delete it...).

	public RestSkill(Isa88StateMachine sm, String skillIri) {
		uuid = UUID.randomUUID();
		this.stateMachine = sm;
		this.skillIri = skillIri;
		logger.info("RestSkill \"" + uuid + "\" (skillIri=" + this.skillIri + ") created.");
	}

	public void setStateMachine(Isa88StateMachine stateMachine) {
		this.stateMachine = stateMachine;
		logger.info("RestSkill \"" + uuid + "\": StateMachine set (" + stateMachine.toString() + ").");
	}

	public void start() {
		stateMachine.start();
		logger.info("RestSkill \"" + uuid + "\": StateMachine start.");
	}

	public void reset() {
		stateMachine.reset();
		logger.info("RestSkill \"" + uuid + "\": StateMachine reset.");
	}

	public void hold() {
		stateMachine.hold();
		logger.info("RestSkill \"" + uuid + "\": StateMachine hold.");
	}

	public void unhold() {
		stateMachine.unhold();
		logger.info("RestSkill \"" + uuid + "\": StateMachine unhold.");
	}

	public void suspend() {
		stateMachine.suspend();
		logger.info("RestSkill \"" + uuid + "\": StateMachine suspend.");
	}

	public void unsuspend() {
		stateMachine.unsuspend();
		logger.info("RestSkill \"" + uuid + "\": StateMachine unsuspend.");
	}
	
	public void stop() {
		stateMachine.stop();
		logger.info("RestSkill \"" + uuid + "\": StateMachine stop.");
	}
	
	public void abort() {
		stateMachine.abort();
		logger.info("RestSkill \"" + uuid + "\": StateMachine abort.");
	}
	
	public void clear() {
		stateMachine.clear();
		logger.info("RestSkill \"" + uuid + "\": StateMachine clear.");
	}

	public String getState() {
		return stateMachine.getState().getClass().getSimpleName();
	}

	public UUID getUUID() {
		return uuid;
	}

	public String getSkillIri() {
		return skillIri;
	}

}
