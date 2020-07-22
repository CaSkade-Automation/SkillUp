package opcuaSkillGenerator;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.nodes.Node;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;

import annotations.Skill;
import descriptionGenerator.SkillDescriptionGenerator;
import opcUaServer.Server;
import statemachine.StateMachine;

public class OpcUaSkillDescriptionGenerator extends SkillDescriptionGenerator {

	private String opcUaSkillSnippet = "<${ModuleIri}> Cap:providesOpcUaSkill <${SkillIri}> .\r\n"
			+ "<${SkillIri}> a Cap:OpcUaSkill,\r\n" + "							owl:NamedIndividual.\r\n"
			+ "<${CapabilityIri}> Cap:isExecutableViaOpcUaSkill <${SkillIri}> .\r\n"
			+ "<${SkillIri}> OpcUa:browseName \"${BrowseName}\";  \r\n"
			+ "						OpcUa:browseNamespace \"${BrowseNamespace}\";\r\n"
			+ "						OpcUa:nodeId \"${NodeId}\";\r\n"
			+ "						OpcUa:nodeNamespace \"${NodeNamespace}\";\r\n"
			+ "						OpcUa:displayName \"${DisplayName}\" ;\r\n"
			+ "						Cap:hasStateMachine <${SkillIri}_StateMachine>;\r\n"
			+ "						Cap:hasCurrentState <${SkillIri}_StateMachine_${StateName}>.\r\n"
			+ "<${ModuleIri}_${ServerName}_NodeSet> OpcUa:containsNode <${SkillIri}> .";

	private String opcUaMethodSnippet = "<${SkillIri}_${MethodName}> a OpcUa:UAMethod,\r\n"
			+ "										owl:NamedIndiviual;\r\n"
			+ "									OpcUa:browseName \"${BrowseName}\";  \r\n"
			+ "									OpcUa:browseNamespace \"${BrowseNamespace}\";\r\n"
			+ "									OpcUa:nodeId \"${NodeId}\";\r\n"
			+ "									OpcUa:nodeNamespace \"${NodeNamespace}\";\r\n"
			+ "									OpcUa:displayName \"${DisplayName}\".   \r\n"
			+ "<${SkillIri}> OpcUa:hasComponent <${SkillIri}_${MethodName}>. \r\n";

	private String opcUaMethodInvokesTransitionSnippet = "<${SkillIri}_${MethodName}> Cap:invokes <${SkillIri}_StateMachine_${CommandName}_Command> .   \r\n";

	private String opcUaVariableSnippet = "<${SkillIri}_${VariableName}> a OpcUa:UAVariable,\r\n"
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
			+ "<${SkillIri}> OpcUa:organizes <${SkillIri}_${VariableName}>. \r\n";

	private String opcUaSkillParameterSnippet = "<${SkillIri}_${VariableName}> a Cap:SkillParameter;\r\n"
			+ "								Cap:hasParameterName \"${BrowseName}\";\r\n"
			+ "								Cap:hasParameterType \"${DataType}\";\r\n"
			+ "								Cap:isRequired \"${Required}\";\r\n"
			+ "								Cap:hasDefaultValue \"${DefaultValue}\".\r\n"
			+ "<${SkillIri}> Cap:hasSkillParameter <${SkillIri}_${VariableName}>.";

	private String opcUaServerSnippet = "<${ModuleIri}_${ServerName}> a OpcUa:UAServer,\r\n"
			+ "						owl:NamedIndividual;\r\n"
			+ "					OpcUa:hasNodeSet <${ModuleIri}_${ServerName}_NodeSet>.\r\n"
			+ "<${ModuleIri}_${ServerName}_NodeSet> a OpcUa:UANodeSet, \r\n"
			+ "									owl:NamedIndividual. \r\n" + "";

	private String opcUaServerSecuritySnippet = "<${ModuleIri}_${ServerName}> OpcUa:hasEndpointUrl \"${EndpointUrl}\";\r\n"
			+ "					OpcUa:hasMessageSecurityMode OpcUa:MessageSecurityMode_${MessageSecurityMode}; \r\n"
			+ "					OpcUa:hasSecurityPolicy OpcUa:${SecurityPolicy} .";

	private String opcUaServerUserSnippet = "<${ModuleIri}_${ServerName}> OpcUa:requiresUserName \"${UserName}\";\r\n"
			+ "					OpcUa:requiresPassword \"${Password}\" .";

	private boolean serverDescription = false;

	public String generateOpcUaDescription(Server server, Object skill, StateMachine stateMachine,
			Enumeration<String> userFiles) {
		Skill skillAnnotation = skill.getClass().getAnnotation(Skill.class);

		String opcUaServerDescription = null;
		if (!serverDescription) {
			opcUaServerDescription = generateOpcUaServerDescription(server);
			serverDescription = true;
		}

		String opcUaSkillDescription = generateOpcUaSkillDescription(skill.getClass().getSimpleName(), stateMachine,
				server);
		String stateMachineDescription = generateStateMachineDescription(stateMachine);

		String userSnippet = getUserSnippets(userFiles, skill.getClass().getClassLoader());

		String completeSkillDescription;
		try {
			completeSkillDescription = getFileFromResources(null, "prefix.ttl") + opcUaServerDescription
					+ opcUaSkillDescription + stateMachineDescription + userSnippet;

			completeSkillDescription = completeSkillDescription.replace("${ModuleIri}", skillAnnotation.moduleIri())
					.replace("${ServerName}", server.getServer().getConfig().getApplicationName().getText())
					.replace("${CapabilityIri}", skillAnnotation.capabilityIri())
					.replace("${SkillIri}", skillAnnotation.skillIri());

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

					String skillParameterDescription = "";
					if (variableNode.getAccessLevel().toString().contains("READ_WRITE")) {
						skillParameterDescription = opcUaSkillParameterSnippet;
					}

					String variableDescription = skillParameterDescription + opcUaVariableSnippet;
					variableDescription = variableDescription
							.replace("${VariableName}", variableNode.getBrowseName().getName())
							.replace("${AccessLevel}", variableNode.getAccessLevel().toString())
							.replace("${DataType}", variableNode.getDataType().toParseableString())
							.replace("${Historizing}", variableNode.getHistorizing().toString())
							.replace("${UserAccessLevel}", variableNode.getUserAccessLevel().toString())
							.replace("${ValueRank}", variableNode.getValueRank().toString());

					if (variableNode.getValue() != null) {
						variableDescription.replace("${DefaultValue}", variableNode.getValue().toString());
					}

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

	public String generateOpcUaServerDescription(Server server) {

		OpcUaServer opcUaServer = server.getServer();
		String opcUaServerDescription = opcUaServerSnippet;

		List<EndpointDescription> endpointDescriptions = opcUaServer.getEndpointDescriptions();

		for (EndpointDescription endpointDescription : endpointDescriptions) {
			String securityPolicy = endpointDescription.getSecurityPolicyUri();
			securityPolicy = securityPolicy.substring(securityPolicy.lastIndexOf("/") + 1);
			securityPolicy = securityPolicy.replace("#", "_");

			String opcUaServerSecurity = opcUaServerSecuritySnippet
					.replace("${EndpointUrl}", endpointDescription.getEndpointUrl())
					.replace("${MessageSecurityMode}", endpointDescription.getSecurityMode().name())
					.replace("${SecurityPolicy}", securityPolicy);

			if (endpointDescription.getSecurityMode().name().equals("SignAndEncrypt")) {
				String opcUaServerUser = opcUaServerUserSnippet.replace("${UserName}", server.getUserName())
						.replace("${Password}", server.getUserPassword());
				opcUaServerDescription = opcUaServerDescription + opcUaServerUser;
			}

			opcUaServerDescription = opcUaServerDescription + opcUaServerSecurity;
			opcUaServerDescription = opcUaServerDescription.replace("${ServerName}",
					server.getServer().getConfig().getApplicationName().getText());
		}
		return opcUaServerDescription;
	}
}
