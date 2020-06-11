package moduleDescriptionGenerator;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.osgi.service.component.annotations.Component;

import annotations.Module;
import descriptionGenerator.DescriptionGenerator;
import server.Server;

@Component(immediate = true, service = ModuleDescriptionGenerator.class)
public class ModuleDescriptionGenerator extends DescriptionGenerator {

	private String moduleSnippet = "${ModuleIri} a VDI3682:Module,\r\n" + "					owl:NamedIndividual. ";

	private String capabilitySnippet = "${ModuleIri} Cap:hasCapability ${CapabilityIri} .";

	private String opcUaServerSnippet = "${ModuleIri}_${ServerName} a OpcUa:UAServer,\r\n"
			+ "						owl:NamedIndividual;\r\n"
			+ "					OpcUa:hasNodeSet module:_${ServerName}_NodeSet.\r\n"
			+ "${ModuleIri}_${ServerName}_NodeSet a OpcUa:UANodeSet, \r\n"
			+ "									owl:NamedIndividual. \r\n" + "";

	private String opcUaServerSecuritySnippet = "${ModuleIri}_${ServerName} OpcUa:hasEndpointUrl \"${EndpointUrl}\";\r\n"
			+ "					OpcUa:requiresUserName \"${UserName}\";\r\n"
			+ "					OpcUa:requiresPassword \"${Password}\";\r\n"
			+ "					OpcUa:hasMessageSecurityMode OpcUa:MessageSecurityMode_${MessageSecurityMode}; \r\n"
			+ "					OpcUa:hasSecurityPolicy OpcUa:${SecurityPolicy} .";

	public String generateModuleDescription(Object module, Server server, Enumeration<String> userFiles) {

		try {
			String prefix = getFileFromResources(null, "prefix.ttl");

			String opcUaServerDescription = generateOpcUaServerDescription(server);

			Module moduleAnnotation = module.getClass().getAnnotation(Module.class);

			String userSnippet = getUserSnippets(userFiles, module.getClass().getClassLoader());

			String moduleDescription = prefix + moduleSnippet + opcUaServerDescription + userSnippet;

			if (!moduleAnnotation.capabilityIri().isEmpty()) {
				moduleDescription = moduleDescription + capabilitySnippet;
			}

			moduleDescription = moduleDescription.replace("${ModuleIri}", "<" + moduleAnnotation.moduleIri() + ">")
					.replace("${CapabilityIri}", "<" + moduleAnnotation.capabilityIri() + ">");
			try {
				createFile(moduleDescription, "module.ttl");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return moduleDescription;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
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

			if (endpointDescription.getSecurityMode().name().equals("Sign&Encrypt")) {
				opcUaServerSecuritySnippet.replace("${UserName}", server.getUserName()).replace("${Password}",
						server.getUserPassword());
			}

			opcUaServerDescription = opcUaServerDescription + opcUaServerSecurity;
			opcUaServerDescription = opcUaServerDescription.replace("${ServerName}",
					server.getServer().getConfig().getApplicationName().getText());
		}
		return opcUaServerDescription;
	}
}
