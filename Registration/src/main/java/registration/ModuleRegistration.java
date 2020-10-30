package registration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import annotations.Module;

/**
 * Class to register/delete modules on/from OPS 
 */
public class ModuleRegistration extends RegistrationMethods {

	private Logger logger = LoggerFactory.getLogger(ModuleRegistration.class);

	@Override
	public void register(String requestBody, Object object, ModuleRegistry moduleRegistry) {
		// TODO Auto-generated method stub

		// module is registered to every available OPS
		for (OpsDescription ops : moduleRegistry.getOpsDescriptionList()) {
			logger.info("Registering Module " + object.getClass().getAnnotation(Module.class).moduleIri()
					+ " with description in rdf syntax to " + ops.getId());

			String basePath = ops.getBasePath();
			String moduleEndpoint = ops.getModuleEndpoint();

			String location = basePath + moduleEndpoint;

			int responseStatusCode = opsRequest(ops, "POST", location, requestBody, "text/plain");

			// if module successfully registered to OPS, it is added to module list of this
			// OPS
			if (responseStatusCode == 201) {
				logger.info("Module successfully registered...");
				ops.addModule(object);
			} else {
				logger.error("Module couldn't be registered...");
			}
		}
	}

	@Override
	public void delete(Object object, ModuleRegistry moduleRegistry) {
		// TODO Auto-generated method stub
		logger.info("Unregistering Module " + object.getClass().getAnnotation(Module.class).moduleIri());

		// module is deleted from every OPS, on which module is registered
		for (OpsDescription myOps : moduleRegistry.getOpsDescriptionList()) {
			Object deletedModule = null; 
			for (Object module : myOps.getModules()) {
				if (object.equals(module)) {
					logger.info("Delete Module from " + myOps.getId());
					String basePath = myOps.getBasePath();
					String moduleEndpoint = myOps.getModuleEndpoint();
					String moduleIriEncoded = encodeValue(object.getClass().getAnnotation(Module.class).moduleIri());
					String location = basePath + moduleEndpoint + "/" + moduleIriEncoded;

					int responseStatusCode = opsRequest(myOps, "DELETE", location, "", "text/plain");

					if (responseStatusCode == 200) {
						deletedModule = module; 
					} else {
						logger.info("Module couldn't be deleted from OPS...");
					}
					break;
				}
			}
			myOps.deleteModule(deletedModule);
		}
	}
}
