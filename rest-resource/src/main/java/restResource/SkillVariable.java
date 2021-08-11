package restResource;

public class SkillVariable {

	private String name;
	private String description;
	private boolean isRequired;
	private String type;
	private Object value;

	public SkillVariable() {
	}

	public SkillVariable(String name, String description, boolean isRequired, String type, Object value) {
		this.name = name;
		this.description = description;
		this.isRequired = isRequired;
		this.type = type;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public boolean isRequired() {
		return isRequired;
	}

	public String getType() {
		return type;
	}

	public Object getValue() {
		return value;
	}

}
