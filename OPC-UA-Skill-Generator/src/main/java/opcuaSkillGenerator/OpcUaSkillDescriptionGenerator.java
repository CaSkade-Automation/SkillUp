package opcuaSkillGenerator;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.nodes.Node;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.core.BuiltinDataType;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;

import annotations.Skill;
import annotations.SkillOutput;
import annotations.SkillParameter;
import descriptionGenerator.SkillDescriptionGenerator;
import opcUaServer.Server;
import statemachine.Isa88StateMachine;
import states.TransitionName;

/**
 * Class to generate rdf description for OpcUa skill
 *
 */
public class OpcUaSkillDescriptionGenerator extends SkillDescriptionGenerator {

	// Snippets to create OpcUa description
	private String opcUaSkillSnippet = "<${ModuleIri}> Cap:providesOpcUaMethodSkill <${SkillIri}> .\r\n"
			+ "<${SkillIri}> a Cap:OpcUaMethodSkill,\r\n" + "							owl:NamedIndividual.\r\n"
			+ "<${SkillIri}> OpcUa:browseName \"${BrowseName}\";  \r\n"
			+ "						OpcUa:browseNamespace ${BrowseNamespace};\r\n"
			+ "						OpcUa:nodeId \"${NodeId}\";\r\n"
			+ "						OpcUa:nodeNamespace ${NodeNamespace};\r\n"
			+ "						OpcUa:displayName \"${DisplayName}\" ;\r\n"
			+ "						Cap:hasStateMachine <${SkillIri}_StateMachine>;\r\n"
			+ "						Cap:hasCurrentState <${SkillIri}_StateMachine_${StateName}>.\r\n"
			+ "<${ModuleIri}_${ServerName}_NodeSet> OpcUa:containsNode <${SkillIri}> .";

	private String capabilitySnippet = "<${CapabilityIri}> Cap:isExecutableViaOpcUaMethodSkill <${SkillIri}>;\r\n"
			+ "					a Cap:Capability,\r\n" + "					owl:NamedIndividual. \r\n"
			+ "<${ModuleIri}> Cap:hasCapability <${CapabilityIri}> .";

	private String opcUaMethodSnippet = "<${SkillIri}_${MethodName}> a OpcUa:UAMethod,\r\n"
			+ "										Cap:${MethodName},\r\n"
			+ "										owl:NamedIndiviual;\r\n"
			+ "									OpcUa:browseName \"${BrowseName}\";  \r\n"
			+ "									OpcUa:browseNamespace ${BrowseNamespace};\r\n"
			+ "									OpcUa:nodeId \"${NodeId}\";\r\n"
			+ "									OpcUa:nodeNamespace ${NodeNamespace};\r\n"
			+ "									OpcUa:displayName \"${DisplayName}\".   \r\n"
			+ "<${SkillIri}> Cap:hasSkillMethod <${SkillIri}_${MethodName}>; \r\n"
			+ "									OpcUa:hasComponent <${SkillIri}_${MethodName}>. \r\n";

	private String opcUaMethodInvokesTransitionSnippet = "<${SkillIri}_${MethodName}> Cap:invokes <${SkillIri}_StateMachine_${CommandName}_Command> .   \r\n";

	private String opcUaVariableSnippet = "<${SkillIri}_${VariableName}> a OpcUa:UAVariable,\r\n"
			+ "										owl:NamedIndividual;\r\n"
			+ "									OpcUa:browseName \"${BrowseName}\";  \r\n"
			+ "									OpcUa:browseNamespace ${BrowseNamespace};\r\n"
			+ "									OpcUa:nodeId \"${NodeId}\";\r\n"
			+ "									OpcUa:nodeNamespace ${NodeNamespace};\r\n"
			+ "									OpcUa:displayName \"${DisplayName}\";\r\n"
			+ "									OpcUa:accessLevel ${AccessLevel};\r\n"
			+ "									OpcUa:hasDataType xsd:${DataType};\r\n"
			+ "									OpcUa:historizing ${Historizing};\r\n"
			+ "									OpcUa:userAccessLevel ${UserAccessLevel};\r\n"
			+ "									OpcUa:valueRank ${ValueRank}.\r\n"
			+ "<${SkillIri}> OpcUa:organizes <${SkillIri}_${VariableName}>. \r\n";

	private String opcUaSkillParameterSnippet = "<${SkillIri}_${VariableName}> a Cap:SkillParameter,\r\n"
			+ "										owl:NamedIndividual;\r\n"
			+ "								Cap:hasVariableName \"${BrowseName}\";\r\n"
			+ "								Cap:hasVariableType xsd:${VariableType};\r\n"
			+ "								Cap:isRequired ${Required};\r\n"
			+ "								Cap:hasDefaultValue ${DefaultValue}.\r\n"
			+ "<${SkillIri}> Cap:hasSkillParameter <${SkillIri}_${VariableName}>.";

	private String opcUaSkillParameterOptionSnippet = "<${SkillIri}_${VariableName}_Option${Number}> a Cap:SkillVariableOption,\r\n"
			+ "										owl:NamedIndividual;\r\n"
			+ "								Cap:hasOptionValue ${OptionValue}.\r\n"
			+ "<${SkillIri}_${VariableName}> Cap:hasSkillVariableOption <${SkillIri}_${VariableName}_Option${Number}>.";

	private String opcUaSkillOutputSnippet = "<${SkillIri}_${VariableName}> a Cap:SkillOutput,\r\n"
			+ "										owl:NamedIndividual;\r\n"
			+ "								Cap:hasVariableName \"${BrowseName}\";\r\n"
			+ "								Cap:hasVariableType OpcUa:${VariableType};\r\n"
			+ "								Cap:isRequired ${Required}.\r\n"
			+ "<${SkillIri}> Cap:hasSkillOutput <${SkillIri}_${VariableName}>.";

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

	/**
	 * Method to generate OpcUa description for a OpcUa skill
	 * 
	 * @param server       OpcUa Server to generate its description
	 * @param skill        skills object to generate its description
	 * @param stateMachine skills stateMachine to get current state
	 * @param userFiles    additional user written rdf description
	 * @return OpcUa rdf description
	 */
	public String generateOpcUaDescription(Server server, Object skill, Isa88StateMachine stateMachine,
			Enumeration<String> userFiles) {
		Skill skillAnnotation = skill.getClass().getAnnotation(Skill.class);

		String opcUaServerDescription = "";
		// if OpcUa server description already set, its not written another time
		if (!serverDescription) {
			opcUaServerDescription = generateOpcUaServerDescription(server);
			serverDescription = true;
		}

		String opcUaSkillDescription = generateOpcUaSkillDescription(skill, stateMachine, server);
		String stateMachineDescription = generateStateMachineDescription();

		String userSnippet = getUserSnippets(userFiles, skill.getClass().getClassLoader());

		// whole description of new skill
		String completeSkillDescription = getFileFromResources(null, "prefix.ttl") + opcUaServerDescription
				+ opcUaSkillDescription + stateMachineDescription + userSnippet;

		// replace some dummies in description
		completeSkillDescription = completeSkillDescription.replace("${ModuleIri}", skillAnnotation.moduleIri())
				.replace("${ServerName}", server.getServer().getConfig().getApplicationName().getText())
				.replace("${CapabilityIri}", skillAnnotation.capabilityIri())
				.replace("${SkillIri}", skillAnnotation.skillIri());

		try {
			createFile(completeSkillDescription, "opcUaDescription.ttl");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return completeSkillDescription;
	}

	/**
	 * Method to tell that OpcUa Server is gone so that server description has to be
	 * written again next time a server comes up
	 */
	public void setServerDescriptionBoolBack() {
		this.serverDescription = false;
	}

	/**
	 * Method to generate OpcUa Server description in rdf syntax
	 * 
	 * @param server OpcUa server for the rdf description has to be created
	 * @return OpcUa server description
	 */
	public String generateOpcUaServerDescription(Server server) {

		OpcUaServer opcUaServer = server.getServer();
		String opcUaServerDescription = opcUaServerSnippet;

		List<EndpointDescription> endpointDescriptions = opcUaServer.getEndpointDescriptions();

		// every endpoint description is added to the rdf descripiton
		for (EndpointDescription endpointDescription : endpointDescriptions) {
			String securityPolicy = endpointDescription.getSecurityPolicyUri();
			securityPolicy = securityPolicy.substring(securityPolicy.lastIndexOf("/") + 1);
			securityPolicy = securityPolicy.replace("#", "_");

			// some dummies are replaced
			String opcUaServerSecurity = opcUaServerSecuritySnippet
					.replace("${EndpointUrl}", endpointDescription.getEndpointUrl())
					.replace("${MessageSecurityMode}", endpointDescription.getSecurityMode().name())
					.replace("${SecurityPolicy}", securityPolicy);

			// if an password is necessary this is added to the description
			if (endpointDescription.getSecurityMode().name().equals("SignAndEncrypt")) {
				String opcUaServerUser = opcUaServerUserSnippet.replace("${UserName}", server.getUserName())
						.replace("${Password}", server.getUserPassword());
				opcUaServerDescription = opcUaServerDescription + opcUaServerUser;
			}
			opcUaServerDescription = opcUaServerDescription + opcUaServerSecurity;
		}
		return opcUaServerDescription;
	}

	/**
	 * Method to generate OpcUa skill description with its variables and methods
	 * 
	 * @param skill        skills object to get IRIs etc.
	 * @param stateMachine skills stateMachine to get current state
	 * @param server       OpcUa server
	 * @return OpcUa skill rdf description
	 */
	public String generateOpcUaSkillDescription(Object skill, Isa88StateMachine stateMachine, Server server) {

		String skillName = skill.getClass().getAnnotation(Skill.class).skillIri();
		String capability = skill.getClass().getAnnotation(Skill.class).capabilityIri();

		String opcUaSkillDescription = "";
		List<Node> opcUaNodes = server.getNamespace().getFolder().getOrganizesNodes();

		// description is only created for skill node
		for (Node node : opcUaNodes) {
			if (node.getBrowseName().getName().equals(skillName)) {
				String stateName = stateMachine.getState().toString()
						.substring(stateMachine.getState().toString().lastIndexOf(".") + 1);
				stateName = stateName.substring(0, stateName.lastIndexOf("State"));
				opcUaSkillDescription = opcUaSkillSnippet.replace("${StateName}", stateName);

				// if skill is connected with capability its added to the description
				if (!capability.isEmpty()) {
					opcUaSkillDescription = opcUaSkillDescription + capabilitySnippet;
				}

				opcUaSkillDescription = generateOpcUaSkillDataPropertyDescription(opcUaSkillDescription, node);

				UaFolderNode folder = (UaFolderNode) node;

				String methodDescription = generateOpcUaMethodDescription(folder);
				String variableDescription = generateOpcUaVariableDescription(folder, skill);
				opcUaSkillDescription = opcUaSkillDescription + methodDescription + variableDescription;
				break;
			}
		}
		return opcUaSkillDescription;
	}

	/**
	 * Method generates for every OpcUa node default data properties like nodeId
	 * 
	 * @param description current rdf description
	 * @param node        current node to replace its default data properties
	 * @return changed rdf description
	 */
	public String generateOpcUaSkillDataPropertyDescription(String description, Node node) {
		String newDescription = description.replace("${BrowseName}", node.getBrowseName().getName())
				.replace("${BrowseNamespace}", node.getBrowseName().getNamespaceIndex().toString())
				.replace("${NodeId}", node.getNodeId().toParseableString())
				.replace("${NodeNamespace}", node.getNodeId().getNamespaceIndex().toString())
				.replace("${DisplayName}", node.getDisplayName().getText());

		return newDescription;
	}

	/**
	 * Method to generate description of skills methods like start etc.
	 * 
	 * @param folder node to get all component nodes which represent methods
	 * @return total description of all methods
	 */
	public String generateOpcUaMethodDescription(UaFolderNode folder) {

		String totalMethodDescription = "";

		// generate description for every component node (methods) of the skill
		for (UaNode componentNode : folder.getComponentNodes()) {
			String methodDescription = generateOpcUaSkillDataPropertyDescription(opcUaMethodSnippet, componentNode);

			// for every method corresponding to an transition like start etc. this
			// connection between method and transition is added
			for (TransitionName transition : TransitionName.values()) {
				if (componentNode.getBrowseName().getName().equals(transition.toString())) {
					String opcUaMethodInvokesTransitionDescription = opcUaMethodInvokesTransitionSnippet.replace(
							"${CommandName}", componentNode.getBrowseName().getName().substring(0, 1).toUpperCase()
									+ componentNode.getBrowseName().getName().substring(1));
					methodDescription = methodDescription + opcUaMethodInvokesTransitionDescription;
					break;
				}
			}
			methodDescription = methodDescription.replace("${MethodName}",
					componentNode.getBrowseName().getName().substring(0, 1).toUpperCase()
							+ componentNode.getBrowseName().getName().substring(1));
			totalMethodDescription = totalMethodDescription + methodDescription;
		}
		return totalMethodDescription;
	}

	/**
	 * Method to generate description of skills variables (parameter/output)
	 * 
	 * @param folder node to get all organized nodes which represent variables
	 * @param skill  skills object to get fields
	 * @return total description of all variables
	 */
	public String generateOpcUaVariableDescription(UaFolderNode folder, Object skill) {

		String totalVariableDescription = "";

		// generate description for every organized node (variables) of the skill
		for (Node organizedNode : folder.getOrganizesNodes()) {
			UaVariableNode variableNode = (UaVariableNode) organizedNode;

			String skillParameterDescription = "";
			String skillOutputDescription = "";

			Field[] fields = skill.getClass().getDeclaredFields();
			for (Field field : fields) {
				// differentiation between skill parameter and output
				if (field.isAnnotationPresent(SkillParameter.class)) {
					field.setAccessible(true);
					if ((field.getAnnotation(SkillParameter.class).name()
							.equals(variableNode.getBrowseName().getName()))
							|| field.getName().equals(variableNode.getBrowseName().getName())) {

						boolean isRequired = field.getAnnotation(SkillParameter.class).isRequired();
						try {
							skillParameterDescription = opcUaSkillParameterSnippet
									.replace("${Required}", Boolean.toString(isRequired))
									.replace("${DefaultValue}", field.get(skill).toString());
							// add parameter options to description
							skillParameterDescription = generateOptionValuesDescription(field,
									skillParameterDescription);
						} catch (IllegalArgumentException | IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						break;
					}
				} else if (field.isAnnotationPresent(SkillOutput.class)) {
					field.setAccessible(true);
					if ((field.getAnnotation(SkillOutput.class).name().equals(variableNode.getBrowseName().getName()))
							|| field.getName().equals(variableNode.getBrowseName().getName())) {

						boolean isRequired = field.getAnnotation(SkillOutput.class).isRequired();

						skillOutputDescription = opcUaSkillOutputSnippet.replace("${Required}",
								Boolean.toString(isRequired));
						break;
					}
				}
			}

			String variableType = variableNode.getDataType().getIdentifier().toString();
			String opcUaDataType = BuiltinDataType.getBackingClass(Integer.parseInt(variableType)).getSimpleName().toLowerCase();

			String variableDescription = skillParameterDescription + skillOutputDescription + opcUaVariableSnippet;
			variableDescription = variableDescription.replace("${VariableName}", variableNode.getBrowseName().getName())
					.replace("${AccessLevel}", variableNode.getAccessLevel().toString())
					.replace("${DataType}", opcUaDataType).replace("${VariableType}", opcUaDataType)
					.replace("${Historizing}", variableNode.getHistorizing().toString())
					.replace("${UserAccessLevel}", variableNode.getUserAccessLevel().toString())
					.replace("${ValueRank}", variableNode.getValueRank().toString());

			variableDescription = generateOpcUaSkillDataPropertyDescription(variableDescription, organizedNode);
			totalVariableDescription = totalVariableDescription + variableDescription;
		}
		return totalVariableDescription;
	}

	/**
	 * Method adds skill parameter option values to description
	 * 
	 * @param field                     Field of skill parameter
	 * @param skillParameterDescription actual description
	 * @return changed description
	 */
	public String generateOptionValuesDescription(Field field, String skillParameterDescription) {
		String options[] = field.getAnnotation(SkillParameter.class).option();
		int i = 1;
		// only if options are not empty, option values are added to the description
		if (options.length <= 0)
			return skillParameterDescription;
		for (String option : options) {
			if (!option.isEmpty()) {
				skillParameterDescription = skillParameterDescription + opcUaSkillParameterOptionSnippet
						.replace("${Number}", Integer.toString(i)).replace("${OptionValue}", option);
				i++;
			}
		}
		return skillParameterDescription;
	}
}
