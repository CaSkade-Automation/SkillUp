package annotations;

public enum States {
	Aborting(Aborting.class), Clearing(Clearing.class), Completing(Completing.class), Execute(Execute.class),
	Holding(Holding.class), Resetting(Resetting.class), Starting(Starting.class), Stopping(Stopping.class),
	Suspending(Suspending.class), Unholding(Unholding.class), Unsuspending(Unsuspending.class);

	private final Class key;

	States(Class key) {
		this.key = key;
	}

	public Class getKey() {
		return key;
	}
}
