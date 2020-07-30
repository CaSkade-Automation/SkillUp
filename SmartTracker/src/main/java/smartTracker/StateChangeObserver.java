package smartTracker;

import registration.Registration;
import statemachine.IStateChangeObserver;
import states.IState;

public class StateChangeObserver implements IStateChangeObserver {

	Registration registration;
	Object skill;

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
		registration.skillStateChanged(skill, newState);
	}
}
