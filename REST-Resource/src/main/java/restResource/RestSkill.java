package restResource;

import java.util.concurrent.atomic.AtomicInteger;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import statemachine.StateMachine;

public class RestSkill {

	private static AtomicInteger instanceCounter = new AtomicInteger(-1); // -1: Adjustment so that first generated instance = 0

	private final int instanceNo;

	private static Logger logger = LoggerFactory.getLogger(RestSkill.class);

	private StateMachine stateMachine;

	public RestSkill() {
		instanceNo = instanceCounter.incrementAndGet();
	}
	
	public RestSkill(StateMachine sm) {
		instanceNo = instanceCounter.incrementAndGet();
		this.stateMachine = sm;
	}

	@Activate
	void activate() {
		logger.info("RestSkill # " + instanceNo + " activated");
	}

	@Deactivate
	void deactivate() {
		logger.info("RestSkill # " + instanceNo + " deactivated");
	}

	public void setStateMachine(StateMachine stateMachine) {
		this.stateMachine = stateMachine;
		logger.info("RestSkill # " + instanceNo + " StateMachine set (" + stateMachine.toString() + ")");
	}

	public void start() {
		stateMachine.start();
		logger.info("RestSkill # " + instanceNo + " StateMachine started");
	}

	public void reset() {
		stateMachine.reset();
		logger.info("RestSkill # " + instanceNo + " StateMachine reset");
	}

	public String getState() {
		return stateMachine.getState().getClass().getSimpleName();
	}

	public int getInstanceNo() {
		return instanceNo;
	}

}
