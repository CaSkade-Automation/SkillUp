package opcUaSkillDescriptionGenerator;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.eclipse.milo.opcua.sdk.server.api.nodes.Node;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.osgi.service.component.annotations.Component;

import annotations.Module;
import annotations.Skill;
import server.Server;
import skillDescriptionGenerator.SkillDescriptionGenerator;
import statemachine.StateMachine;

@Component(immediate = true, service = OpcUaSkillDescriptionGenerator.class)
public class OpcUaSkillDescriptionGenerator extends SkillDescriptionGenerator {

	private String opcUaSkillSnippet = "registration:${MACAddress}_${ModuleName} Cap:providesOpcUaSkill module:_${SkillName}OpcUaSkill .\r\n"
			+ "module:_${SkillName}OpcUaSkill a Cap:OpcUaSkill,\r\n"
			+ "							owl:NamedIndividual.\r\n"
			+ "${CapabilityIri} Cap:isExecutableViaOpcUaSkill module:_${SkillName}OpcUaSkill .\r\n"
			+ "module:_${SkillName}OpcUaSkill OpcUa:browseName \"${BrowseName}\";  \r\n"
			+ "						OpcUa:browseNamespace \"${BrowseNamespace}\";\r\n"
			+ "						OpcUa:nodeId \"${NodeId}\";\r\n"
			+ "						OpcUa:nodeNamespace \"${NodeNamespace}\";\r\n"
			+ "						OpcUa:displayName \"${DisplayName}\" ;\r\n"
			+ "						Cap:hasStateMachine module:_${SkillName}OpcUaSkill_StateMachine;\r\n"
			+ "						Cap:hasCurrentState module:_${SkillName}OpcUaSkill_StateMachine_${StateName}.\r\n"
			+ "module:_${ServerName}_NodeSet OpcUa:containsNode module:_${SkillName}OpcUaSkill .";

	private String opcUaMethodSnippet = "module:_${SkillName}OpcUaSkill_${MethodName} a OpcUa:UAMethod,\r\n"
			+ "										owl:NamedIndiviual;\r\n"
			+ "									OpcUa:browseName \"${BrowseName}\";  \r\n"
			+ "									OpcUa:browseNamespace \"${BrowseNamespace}\";\r\n"
			+ "									OpcUa:nodeId \"${NodeId}\";\r\n"
			+ "									OpcUa:nodeNamespace \"${NodeNamespace}\";\r\n"
			+ "									OpcUa:displayName \"${DisplayName}\".   \r\n"
			+ "module:_${SkillName}OpcUaSkill OpcUa:hasComponent module:_${SkillName}OpcUaSkill_${MethodName}. \r\n";

	private String opcUaMethodInvokesTransitionSnippet = "module:_${SkillName}OpcUaSkill_${MethodName} Cap:invokes module:_${SkillName}OpcUaSkill_StateMachine_${CommandName}_Command .   \r\n";

	private String opcUaVariableSnippet = "module:_${SkillName}OpcUaSkill_${VariableName} a OpcUa:UAVariable,\r\n"
			+ "										owl:NamedIndividual;\r\n"
			+ "									OpcUa:browseName \"${BrowseName}\";  \r\n"
			+ "									OpcUa:browseNamespace \"${BrowseNamespace}\";\r\n"
			+ "									OpcUa:nodeId \"${NodeId}\";\r\n"
			+ "									OpcUa:nodeNamespace \"${NodeNamespace}\";\r\n"
			+ "									OpcUa:displayName \"${DisplayName}\";\r\n"
			+ "									OpcUa:accessLevel \"${AccessLevel}\";\r\n"
			+ "									OpcUa:hasDataType \"${DataType}\";\r\n"
			+ "									OpcUa:historizing \"${Historizing}\";\r\n"
			+ "									OpcUa:userAccessLevel \"${UserAccessLevel}\";\r\n"
			+ "									OpcUa:valueRank \"${ValueRank}\".\r\n"
			+ "module:_${SkillName}OpcUaSkill OpcUa:organizes module:_${SkillName}OpcUaSkill_${VariableName}. \r\n";

	public String generateOpcUaDescription(Server server, Object skill, StateMachine stateMachine,
			Enumeration<String> userFiles, Object module) {

		String modulePrefix = getModulePrefixSnippet();

		String opcUaSkillDescription = generateOpcUaSkillDescription(skill.getClass().getSimpleName(), stateMachine,
				server);
		String stateMachineDescription = generateStateMachineDescription(stateMachine);

		String userSnippet = "";
		if (userFiles != null) {
			for (Iterator<String> it = userFiles.asIterator(); it.hasNext();) {
				try {
					userSnippet = userSnippet + getFileFromResources(skill.getClass().getClassLoader(), it.next());
					userSnippet = userSnippet.replace("${Prefix}", "module");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		String completeSkillDescription;
		try {
			completeSkillDescription = getFileFromResources(null, "prefix.ttl") + modulePrefix + opcUaSkillDescription
					+ stateMachineDescription + userSnippet;

			completeSkillDescription = completeSkillDescription.replace("${MACAddress}", getThisMacAddress())
					.replace("${ModuleName}", module.getClass().getAnnotation(Module.class).name())
					.replace("${ServerName}", server.getServer().getConfig().getApplicationName().getText())
					.replace("${CapabilityIri}", skill.getClass().getAnnotation(Skill.class).capabilityIri())
					.replace("${SkillName}", skill.getClass().getSimpleName());

			createFile(completeSkillDescription, "opcUaDescription.ttl");

			return completeSkillDescription;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return null;
	}

	public String generateOpcUaSkillDescription(String skillName, StateMachine stateMachine, Server server) {

		String opcUaSkillDescription = null;
		List<Node> opcUaNodes = server.getNamespace().getFolder().getOrganizesNodes();

		for (Node node : opcUaNodes) {
			if (node.getBrowseName().getName().equals(skillName)) {
				String stateName = stateMachine.getState().toString()
						.substring(stateMachine.getState().toString().lastIndexOf(".") + 1);
				stateName = stateName.substring(0, stateName.lastIndexOf("State"));
				opcUaSkillDescription = opcUaSkillSnippet.replace("${StateName}", stateName);

				opcUaSkillDescription = generateOpcUaSkillDataPropertyDescription(opcUaSkillDescription, node);

				UaFolderNode folder = (UaFolderNode) node;
				List<UaNode> componentNodes = folder.getComponentNodes();

				for (UaNode componentNode : componentNodes) {
					String methodDescription = generateOpcUaSkillDataPropertyDescription(opcUaMethodSnippet,
							componentNode);

					if (!componentNode.getBrowseName().getName().equals("getResult")) {
						String opcUaMethodInvokesTransitionDescription = opcUaMethodInvokesTransitionSnippet.replace(
								"${CommandName}", componentNode.getBrowseName().getName().substring(0, 1).toUpperCase()
										+ componentNode.getBrowseName().getName().substring(1));
						methodDescription = methodDescription + opcUaMethodInvokesTransitionDescription;
					}

					methodDescription = methodDescription.replace("${MethodName}",
							componentNode.getBrowseName().getName());
					opcUaSkillDescription = opcUaSkillDescription + methodDescription;
				}

				List<Node> organizedNodes = folder.getOrganizesNodes();

				for (Node organizedNode : organizedNodes) {
					UaVariableNode variableNode = (UaVariableNode) organizedNode;

					String variableDescription = opcUaVariableSnippet
							.replace("${VariableName}", variableNode.getBrowseName().getName())
							.replace("${AccessLevel}", variableNode.getAccessLevel().toString())
							.replace("${DataType}", variableNode.getDataType().toParseableString())
							.replace("${Historizing}", variableNode.getHistorizing().toString())
							.replace("${UserAccessLevel}", variableNode.getUserAccessLevel().toString())
							.replace("${ValueRank}", variableNode.getValueRank().toString());

					variableDescription = generateOpcUaSkillDataPropertyDescription(variableDescription, organizedNode);
					opcUaSkillDescription = opcUaSkillDescription + variableDescription;
				}
			}
		}
		return opcUaSkillDescription;
	}

	public String generateOpcUaSkillDataPropertyDescription(String description, Node node) {
		String newDescription = description.replace("${BrowseName}", node.getBrowseName().getName())
				.replace("${BrowseNamespace}", node.getBrowseName().getNamespaceIndex().toString())
				.replace("${NodeId}", node.getNodeId().toParseableString())
				.replace("${NodeNamespace}", node.getNodeId().getNamespaceIndex().toString())
				.replace("${DisplayName}", node.getDisplayName().getText());

		return newDescription;
	}
}
