package skillRegistration;

import java.util.ArrayList;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import annotations.Skill;
import opsDescription.OpsDescription;
import registration.Registration;

@Component(immediate = true, service = SkillRegistration.class)
public class SkillRegistration extends Registration {

	private Logger logger = LoggerFactory.getLogger(SkillRegistration.class);

	@Override
	public void register(String requestBody, Object object) {
		// TODO Auto-generated method stub
		String moduleIri = getModules().get(skillNeedsModule(object.getClass().getAnnotation(Skill.class).namespace()));

		logger.info("Registering Skill " + object.getClass().getSimpleName() + " with module " + moduleIri);

		for (OpsDescription myOpsDescription : getOpsDescriptionList()) {

			String moduleEndpoint = myOpsDescription.getModuleEndpoint();
			String moduleIriEncoded = encodeValue(moduleIri);
			String capabilityEndpoint = myOpsDescription.getCapabilityEndpoint();
			String location = moduleEndpoint + "/" + moduleIriEncoded + capabilityEndpoint;

			int responseStatusCode = opsRequest(myOpsDescription, "POST", location, requestBody);

			if (responseStatusCode == 201) {
				logger.info("Skill " + object.getClass().getSimpleName() + " registered to " + "OPS "
						+ myOpsDescription.getId());
				ArrayList<String> skills = getOpsAndSkillList().get(myOpsDescription.getId());
				skills.add(object.getClass().getSimpleName());
				getOpsAndSkillList().put(myOpsDescription.getId(), skills);
				opsSkillListMessage();

			} else {
				logger.info("Skill: " + object.getClass().getSimpleName() + " couldn't be registered to " + "OPS "
						+ myOpsDescription.getId());
			}
		}
	}

//	public void stateChanged(Object skill, IState state) {
//	String location = moduleEndpoint + "/" + moduleIri + capabilityEndpoint + "/"
//			+ stateEndpoint;
//
//	for (OpsDescription myOpsDescription : opsDescriptionList) {
//		int responseStatusCode = sendSPARQLQuery(myOpsDescription, "POST", location, state);
//	}
//}

	@Override
	public void delete(Object object) {
		// TODO Auto-generated method stub
		String name = object.getClass().getSimpleName();
		logger.info("Unregistering Skill " + name + "...");

		for (OpsDescription myOpsDescription : getOpsDescriptionList()) {

			// String moduleEndpoint = myOpsDescription.getModuleEndpoint();
			String capabilityIri = getModules()
					.get(skillNeedsModule(object.getClass().getAnnotation(Skill.class).namespace())) + "_" + name + "_"
					+ "Process";
			capabilityIri = encodeValue(capabilityIri);
			String capabilityEndpoint = myOpsDescription.getCapabilityEndpoint();
			String location = capabilityEndpoint + "/" + capabilityIri;
			// String location = moduleEndpoint + "/" + capabilityIri + capabilityEndpoint +
			// "/" + serviceName;

			// When OPS is capable of deleting a skill which response status code do you
			// get????
			opsRequest(myOpsDescription, "DELETE", location, "");

			logger.info("Skill " + name + " removed from " + myOpsDescription.getId());
			ArrayList<String> skills = getOpsAndSkillList().get(myOpsDescription.getId());
			skills.remove(name);
			getOpsAndSkillList().put(myOpsDescription.getId(), skills);
			opsSkillListMessage();
		}
	}
}
