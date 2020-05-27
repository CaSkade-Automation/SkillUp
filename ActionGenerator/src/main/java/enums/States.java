package enums;

import annotations.Aborting;
import annotations.Clearing;
import annotations.Completing;
import annotations.Execute;
import annotations.Holding;
import annotations.Resetting;
import annotations.Starting;
import annotations.Stopping;
import annotations.Suspending;
import annotations.Unholding;
import annotations.Unsuspending;
import states.ActiveStateName;

public enum States {
	Aborting(Aborting.class, ActiveStateName.Aborting), Clearing(Clearing.class, ActiveStateName.Clearing),
	Completing(Completing.class, ActiveStateName.Completing), Execute(Execute.class, ActiveStateName.Execute),
	Holding(Holding.class, ActiveStateName.Holding), Resetting(Resetting.class, ActiveStateName.Resetting),
	Starting(Starting.class, ActiveStateName.Starting), Stopping(Stopping.class, ActiveStateName.Stopping),
	Suspending(Suspending.class, ActiveStateName.Suspending), Unholding(Unholding.class, ActiveStateName.Unholding),
	Unsuspending(Unsuspending.class, ActiveStateName.Unsuspending);

	private final Class key;
	private final ActiveStateName stateName;

	States(Class key, ActiveStateName stateName) {
		this.key = key;
		this.stateName = stateName;
	}

	public Class getKey() {
		return key;
	}

	public ActiveStateName getStateName() {
		return stateName;
	}
}
