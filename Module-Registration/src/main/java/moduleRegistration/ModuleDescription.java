package moduleRegistration;

public class ModuleDescription {

	String name;
	String description; 
	
	public ModuleDescription() {

	}

	public ModuleDescription(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public String getModuleName() {
		return name;
	}
}
