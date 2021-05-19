package restSkillGenerator;

import java.util.Enumeration;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import restResource.RestResource;
import skillGeneratorInterface.SkillGeneratorInterface;
import statemachine.Isa88StateMachine;

@Component(immediate = true, property = "type:String=RestSkillType")
public class RestSkillGenerator implements SkillGeneratorInterface {

	private static Logger logger = LoggerFactory.getLogger(RestSkillGenerator.class);

	private RestSkillDescriptionGenerator restSkillDescriptionGenerator = new RestSkillDescriptionGenerator();

	@Reference
	RestResource resource;

	@Activate
	public void activate() {
		logger.info("Activating " + getClass().getSimpleName());
	}

	@Deactivate
	public void deactivate() {
		logger.info("Deactivating " + getClass().getSimpleName());
	}

	@Override
	public void generateSkill(Object skill, Isa88StateMachine stateMachine) {
		if (resource != null) {
			logger.info(getClass().getSimpleName() + ": Trying to generate skill...");
			resource.generateSkill(skill, stateMachine);
		} else {
			logger.info(getClass().getSimpleName() + ": ERR while generating skill: RestResource is NULL.");
		}
	}

	@Override
	public String generateDescription(Object skill, Isa88StateMachine stateMachine, Enumeration<String> userFiles) {
		logger.info(getClass().getSimpleName() + ": Trying to generate REST skill description...");
		String description = restSkillDescriptionGenerator.generateRestDescription(resource, skill, stateMachine,
				userFiles);
		return description;
	}

	@Override
	public void deleteSkill(Object skill) {
		if (resource != null) {
			logger.info(getClass().getSimpleName() + ": Trying to delete skill...");
			resource.deleteSkill(skill);
		} else {
			logger.info(getClass().getSimpleName() + ": ERR while deleting skill: RestResource is NULL.");
		}
	}

}
