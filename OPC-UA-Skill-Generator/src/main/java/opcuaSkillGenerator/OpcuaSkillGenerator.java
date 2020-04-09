package opcuaSkillGenerator;

import java.util.List;

import org.eclipse.milo.opcua.sdk.server.api.nodes.Node;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNode;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import server.Server;
import skillGeneratorInterface.SkillGeneratorInterface;
import statemachine.StateMachine;

@Component(immediate = true, property = "name=OpcUa")
public class OpcuaSkillGenerator implements SkillGeneratorInterface {
	
	@Reference
	Server server; 

	@Override
	public void generateSkill(Object skill, StateMachine stateMachine) {
		// TODO Auto-generated method stub
		
		String skillName = skill.getClass().getSimpleName();
		
		UaFolderNode folder = server.getNamespace().addFolder(skillName);

		server.getNamespace().addVariableNodes(skill, folder);
		
		server.getNamespace().addAllSkillMethods(folder, stateMachine);
	}

	@Override
	public void deleteSkill(Object skill) {
		// TODO Auto-generated method stub
		
		String skillName = skill.getClass().getSimpleName();
		List<Node> organizedNodes = server.getNamespace().getFolder().getOrganizesNodes();
		UaNode skillNode = null;
		for (Node organizedNode : organizedNodes) {
			if (organizedNode.getBrowseName().getName().equals(skillName)) {
				skillNode = (UaNode) organizedNode;
				skillNode.delete();
			}
		}
	}
}