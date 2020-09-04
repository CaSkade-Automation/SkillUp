package registration;

import java.io.IOException;

import org.osgi.service.component.annotations.Component;

@Component(immediate = true, service = Registration.class)
public class Registration {

	private ModuleRegistration moduleRegistration = new ModuleRegistration();
	private SkillRegistration skillRegistration = new SkillRegistration();
	private Broadcast broadcast = new Broadcast();
	private ModuleRegistry moduleRegistry = new ModuleRegistry();

	public void registerModule(String requestBody, Object object) {
		moduleRegistration.register(requestBody, object, moduleRegistry);
	}

	public void registerSkill(String requestBody, Object object) {
		skillRegistration.register(requestBody, object, moduleRegistry);
	}

	public void deleteModule(Object object) {
		moduleRegistration.delete(object, moduleRegistry);
	}

	public void deleteSkill(Object object) {
		skillRegistration.delete(object, moduleRegistry);
	}

	public void broadcast() {
		try {
			broadcast.broadcast(moduleRegistry);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean skillNeedsModule(String moduleIri) {
		boolean moduleAvailable = moduleRegistry.skillNeedsModule(moduleIri);
		return moduleAvailable;
	}

	public void skillStateChanged(Object skill, String stateIri) {
		skillRegistration.stateChanged(skill, stateIri, moduleRegistry); 
}
}
