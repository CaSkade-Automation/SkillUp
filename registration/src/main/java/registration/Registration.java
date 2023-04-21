package registration;

import java.io.IOException;

import org.osgi.service.component.annotations.Component;

/**
 * Registration class to register/delete modules and skills on OPS
 * 
 * Component decorator indicates that annotated class is intended to be an OSGi component. <br>
 * immediate=true, component configuration activates immediately <br>
 * after becoming satisfied component is registered as a service
 */
@Component(immediate = true, service = Registration.class)
public class Registration {

	private ModuleRegistration moduleRegistration = new ModuleRegistration();
	private SkillRegistration skillRegistration = new SkillRegistration();
	private Broadcast broadcast = new Broadcast();
	private ModuleRegistry moduleRegistry = new ModuleRegistry();

	/**
	 * Method to register module
	 * 
	 * @param requestBody information to be sent
	 * @param object      modules object which should be registered
	 */
	public void registerModule(String requestBody, Object object) {
		moduleRegistration.register(requestBody, object, moduleRegistry);
	}

	/**
	 * Method to register skill
	 * 
	 * @param requestBody information to be sent
	 * @param object      skills object which should be registered
	 */
	public void registerSkill(String requestBody, Object object) {
		skillRegistration.register(requestBody, object, moduleRegistry);
	}

	/**
	 * Method to delete module
	 * 
	 * @param object modules object which should be deleted from OPS
	 */
	public void deleteModule(Object object) {
		moduleRegistration.delete(object, moduleRegistry);
	}

	/**
	 * Method to delete skill
	 * 
	 * @param object skills object which should be deleted from OPS
	 */
	public void deleteSkill(Object object) {
		skillRegistration.delete(object, moduleRegistry);
	}

	/**
	 * Method to send broadcast
	 */
	public void broadcast() {
		try {
			broadcast.broadcast(moduleRegistry);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Checks if module to which skill is associated is already registered
	 * 
	 * @param moduleIri module which should be already registered
	 * @return boolean value, true if module already registered otherwise false
	 */
	public boolean skillNeedsModule(String moduleIri) {
		boolean moduleAvailable = moduleRegistry.skillNeedsModule(moduleIri);
		return moduleAvailable;
	}

	/**
	 * Method to inform OPS that the state of a skill has changed
	 * 
	 * @param skill    skills object whose state has changed
	 * @param stateIri new state
	 */
	public void skillStateChanged(Object skill, String stateIri) {
		skillRegistration.stateChanged(skill, stateIri, moduleRegistry);
	}
}
