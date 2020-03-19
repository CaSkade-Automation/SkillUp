package opcuaSkillGenerator;

import java.util.List;

import org.eclipse.milo.opcua.sdk.server.api.nodes.Node;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNode;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import opcuaSkillRegistrationInterface.OPCUASkillRegistrationInterface;
import server.Server; 
import statemachine.StateMachine;

@Component(immediate = true, service=OpcuaSkillGenerator.class)
public class OpcuaSkillGenerator {
	
	@Reference
	Server server; 

	public void generateOpcUaSkill(OPCUASkillRegistrationInterface skillRegistration, StateMachine stateMachine) {
		
		String skillName = skillRegistration.getClass().getName();
		skillName = skillName.substring(skillName.lastIndexOf(".") + 1);
		
		UaFolderNode folder = server.getNamespace().addFolder(skillName);
		
		server.getNamespace().addMethod(folder, stateMachine);
	}
	
	public void deleteOpcUaSkill(OPCUASkillRegistrationInterface skillRegistration) {
		
		String skillName = skillRegistration.getClass().getName();
		List<Node> organizedNodes = server.getNamespace().getFolder().getOrganizesNodes();
		UaNode skillNode = null;
		for (Node organizedNode : organizedNodes) {
			if (organizedNode.getBrowseName().getName().equals(skillName.substring(skillName.lastIndexOf(".") + 1))) {
				skillNode = (UaNode) organizedNode;
				skillNode.delete();
			}
		}
	}
}