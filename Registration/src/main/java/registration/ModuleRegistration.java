package registration;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import annotations.Module;

public class ModuleRegistration extends RegistrationMethods {

	private Logger logger = LoggerFactory.getLogger(ModuleRegistration.class);

	@Override
	public void register(String requestBody, Object object, ModuleRegistry moduleRegistry) {
		// TODO Auto-generated method stub

		for (OpsDescription opsDescription : moduleRegistry.getOpsDescriptionList()) {
			logger.info("Registering Module " + object.getClass().getAnnotation(Module.class).moduleIri()
					+ " with description in rdf syntax to " + opsDescription.getId());

			String basePath = opsDescription.getBasePath(); 
			String moduleEndpoint = opsDescription.getModuleEndpoint();

			String location = basePath + moduleEndpoint; 
			
			int responseStatusCode = opsRequest(opsDescription, "POST", location, requestBody, "text/plain");

			if (responseStatusCode == 201) {
				logger.info("Module successfully registered...");
				moduleRegistry.addModule(opsDescription, object);
			} else {
				logger.error("Module couldn't be registered...");
			}
		}
	}

	@Override
	public void delete(Object object, ModuleRegistry moduleRegistry) {
		// TODO Auto-generated method stub
		logger.info("Unregistering Module " + object.getClass().getAnnotation(Module.class).moduleIri());

		List<OpsDescription> delete = new ArrayList<OpsDescription>();

		for (OpsDescription myOps : moduleRegistry.getOpsDescriptionList()) {

			logger.info("Delete Module from " + myOps.getId());
			String basePath = myOps.getBasePath(); 
			String moduleEndpoint = myOps.getModuleEndpoint();
			String moduleIriEncoded = encodeValue(object.getClass().getAnnotation(Module.class).moduleIri());
			String location = basePath + moduleEndpoint + "/" + moduleIriEncoded;

			int responseStatusCode = opsRequest(myOps, "DELETE", location, "", "text/plain");

			if (responseStatusCode == 200) {
				delete.add(myOps);
				logger.info("Remove OPS " + myOps.getId() + " from OPS-Skill-List...");
				moduleRegistry.getOpsAndSkillList().remove(myOps.getId());
				moduleRegistry.deleteModule(myOps, object);
			}
			else {
				logger.info("Module couldn't be deleted from OPS...");
			}
		}
		logger.info("Remove every OPS which received DELETE from Module from OPS-List...");
		moduleRegistry.getOpsDescriptionList().removeAll(delete);
		moduleRegistry.opsListMessage();
	}
}
