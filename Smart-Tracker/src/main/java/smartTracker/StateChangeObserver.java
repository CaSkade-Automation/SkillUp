package smartTracker;

import registration.Registration;
import statemachine.IStateChangeObserver;
import states.IState;

public class StateChangeObserver implements IStateChangeObserver {

	private Registration registration;
	private Object skill;

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
		for (StateIris state : StateIris.values()) {
			if (state.name().equals(newState.getClass().getSimpleName())) {
				stateIri = state.getStateIri();
			}
		}
		registration.skillStateChanged(skill, stateIri);
	}
}
