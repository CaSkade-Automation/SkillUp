package opcuaSkillGenerator;

import java.util.List;

import org.eclipse.milo.opcua.sdk.server.api.nodes.Node;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNode;

import annotations.Skill;
import opcUaServer.Server;
import statemachine.Isa88StateMachine;
public class OpcUaMethodGenerator {

	public void generateSkill(Object skill, Isa88StateMachine stateMachine, Server opcUaServer) {
		// TODO Auto-generated method stub
		
		String skillName = skill.getClass().getAnnotation(Skill.class).skillIri();
//				getSimpleName();
		
		UaFolderNode folder = opcUaServer.getNamespace().addFolder(skillName);

		opcUaServer.getNamespace().addVariableNodes(skill, folder);
		
		opcUaServer.getNamespace().addAllSkillMethods(folder, stateMachine, skill);
		
		stateMachine.addStateChangeObserver(opcUaServer.getNamespace().getGenericMethod());
	}

	public void deleteSkill(Object skill, Server opcUaServer) {
		// TODO Auto-generated method stub
		
		String skillName = skill.getClass().getAnnotation(Skill.class).skillIri();
		List<Node> organizedNodes = opcUaServer.getNamespace().getFolder().getOrganizesNodes();
		UaNode skillNode = null;
		for (Node organizedNode : organizedNodes) {
			if (organizedNode.getBrowseName().getName().equals(skillName)) {
				skillNode = (UaNode) organizedNode;
				skillNode.delete();
			}
		}
	}
}