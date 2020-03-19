package annotations;

public enum Transitions {
	Start("Start"), Hold("Hold"), Unhold("Unhold"), Reset("Reset"), Suspend("Suspend"), Unsuspend("Unsuspend"),
	Abort("Abort"), Clear("Clear"), Stop("Stop");

	private final String key;

	Transitions(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}
}
