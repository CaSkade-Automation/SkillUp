package smartTracker;

import registration.Registration;
import statemachine.IStateChangeObserver;
import states.IState;

/**
 * Class implements interface to observe a stateMachine and thus also the skill
 * and notifies OPS if the state of a skill has changed
 */
public class StateChangeObserver implements IStateChangeObserver {

	private Registration registration;
	private Object skill;

	/**
	 * Constructor
	 * 
	 * @param registration to inform OPS about changed state
	 * @param skill        skill whose state has changed
	 */
	public StateChangeObserver(Registration registration, Object skill) {
		// TODO Auto-generated constructor stub
		this.registration = registration;
		this.skill = skill;
	}

	@Override
	public void onStateChanged(IState newState) {
		// TODO Auto-generated method stub
		System.out.println("State of " + skill.getClass().getSimpleName() + " has changed, new State is: "
				+ newState.getClass().getSimpleName());
		String stateIri = null;
		// get IRI of new state 
		for (StateIris state : StateIris.values()) {
			if (state.name().equals(newState.getClass().getSimpleName())) {
				stateIri = state.getStateIri();
				break; 
			}
		}
		registration.skillStateChanged(skill, stateIri);
	}
}
