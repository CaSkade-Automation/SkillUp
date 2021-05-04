package enums;

import java.lang.annotation.Annotation;

import skillup.annotations.Aborting;
import skillup.annotations.Clearing;
import skillup.annotations.Completing;
import skillup.annotations.Execute;
import skillup.annotations.Holding;
import skillup.annotations.Resetting;
import skillup.annotations.Starting;
import skillup.annotations.Stopping;
import skillup.annotations.Suspending;
import skillup.annotations.Unholding;
import skillup.annotations.Unsuspending;
import states.ActiveStateName;

/**
 * Enum of states with corresponding annotation for state and its
 * ActiveStateName. Annotation is required to know which method belongs to which
 * state.
 */
public enum States {
	Aborting(Aborting.class, ActiveStateName.Aborting), Clearing(Clearing.class, ActiveStateName.Clearing),
	Completing(Completing.class, ActiveStateName.Completing), Execute(Execute.class, ActiveStateName.Execute),
	Holding(Holding.class, ActiveStateName.Holding), Resetting(Resetting.class, ActiveStateName.Resetting),
	Starting(Starting.class, ActiveStateName.Starting), Stopping(Stopping.class, ActiveStateName.Stopping),
	Suspending(Suspending.class, ActiveStateName.Suspending), Unholding(Unholding.class, ActiveStateName.Unholding),
	Unsuspending(Unsuspending.class, ActiveStateName.Unsuspending);

	private final Class<? extends Annotation> key;
	private final ActiveStateName stateName;

	States(Class<? extends Annotation> key, ActiveStateName stateName) {
		this.key = key;
		this.stateName = stateName;
	}

	public Class<? extends Annotation> getKey() {
		return key;
	}

	public ActiveStateName getStateName() {
		return stateName;
	}
}
