package skillGeneratorInterface;

import java.util.Enumeration;

import statemachine.Isa88StateMachine;

/**
 * Interface, which is implemented by every Skill Generator. Whether for
 * generation of OpcUa Skills or REST Skills or any other skill type.
 *
 */
public interface SkillGeneratorInterface {

	/**
	 * Method to generate the skill for the corresponding technology (e.g. REST).
	 * 
	 * @param skill        Skill object to get every skill parameter or
	 *                     outputs and methods.
	 * @param stateMachine StateMachine of the skill to invoke transitions of
	 *                     stateMachine when transitions of skill are executed.
	 */
	public void generateSkill(Object skill, Isa88StateMachine stateMachine);

	/**
	 * Method to generate the description of the skill in turtle syntax to add the
	 * skill to ontology managed by OPS.
	 * 
	 * @param skill        Skill object to get required information like
	 *                     skill-, capability- and module-IRI
	 * @param stateMachine StateMachine of the skill is necessary to add the current
	 *                     state to the description
	 * @param userFiles    User dependent extra information for the skill in turtle
	 *                     syntax. Only some terms like skill IRI can be
	 *                     substituted.
	 * @return Skill description in turtle syntax returned as String.
	 */
	public String generateDescription(Object skill, Isa88StateMachine stateMachine, Enumeration<String> userFiles);

	/**
	 * Method to delete the skill for the corresponding technology (e.g. OpcUa) 
	 * 
	 * @param skill Skill object to know, which skill has to be deleted. 
	 */
	public void deleteSkill(Object skill);
}
