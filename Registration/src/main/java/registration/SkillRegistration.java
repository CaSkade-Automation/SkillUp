package registration;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import annotations.Skill;

/**
 * Class to register/delete skills on/from OPS
 */
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

		// skill is registered to every OPS on which necessary module is registered
		for (OpsDescription ops : opsList) {

			String location = ops.getBasePath() + ops.getModuleEndpoint() + "/" + encodeValue(moduleIri)
					+ ops.getSkillEndpoint();

			int responseStatusCode = opsRequest(ops, "POST", location, requestBody, "text/plain");

			if (responseStatusCode == 201) {
				logger.info("Skill " + object.getClass().getAnnotation(Skill.class).skillIri() + " registered to "
						+ "OPS " + ops.getId());
				ops.addSkill(object);
			} else {
				logger.info("Skill: " + object.getClass().getAnnotation(Skill.class).skillIri()
						+ " couldn't be registered to " + "OPS " + ops.getId());
			}
		}
	}

	/**
	 * If skills state changed every OPS is informed
	 * 
	 * @param skill          skills object whose state has changed
	 * @param stateIri       new state
	 * @param moduleRegistry to get OPS list etc.
	 */
	public void stateChanged(Object skill, String stateIri, ModuleRegistry moduleRegistry) {

		String moduleIri = skill.getClass().getAnnotation(Skill.class).moduleIri();

		// new state is sent to every OPS on which skill is registered
		for (OpsDescription ops : moduleRegistry.getOpsDescriptionList()) {
			for (Object object : ops.getSkills()) {
				if (skill.equals(object)) {
					String location = ops.getBasePath() + ops.getModuleEndpoint() + "/" + encodeValue(moduleIri)
							+ ops.getSkillEndpoint() + "/"
							+ encodeValue(skill.getClass().getAnnotation(Skill.class).skillIri());

//				String json = "{ \"newState\":" + " \"" + stateIri + "\" " + "}";
					ChangedState newState = new ChangedState();
					newState.newState = stateIri;

					String json = gson.toJson(newState);
					logger.info(json);

					opsRequest(ops, "PATCH", location, json, "application/json");
					break;
				}
			}
		}
	}

	@Override
	public void delete(Object object, ModuleRegistry moduleRegistry) {
		// TODO Auto-generated method stub
		String skillIri = object.getClass().getAnnotation(Skill.class).skillIri();
		logger.info("Unregistering Skill " + skillIri + "...");

		// skill is deleted from every OPS on which skill is registered
		for (OpsDescription ops : moduleRegistry.getOpsDescriptionList()) {
			Object deletedSkill = null;
			for (Object skill : ops.getSkills()) {
				if (object.equals(skill)) {
					String location = ops.getBasePath() + ops.getModuleEndpoint() + "/"
							+ encodeValue(object.getClass().getAnnotation(Skill.class).moduleIri())
							+ ops.getSkillEndpoint() + "/" + encodeValue(skillIri);

					int responseStatusCode = opsRequest(ops, "DELETE", location, "", "text/plain");

					if (responseStatusCode == 200) {
						logger.info("Skill " + skillIri + " removed from " + ops.getId());
						deletedSkill = skill;
					} else {
						logger.info("Skill: " + object.getClass().getAnnotation(Skill.class).skillIri()
								+ " couldn't be deleted from " + "OPS " + ops.getId());
					}
					break;
				}
			}
			// after for loop skill deleted from list, because while in a loop of list it's
			// not possible to change list
			ops.deleteSkill(deletedSkill);
		}
	}
}
