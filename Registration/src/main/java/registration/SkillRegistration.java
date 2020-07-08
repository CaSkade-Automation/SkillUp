package registration;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import annotations.Skill;

public class SkillRegistration extends RegistrationMethods {

	private Logger logger = LoggerFactory.getLogger(SkillRegistration.class);

	@Override
	public void register(String requestBody, Object object, ModuleRegistry moduleRegistry) {
		// TODO Auto-generated method stub
		String moduleIri = object.getClass().getAnnotation(Skill.class).moduleIri();

		logger.info("Registering Skill " + object.getClass().getAnnotation(Skill.class).skillIri() + " with module "
				+ moduleIri);

		List<OpsDescription> opsList = moduleRegistry.skillRegisterOpsList(moduleIri);

		for (OpsDescription opsDescription : opsList) {
			String moduleEndpoint = opsDescription.getModuleEndpoint();
			String moduleIriEncoded = encodeValue(moduleIri);
			String skillEndpoint = opsDescription.getSkillEndpoint();
			String location = moduleEndpoint + "/" + moduleIriEncoded + skillEndpoint;

			int responseStatusCode = opsRequest(opsDescription, "POST", location, requestBody);

			if (responseStatusCode == 201) {
				logger.info("Skill " + object.getClass().getAnnotation(Skill.class).skillIri() + " registered to "
						+ "OPS " + opsDescription.getId());
				moduleRegistry.addSkill(opsDescription, object.getClass().getAnnotation(Skill.class).skillIri());
			} else {
				logger.info("Skill: " + object.getClass().getAnnotation(Skill.class).skillIri()
						+ " couldn't be registered to " + "OPS " + opsDescription.getId());
			}
		}
	}

//	public void stateChanged(Object skill, IState state) {
//	String location = moduleEndpoint + "/" + moduleIri + skillEndpoint + "/"
//			+ skillIri;
//
//	for (OpsDescription myOpsDescription : opsDescriptionList) {
//		int responseStatusCode = sendSPARQLQuery(myOpsDescription, "POST", location, state);
//	}
//}

	@Override
	public void delete(Object object, ModuleRegistry moduleRegistry) {
		// TODO Auto-generated method stub
		String skill = object.getClass().getAnnotation(Skill.class).skillIri();
		logger.info("Unregistering Skill " + skill + "...");

		List<OpsDescription> opsList = moduleRegistry
				.skillRegisterOpsList(object.getClass().getAnnotation(Skill.class).moduleIri());

		for (OpsDescription myOpsDescription : opsList) {

			String moduleEndpoint = myOpsDescription.getModuleEndpoint();
			String moduleIriEncoded = encodeValue(object.getClass().getAnnotation(Skill.class).moduleIri());
			String skillEndpoint = myOpsDescription.getSkillEndpoint();
			String skillIri = encodeValue(skill);
			String location = moduleEndpoint + "/" + moduleIriEncoded + skillEndpoint + "/" + skillIri;

			int responseStatusCode = opsRequest(myOpsDescription, "DELETE", location, "");

			if (responseStatusCode == 200) {
				logger.info("Skill " + skill + " removed from " + myOpsDescription.getId());
				moduleRegistry.deleteSkill(myOpsDescription, skill);
			} else {
				logger.info("Skill: " + object.getClass().getAnnotation(Skill.class).skillIri()
						+ " couldn't be deleted from " + "OPS " + myOpsDescription.getId());
			}
		}
	}
}
