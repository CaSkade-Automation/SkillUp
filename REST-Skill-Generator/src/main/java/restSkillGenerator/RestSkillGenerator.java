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
import statemachine.StateMachine;

@Component(immediate = true, property = "name=Rest")
public class RestSkillGenerator implements SkillGeneratorInterface {

	private static Logger logger = LoggerFactory.getLogger(RestSkillGenerator.class);

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
	public void generateSkill(Object skill, StateMachine stateMachine) {
		if (resource != null) {
			resource.generateSkill(skill, stateMachine);
		}

	}

	// TODO
	@Override
	public String generateDescription(Object skill, StateMachine stateMachine, Enumeration<String> userFiles) {
		// TODO Auto-generated method stub
		return "ERR: no valid description generated";
	}

	@Override
	public void deleteSkill(Object skill) {
		if (resource != null) {
			deleteSkill(skill);
		}
	}

}
