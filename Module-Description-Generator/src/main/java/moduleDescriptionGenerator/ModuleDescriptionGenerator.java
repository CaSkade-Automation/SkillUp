package moduleDescriptionGenerator;

import java.io.IOException;
import java.util.List;

import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.osgi.service.component.annotations.Component;

import annotations.Module;
import descriptionGenerator.DescriptionGenerator;
import server.Server;

@Component(immediate = true, service = ModuleDescriptionGenerator.class)
public class ModuleDescriptionGenerator extends DescriptionGenerator {

	private String registrationPrefixSnippet = "@prefix registration: <${Namespace}/modules#> .";

	private String moduleSnippet = "registration:${MACAddress}_${ModuleName} a VDI3682:Module,\r\n"
			+ "					owl:NamedIndividual. ";

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

	public String generateModuleDescription(Object module, Server server) {

		try {
			String prefix = getFileFromResources(null, "prefix.ttl");

			String macAddress = getMacAddress();
			setMacAddress(macAddress);

			String capabilityDescription = generateCapabilityDescription(module, true);

			String opcUaServerDescription = generateOpcUaServerDescription(server);

			Module moduleAnnotation = module.getClass().getAnnotation(Module.class);

			String moduleDescription = prefix + registrationPrefixSnippet + getModulePrefixSnippet() + moduleSnippet
					+ capabilityDescription + opcUaServerDescription;
			moduleDescription = moduleDescription.replace("${Namespace}", moduleAnnotation.namespace())
					.replace("${ModuleName}", moduleAnnotation.name()).replace("${MACAddress}", macAddress);
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

			opcUaServerDescription = opcUaServerDescription + opcUaServerSecurity;
			opcUaServerDescription = opcUaServerDescription.replace("${ServerName}",
					server.getServer().getConfig().getApplicationName().getText());
		}
		return opcUaServerDescription;
	}
}
