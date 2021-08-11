package opcuaSkillGenerator;

import java.util.List;

import org.eclipse.milo.opcua.sdk.core.nodes.Node;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNode;

import skillup.annotations.Skill;
import opcUaServer.FolderDescription;
import opcUaServer.Server;
import statemachine.Isa88StateMachine;

public class OpcUaMethodGenerator {

	/**
	 * Method to generate OpcUa skill by creating Ua folders, variables and methods.
	 * 
	 * @param skill        skills object which should be generated
	 * @param stateMachine skills stateMachine
	 * @param opcUaServer  server to which skill should be added
	 */
	public void generateSkill(Object skill, Isa88StateMachine stateMachine, Server opcUaServer) {
		// TODO Auto-generated method stub

		String skillName = skill.getClass().getAnnotation(Skill.class).skillIri();
		String skillDescription = skill.getClass().getAnnotation(Skill.class).description();
		FolderDescription folderDescription = new FolderDescription(skillName, skillName, skillDescription,
				opcUaServer.getNamespace().getFolder());

		UaFolderNode folder = opcUaServer.getNamespace().addFolder(folderDescription);

		opcUaServer.getNamespace().addVariableNodes(skill, folder);

		opcUaServer.getNamespace().addAllSkillMethods(folder, stateMachine, skill);

		stateMachine.addStateChangeObserver(opcUaServer.getNamespace().getGenericMethod());
	}

	/**
	 * Method to delete OpcUa skill by deleting skills node and every node below
	 * 
	 * @param skill       skills object which should be deleted
	 * @param opcUaServer server from which skill should be deleted
	 */
	public void deleteSkill(Object skill, Server opcUaServer) {
		// TODO Auto-generated method stub

		String skillName = skill.getClass().getAnnotation(Skill.class).skillIri();
		List<Node> organizedNodes = opcUaServer.getNamespace().getFolder().getOrganizesNodes();
		UaNode skillNode = (UaNode) organizedNodes.stream()
				.filter(organizedNode -> organizedNode.getBrowseName().getName().equals(skillName)).findFirst().get();
		skillNode.delete();
	}
}