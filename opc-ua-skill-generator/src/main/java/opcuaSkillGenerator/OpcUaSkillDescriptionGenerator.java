package opcuaSkillGenerator;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.milo.opcua.sdk.core.nodes.Node;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.core.BuiltinDataType;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;

import skillup.annotations.Helper;
import skillup.annotations.Skill;
import skillup.annotations.SkillOutput;
import skillup.annotations.SkillParameter;
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
	private String opcUaSkillSnippet = "<${ModuleIri}> CSS:providesSkill <${SkillIri}> .\r\n"
			+ "<${SkillIri}> a CaSkMan:JavaSkill, owl:NamedIndividual;\r\n"
			+ "						CaSkMan:accessibleThroughOpcUaInterface <${SkillIri}_UaInterface>;\r\n"
			+ "						CSS:behaviorConformsTo <${SkillIri}_StateMachine>;\r\n"
			+ "						CaSk:hasCurrentState <${SkillIri}_StateMachine_${StateName}>.\r\n"
			+ "<${SkillIri}_UaInterface> a CaSkMan:OpcUaMethodSkillInterface."
			+ "<${SkillIri}_UaInterface> OpcUa:browseName \"${BrowseName}\";  \r\n"
			+ "						OpcUa:browseNamespace ${BrowseNamespace};\r\n"
			+ "						OpcUa:nodeId \"${NodeId}\";\r\n"
			+ "						OpcUa:nodeNamespace ${NodeNamespace};\r\n"
			+ "						OpcUa:displayName \"${DisplayName}\" ;\r\n"
			+ "						CSS:exposes <${SkillIri}_StateMachine>.\r\n"
			+ "<${ModuleIri}_${ServerName}_NodeSet> OpcUa:containsNode <${SkillIri}_UaInterface> .";

	private String capabilitySnippet = "<${CapabilityIri}> CSS:isRealizedBy <${SkillIri}>;\r\n"
			+ "					a CaSk:ProvidedCapability, owl:NamedIndividual. \r\n"
			+ "<${ModuleIri}> CSS:providesCapability <${CapabilityIri}> .";

	private String opcUaMethodSnippet = "<${SkillIri}_UaInterface_${MethodName}> a OpcUa:UAMethod,\r\n"
			+ "										CaSk:${MethodName},\r\n"
			+ "										owl:NamedIndiviual;\r\n"
			+ "									OpcUa:browseName \"${BrowseName}\";  \r\n"
			+ "									OpcUa:browseNamespace ${BrowseNamespace};\r\n"
			+ "									OpcUa:nodeId \"${NodeId}\";\r\n"
			+ "									OpcUa:nodeNamespace ${NodeNamespace};\r\n"
			+ "									OpcUa:displayName \"${DisplayName}\".   \r\n"
			+ "<${SkillIri}_UaInterface> CaSk:hasSkillMethod <${SkillIri}_UaInterface_${MethodName}>; \r\n"
			+ "									OpcUa:hasComponent <${SkillIri}_UaInterface_${MethodName}>. \r\n";

	private String opcUaMethodInvokesTransitionSnippet = "<${SkillIri}_UaInterface_${MethodName}> CaSk:invokes <${SkillIri}_StateMachine_${CommandName}_Command> .   \r\n";

	private String skillParameterSnippet = "<${SkillIri}_${VariableName}> a CSS:SkillParameter,\r\n"
			+ "										owl:NamedIndividual;\r\n"
			+ "								CaSk:hasVariableName \"${BrowseName}\";\r\n"
			+ "								CaSk:hasVariableType xsd:${VariableType};\r\n"
			+ "								CaSk:isRequired ${Required};\r\n"
			+ "								CaSk:hasDefaultValue ${DefaultValue}.\r\n"
			+ "<${SkillIri}> CSS:hasParameter <${SkillIri}_${VariableName}>.";

	private String skillParameterOptionSnippet = "<${SkillIri}_${VariableName}_Option${Number}> a CaSk:SkillVariableOption,\r\n"
			+ "										owl:NamedIndividual;\r\n"
			+ "								CaSk:hasOptionValue ${OptionValue}.\r\n"
			+ "<${SkillIri}_${VariableName}> CaSk:hasSkillVariableOption <${SkillIri}_${VariableName}_Option${Number}>.";

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
			+ "<${SkillIri}_UaInterface> OpcUa:organizes <${SkillIri}_${VariableName}>; \r\n"
			+ "							CSS:exposes <${SkillIri}_${VariableName}>. \r\n";
	
	private String skillOutputSnippet = "<${SkillIri}_${VariableName}> a CaSk:SkillOutput,\r\n"
			+ "										owl:NamedIndividual;\r\n"
			+ "								CaSk:hasVariableName \"${BrowseName}\";\r\n"
			+ "								CaSk:hasVariableType xsd:${VariableType};\r\n"
			+ "								CaSk:isRequired ${Required}.\r\n"
			+ "<${SkillIri}> CaSk:hasSkillOutput <${SkillIri}_${VariableName}>.";

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
	private Helper helper = new Helper();

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
		Node skillNode = opcUaNodes.stream().filter(opcUaNode -> opcUaNode.getBrowseName().getName().equals(skillName))
				.findFirst().get();
		String stateName = stateMachine.getState().toString()
				.substring(stateMachine.getState().toString().lastIndexOf(".") + 1);
		stateName = stateName.substring(0, stateName.lastIndexOf("State"));
		opcUaSkillDescription = opcUaSkillSnippet.replace("${StateName}", stateName);

		// if skill is connected with capability its added to the description
		if (!capability.isEmpty()) {
			opcUaSkillDescription = opcUaSkillDescription + capabilitySnippet;
		}

		opcUaSkillDescription = generateOpcUaSkillDataPropertyDescription(opcUaSkillDescription, skillNode);

		UaFolderNode folder = (UaFolderNode) skillNode;

		String methodDescription = generateOpcUaMethodDescription(folder);
		String variableDescription = generateOpcUaVariablesDescription(folder, skill);
		opcUaSkillDescription = opcUaSkillDescription + methodDescription + variableDescription;
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

		UaFolderNode methodFolder = getFolder(folder, "SkillMethods");

		String totalMethodDescription = "";

		// generate description for every component node (methods) of the skill
		for (UaNode componentNode : methodFolder.getComponentNodes()) {
			String methodDescription = generateOpcUaSkillDataPropertyDescription(opcUaMethodSnippet, componentNode);

			// for every method corresponding to an transition like start etc. this
			// connection between method and transition is added

			List<TransitionName> transitions = Arrays.asList(TransitionName.values());

			try {
				TransitionName transitionName = transitions.stream()
						.filter(transition -> transition.toString().equals(componentNode.getBrowseName().getName()))
						.findFirst().get();

				String opcUaMethodInvokesTransitionDescription = opcUaMethodInvokesTransitionSnippet
						.replace("${CommandName}", transitionName.toString().substring(0, 1).toUpperCase()
								+ transitionName.toString().substring(1));
				methodDescription = methodDescription + opcUaMethodInvokesTransitionDescription;
			} catch (NoSuchElementException e) {
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
	public String generateOpcUaVariablesDescription(UaFolderNode folder, Object skill) {

		String variablesDescription = "";

		List<Field> paramFields = helper.getVariables(skill, true);
		List<Field> outputFields = helper.getVariables(skill, false);

		String skillParameterDescription = "";
		String skillOutputDescription = "";

		UaFolderNode paramFolder = getFolder(folder, "SkillParameters");

		// generate description for every variable of the skill
		for (Node organizedNode : paramFolder.getOrganizesNodes()) {
			UaVariableNode variableNode = (UaVariableNode) organizedNode;

			try {
				Field paramField = paramFields.stream()
						.filter(field -> field.getAnnotation(SkillParameter.class).name()
								.equals(variableNode.getBrowseName().getName())
								|| field.getName().equals(variableNode.getBrowseName().getName()))
						.findFirst().get();

				paramField.setAccessible(true);

				boolean isRequired = paramField.getAnnotation(SkillParameter.class).isRequired();

				skillParameterDescription = skillParameterDescription
						+ skillParameterSnippet.replace("${Required}", Boolean.toString(isRequired))
								.replace("${DefaultValue}", paramField.get(skill).toString());
				// add parameter options to description
				skillParameterDescription = generateOptionValuesDescription(paramField, skillParameterDescription);
			} catch (IllegalArgumentException | IllegalAccessException | NoSuchElementException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			skillParameterDescription = generateOpcUaVariableDescription(variableNode, skillParameterDescription);
			skillParameterDescription = generateOpcUaSkillDataPropertyDescription(skillParameterDescription,
					organizedNode);
		}

		UaFolderNode outputFolder = getFolder(folder, "SkillOutputs");

		for (Node organizedNode : outputFolder.getOrganizesNodes()) {
			UaVariableNode variableNode = (UaVariableNode) organizedNode;

			try {
				Field outputField = outputFields.stream()
						.filter(field -> field.getAnnotation(SkillOutput.class).name()
								.equals(variableNode.getBrowseName().getName())
								|| field.getName().equals(variableNode.getBrowseName().getName()))
						.findFirst().get();

				outputField.setAccessible(true);

				boolean isRequiredOutput = outputField.getAnnotation(SkillOutput.class).isRequired();

				skillOutputDescription = skillOutputDescription
						+ skillOutputSnippet.replace("${Required}", Boolean.toString(isRequiredOutput));
			} catch (NoSuchElementException e) {
				e.printStackTrace();
			}

			skillOutputDescription = generateOpcUaVariableDescription(variableNode, skillOutputDescription);
			skillOutputDescription = generateOpcUaSkillDataPropertyDescription(skillOutputDescription, organizedNode);
		}

		variablesDescription = skillParameterDescription + skillOutputDescription;
		return variablesDescription;
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
				skillParameterDescription = skillParameterDescription + skillParameterOptionSnippet
						.replace("${Number}", Integer.toString(i)).replace("${OptionValue}", option);
				i++;
			}
		}
		return skillParameterDescription;
	}

	public UaFolderNode getFolder(UaFolderNode skillFolder, String folderName) {

		List<Node> skillNodes = skillFolder.getOrganizesNodes();

		Node searchedNode = skillNodes.stream().filter(node -> node.getBrowseName().getName().equals(folderName))
				.findFirst().get();

		UaFolderNode searchedFolder = (UaFolderNode) searchedNode;
		return searchedFolder;

	}

	public String generateOpcUaVariableDescription(UaVariableNode variableNode, String variableDescription) {

		variableDescription = variableDescription + opcUaVariableSnippet;
		String variableType = variableNode.getDataType().getIdentifier().toString();
		String opcUaDataType = BuiltinDataType.getBackingClass(Integer.parseInt(variableType)).getSimpleName()
				.toLowerCase();

		variableDescription = variableDescription.replace("${VariableName}", variableNode.getBrowseName().getName())
				.replace("${AccessLevel}", variableNode.getAccessLevel().toString())
				.replace("${DataType}", opcUaDataType).replace("${VariableType}", opcUaDataType)
				.replace("${Historizing}", variableNode.getHistorizing().toString())
				.replace("${UserAccessLevel}", variableNode.getUserAccessLevel().toString())
				.replace("${ValueRank}", variableNode.getValueRank().toString());

		return variableDescription;
	}
}
