package smartTracker;

/**
 * Enum with every possible state and its IRI to tell OPS to which state the
 * skill has changed
 *
 */
public enum StateIris {
	IdleState("http://www.hsu-ifa.de/ontologies/ISA-TR88#Idle"),
	StartingState("http://www.hsu-ifa.de/ontologies/ISA-TR88#Starting"),
	ExecuteState("http://www.hsu-ifa.de/ontologies/ISA-TR88#Execute"),
	CompletingState("http://www.hsu-ifa.de/ontologies/ISA-TR88#Completing"),
	CompleteState("http://www.hsu-ifa.de/ontologies/ISA-TR88#Complete"),
	UnholdingState("http://www.hsu-ifa.de/ontologies/ISA-TR88#Unholding"),
	HeldState("http://www.hsu-ifa.de/ontologies/ISA-TR88#Held"),
	HoldingState("http://www.hsu-ifa.de/ontologies/ISA-TR88#Holding"),
	UnsuspendingState("http://www.hsu-ifa.de/ontologies/ISA-TR88#Unsuspending"),
	SuspendedState("http://www.hsu-ifa.de/ontologies/ISA-TR88#Suspended"),
	SuspendingState("http://www.hsu-ifa.de/ontologies/ISA-TR88#Suspending"),
	ResettingState("http://www.hsu-ifa.de/ontologies/ISA-TR88#Resetting"),
	AbortingState("http://www.hsu-ifa.de/ontologies/ISA-TR88#Aborting"),
	AbortedState("http://www.hsu-ifa.de/ontologies/ISA-TR88#Aborted"),
	ClearingState("http://www.hsu-ifa.de/ontologies/ISA-TR88#Clearing"),
	StoppingState("http://www.hsu-ifa.de/ontologies/ISA-TR88#Stopping"),
	StoppedState("http://www.hsu-ifa.de/ontologies/ISA-TR88#Stopped");

	private final String key;

	StateIris(String key) {
		this.key = key;
	}

	public String getStateIri() {
		return key;
	}
}
