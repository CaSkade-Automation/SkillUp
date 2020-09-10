package descriptionGenerator;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import descriptionGenerator.DescriptionGenerator;
import statemachine.Isa88StateMachine;

public class SkillDescriptionGenerator extends DescriptionGenerator{

	private Logger logger = LoggerFactory.getLogger(SkillDescriptionGenerator.class);

	public String generateStateMachineDescription(Isa88StateMachine stateMachine) {

		String stateMachineDescription = null;

		try {
			stateMachineDescription = getFileFromResources(SkillDescriptionGenerator.class.getClassLoader(), "stateMachine.ttl");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (stateMachineDescription != null) {
			return stateMachineDescription;
		} else {
			logger.error("Couldn't get stateMachineDescription from resources folder.");
			return null;
		}
	}
}