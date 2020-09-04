package registration;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import annotations.Skill;

public class SkillRegistration extends RegistrationMethods {

	private Logger logger = LoggerFactory.getLogger(SkillRegistration.class);
	private Gson gson = new Gson();

	@Override
	public void register(String requestBody, Object object, ModuleRegistry moduleRegistry) {
		// TODO Auto-generated method stub
		String moduleIri = object.getClass().getAnnotation(Skill.class).moduleIri();

		logger.info("Registering Skill " + object.getClass().getAnnotation(Skill.class).skillIri() + " with module "
				+ moduleIri);

		List<OpsDescription> opsList = moduleRegistry.skillRegisterOpsList(moduleIri);

		for (OpsDescription opsDescription : opsList) {
			String basePath = opsDescription.getBasePath();
			String moduleEndpoint = opsDescription.getModuleEndpoint();
			String moduleIriEncoded = encodeValue(moduleIri);
			String skillEndpoint = opsDescription.getSkillEndpoint();
			String location = basePath + moduleEndpoint + "/" + moduleIriEncoded + skillEndpoint;

			int responseStatusCode = opsRequest(opsDescription, "POST", location, requestBody, "text/plain");

			if (responseStatusCode == 201) {
				logger.info("Skill " + object.getClass().getAnnotation(Skill.class).skillIri() + " registered to "
						+ "OPS " + opsDescription.getId());
			} else {
				logger.info("Skill: " + object.getClass().getAnnotation(Skill.class).skillIri()
						+ " couldn't be registered to " + "OPS " + opsDescription.getId());
			}
		}
	}

	public void stateChanged(Object skill, String stateIri, ModuleRegistry moduleRegistry) {

		String moduleIri = skill.getClass().getAnnotation(Skill.class).moduleIri();
		List<OpsDescription> opsList = moduleRegistry.skillRegisterOpsList(moduleIri);

		for (OpsDescription opsDescription : opsList) {
			String location = opsDescription.getBasePath() + opsDescription.getModuleEndpoint() + "/"
					+ URLEncoder.encode(moduleIri, StandardCharsets.UTF_8) + opsDescription.getSkillEndpoint() + "/"
					+ URLEncoder.encode(skill.getClass().getAnnotation(Skill.class).skillIri(), StandardCharsets.UTF_8);

//			String json = "{ \"newState\":" + " \"" + stateIri + "\" " + "}";
			ChangedState newState = new ChangedState();
			newState.newState = stateIri;

			String json = gson.toJson(newState);
			logger.info(json);

			opsRequest(opsDescription, "PATCH", location, json, "application/json");
		}
	}

	@Override
	public void delete(Object object, ModuleRegistry moduleRegistry) {
		// TODO Auto-generated method stub
		String skill = object.getClass().getAnnotation(Skill.class).skillIri();
		logger.info("Unregistering Skill " + skill + "...");

		List<OpsDescription> opsList = moduleRegistry
				.skillRegisterOpsList(object.getClass().getAnnotation(Skill.class).moduleIri());

		for (OpsDescription myOpsDescription : opsList) {

			String basePath = myOpsDescription.getBasePath();
			String moduleEndpoint = myOpsDescription.getModuleEndpoint();
			String moduleIriEncoded = encodeValue(object.getClass().getAnnotation(Skill.class).moduleIri());
			String skillEndpoint = myOpsDescription.getSkillEndpoint();
			String skillIri = encodeValue(skill);
			String location = basePath + moduleEndpoint + "/" + moduleIriEncoded + skillEndpoint + "/" + skillIri;

			int responseStatusCode = opsRequest(myOpsDescription, "DELETE", location, "", "text/plain");

			if (responseStatusCode == 200) {
				logger.info("Skill " + skill + " removed from " + myOpsDescription.getId());
			} else {
				logger.info("Skill: " + object.getClass().getAnnotation(Skill.class).skillIri()
						+ " couldn't be deleted from " + "OPS " + myOpsDescription.getId());
			}
		}
	}
}
