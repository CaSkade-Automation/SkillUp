package skillDescriptionGeneratorInterface;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.nodes.Node;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import annotations.Capability;
import annotations.DIN8580;
import server.Server;
import statemachine.StateMachine;

@Component(immediate = true, service = SkillDescriptionGenerator.class)
public class SkillDescriptionGenerator {

	private Logger logger = LoggerFactory.getLogger(SkillDescriptionGenerator.class);

	private String moduleSnippet = "registration:${MACAdress}_${ModuleName} a VDI3682:Module,\r\n"
			+ "					owl:NamedIndividual. ";

	private String modulePrefixSnippet = "@prefix module: <http://www.hsu-ifa.de/ontologies/OPS-Registration#${MACAdress}_${ModuleName}> .";

	private String opcUaServerSnippet = "module:_${ServerName} a OpcUa:UAServer,\r\n"
			+ "						owl:NamedIndividual;\r\n"
			+ "					OpcUa:hasNodeSet module:_${ServerName}_NodeSet.\r\n"
			+ "module:_${ServerName}_NodeSet a OpcUa:UANodeSet, \r\n"
			+ "									owl:NamedIndividual. \r\n" + "";

	private String opcUaServerSecuritySnippet = "module:_${ServerName} OpcUa:hasEndpointUrl \"${EndpointUrl}\";\r\n"
			+ "					OpcUa:requiresUserName \"${UserName}\";\r\n"
			+ "					OpcUa:requiresPassword \"${Password}\";\r\n"
			+ "					OpcUa:hasMessageSecurityMode OpcUa:MessageSecurityMode_${MessageSecurityMode}; \r\n"
			+ "					OpcUa:hasSecurityPolicy OpcUa:${SecurityPolicy} .";

	private String capabilitySnippet = "registration:${MACAdress}_${ModuleName} Cap:hasCapability module:_${CapabilityName}Capability .\r\n"
			+ "module:_${CapabilityName}Capability a Cap:Capability,\r\n"
			+ "									owl:NamedIndividual. ";

	private String capabilityDIN8580Snippet = "module:_${CapabilityName}Capability a DIN8580:{DIN8580Type}.  \r\n";

	private String opcUaSkillSnippet = "registration:${MACAdress}_${ModuleName} Cap:providesOpcUaSkill module:_${SkillName}OpcUaSkill .\r\n"
			+ "module:_${SkillName}OpcUaSkill a Cap:OpcUaSkill,\r\n"
			+ "							owl:NamedIndividual.\r\n"
			+ "module:_${CapabilityName}Capability Cap:isExecutableViaOpcUaSkill module:_${SkillName}OpcUaSkill .\r\n"
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

	private String moduleName;
	private String capabilityName;
	private String macAdress;
	private String prefix;
	private List<String> serverNames = new ArrayList<String>();

	public String generateModuleDescription(String moduleName) {
		this.moduleName = moduleName;
		try {
			prefix = getFileFromResources(this.getClass().getClassLoader(), "prefix.ttl");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		macAdress = getMacAddress();
		String module = moduleSnippet.replace("${ModuleName}", moduleName).replace("${MACAdress}", macAdress);

		if (prefix != null) {
			String moduleDescription = prefix + module;

			try {
				createFile(moduleDescription, "module.ttl");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return moduleDescription;
		} else {
			logger.error("Couldn't get prefix description from resources folder.");
			return null;
		}
	}

	public String generateOpcUaDescription(Server server, Object skill, StateMachine stateMachine) {

		String modulePrefix = modulePrefixSnippet; 
		String opcUaServerDescription = null;
		if (serverNames.isEmpty()) {
			serverNames.add(server.getServer().getConfig().getApplicationName().getText());
			opcUaServerDescription = generateOpcUaServerDescription(server);
		} else {
			for (String serverName : serverNames) {
				if (!server.getServer().getConfig().getApplicationName().getText().equals(serverName)) {
					serverNames.add(server.getServer().getConfig().getApplicationName().getText());
					opcUaServerDescription = generateOpcUaServerDescription(server);
				}
			}
		}
		String capabilityDescription = generateCapabilityDescription(skill);
		String opcUaSkillDescription = generateOpcUaSkillDescription(skill.getClass().getSimpleName(), stateMachine,
				server);
		String stateMachineDescription = generateStateMachineDescription(stateMachine);

		String completeSkillDescription = prefix + modulePrefix + opcUaServerDescription + capabilityDescription
				+ opcUaSkillDescription + stateMachineDescription;
		completeSkillDescription = completeSkillDescription.replace("${MACAdress}", macAdress)
				.replace("${ModuleName}", moduleName)
				.replace("${ServerName}", server.getServer().getConfig().getApplicationName().getText())
				.replace("${CapabilityName}", capabilityName).replace("${SkillName}", skill.getClass().getSimpleName());

		try {
			createFile(completeSkillDescription, "opcUaDescription.ttl");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return completeSkillDescription;
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

			opcUaServerDescription = opcUaServerDescription + opcUaServerSecurity;
		}
		return opcUaServerDescription;
	}

	public String generateCapabilityDescription(Object skill) {
		String capabilityDescription = capabilitySnippet;

		Capability capabilityAnnotation = skill.getClass().getAnnotation(Capability.class);
		if (capabilityAnnotation != null) {

			if (!capabilityAnnotation.name().isEmpty()) {
				this.capabilityName = capabilityAnnotation.name();
			} else {
				this.capabilityName = skill.getClass().getSimpleName();
			}
			if (!capabilityAnnotation.value().equals(DIN8580.None)) {
				String capabilityDIN8580Description = capabilityDIN8580Snippet.replace("{DIN8580Type}",
						capabilityAnnotation.value().name());
				capabilityDescription = capabilityDescription + capabilityDIN8580Description;
			}
		} else {
			this.capabilityName = skill.getClass().getSimpleName();
		}

		// if Bedingung: falls in- und outputs für capability existieren, die auch
		// reinmachen. Vllt mit Annotation in- und outputs festlegen

		return capabilityDescription;
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

	public String generateStateMachineDescription(StateMachine stateMachine) {

		String stateMachineDescription = null;

		try {
			stateMachineDescription = getFileFromResources(this.getClass().getClassLoader(), "stateMachine.ttl");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (stateMachineDescription != null) {
			return stateMachineDescription;
		} else {
			logger.error("Couldn't get stateMachineDescription from resources folder.");
			return null;
		}
	}

	public String generateOpcUaSkillDataPropertyDescription(String description, Node node) {
		String newDescription = description.replace("${BrowseName}", node.getBrowseName().getName())
				.replace("${BrowseNamespace}", node.getBrowseName().getNamespaceIndex().toString())
				.replace("${NodeId}", node.getNodeId().toParseableString())
				.replace("${NodeNamespace}", node.getNodeId().getNamespaceIndex().toString())
				.replace("${DisplayName}", node.getDisplayName().getText());

		return newDescription;
	}

	/**
	 * Method gets the file from resources folder, reads it and converts it to a
	 * string
	 * 
	 * @param fileName the name of file which we want from resources folder
	 * @return returns given file as string
	 */
	public String getFileFromResources(ClassLoader classLoader, String fileName) throws IOException {

		URL resource = classLoader.getResource(fileName);
		BufferedReader reader = new BufferedReader(new InputStreamReader(resource.openStream()));
		StringBuilder file = new StringBuilder();
		String currentline = "";

		while ((currentline = reader.readLine()) != null) {
			file.append(currentline);
		}
		String fileString = file.toString();
		return fileString;
	}

	public void createFile(String turtleFile, String localFileName) throws IOException {

		FileOutputStream fileOutputStream = new FileOutputStream("turtle-files/" + localFileName);
		byte[] strToBytes = turtleFile.getBytes();
		fileOutputStream.write(strToBytes);

		fileOutputStream.close();
	}

	/**
	 * Method to get the MAC address of the module
	 * 
	 * @return MAC address of module as a string
	 */
	public String getMacAddress() {
		// get all network interfaces of the current system
		Enumeration<NetworkInterface> networkInterface = null;
		String macAdress = null;
		try {
			networkInterface = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// iterate over all interfaces
		while (networkInterface.hasMoreElements()) {
			// get an interface
			NetworkInterface network = networkInterface.nextElement();
			// get its hardware or mac address
			byte[] macAddressBytes = null;
			try {
				macAddressBytes = network.getHardwareAddress();
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (macAddressBytes != null) {
				System.out.print("MAC address of interface \"" + network.getName() + "\" is : ");
				// initialize a string builder to hold mac address
				StringBuilder macAddressStr = new StringBuilder();
				// iterate over the bytes of mac address
				for (int i = 0; i < macAddressBytes.length; i++) {
					// convert byte to string in hexadecimal form and add a "-" to make it more
					// readable
					macAddressStr.append(
							String.format("%02x%s", macAddressBytes[i], (i < macAddressBytes.length - 1) ? "-" : ""));
				}
				macAdress = macAddressStr.toString();
				logger.info("MAC-Adresse: " + macAdress);
				break;
			}
		}
		return macAdress;
	}
}
