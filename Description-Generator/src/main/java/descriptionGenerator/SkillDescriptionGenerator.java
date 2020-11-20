package descriptionGenerator;

import descriptionGenerator.DescriptionGenerator;

/**
 * Class extends from {@link DescriptionGenerator} and has method that is equal
 * for every skill independent of its type (e.g. REST)
 *
 */
public class SkillDescriptionGenerator extends DescriptionGenerator {

	/**
	 * Method to generate the stateMachine description or rather get the turtle
	 * snippet of stateMachine from resources folder
	 * 
	 * @return turtle snippet of stateMachine as String
	 */
	public String generateStateMachineDescription() {

		String stateMachineDescription = getFileFromResources(null, "stateMachine.ttl");
		return stateMachineDescription;
	}
}