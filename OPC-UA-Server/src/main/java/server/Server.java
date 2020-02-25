package server;

import java.io.File;
import java.io.IOException;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig;
import org.eclipse.milo.opcua.sdk.server.identity.CompositeValidator;
import org.eclipse.milo.opcua.sdk.server.identity.UsernameIdentityValidator;
import org.eclipse.milo.opcua.sdk.server.identity.X509IdentityValidator;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaMethodNode;
import org.eclipse.milo.opcua.sdk.server.util.HostnameUtil;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.UaRuntimeException;
import org.eclipse.milo.opcua.stack.core.security.DefaultCertificateManager;
import org.eclipse.milo.opcua.stack.core.security.DefaultCertificateValidator;
import org.eclipse.milo.opcua.stack.core.security.DefaultTrustListManager;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.transport.TransportProfile;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;
import org.eclipse.milo.opcua.stack.core.types.structured.Argument;
import org.eclipse.milo.opcua.stack.core.types.structured.BuildInfo;
import org.eclipse.milo.opcua.stack.core.util.CertificateUtil;
import org.eclipse.milo.opcua.stack.server.EndpointConfiguration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import opcuaSkillRegistration.OPCUASkillRegistration;
import smartModule.SmartModule;

import static com.google.common.collect.Lists.newArrayList;
import static org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig.USER_TOKEN_POLICY_ANONYMOUS;
import static org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig.USER_TOKEN_POLICY_USERNAME;
import static org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig.USER_TOKEN_POLICY_X509;

/**
 * @Component Indicates that annotated class is intended to be an OSGi
 *            component. <br>
 *            immediate=true, component configuration activates immediately <br>
 *            after becoming satisfied component is registered as a service
 */
@Component(immediate = true, service = Server.class)
public class Server {

	private static final int TCP_BIND_PORT = 4841;
	private Namespace namespace;
	private AnnotationEvaluation annotationEvaluation = new AnnotationEvaluation();
	private final Logger logger = LoggerFactory.getLogger(Server.class);

	static {
		// Required for SecurityPolicy.Aes256_Sha256_RsaPss
		Security.addProvider(new BouncyCastleProvider());
	}

	@Reference
	SmartModule module;

	/**
	 * This method is called to bind a new service to the component and adds
	 * referenced skill as a node to the server
	 * 
	 * @Reference used to specify dependency on other services, here:
	 *            MethodRegistration <br>
	 *            cardinality=MULTIPLE (0...n), reference is optional and multiple
	 *            bound services are supported <br>
	 *            policy=DYNAMIC, SCR(Service Component Runtime) can change the set
	 *            of bound services without deactivating the Component Configuration
	 *            -> method can be called while component is active and not only
	 *            before the activate method <br>
	 * @param skillRegistration Service instance of referenced skill is passed
	 */
	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	void bindMethodRegistration(OPCUASkillRegistration skillRegistration) {

		logger.info("OPC-UA-Skill found");
		Map<String, Argument[]> argumentsMap = annotationEvaluation.evaluateAnnotation(skillRegistration);
		Argument[] inputArguments = argumentsMap.get("inputArguments");
		Argument[] outputArguments = argumentsMap.get("outputArguments");

		UaFolderNode folder = namespace.getFolder();
		String skillName = skillRegistration.getClass().getName();
		skillName = skillName.substring(skillName.lastIndexOf(".") + 1);
		namespace.addMethod(folder, skillName, skillRegistration, inputArguments, outputArguments);
		String skillFile = getFileFromResources(skillRegistration.getClass().getClassLoader(), "DeleteSkill.rdf");
		skillFile = skillFile.replace("SkillName", skillName);
		skillFile = skillFile.replace("PathName", "simple");
		module.registerSkill(skillFile, skillName);
	}

	/**
	 * This method is called to unbind a (bound) service and deletes node of
	 * referenced skill from the server
	 * 
	 * @param skillRegistration skill instance of referenced skill is passed
	 */
	void unbindMethodRegistration(OPCUASkillRegistration skillRegistration) {

		String skillName = skillRegistration.getClass().getName();
		List<UaMethodNode> skillNodes = namespace.getFolder().getMethodNodes();
		for (UaMethodNode skillNode : skillNodes) {
			if (skillNode.getBrowseName().getName().equals(skillName.substring(skillName.lastIndexOf(".") + 1))) {
				skillNode.delete();
			}
		}
	}

	/**
	 * Server is started <br>
	 * 
	 * Constructor hasn't to be added to activate method, because an OSGi bundle
	 * starts constructor automatically. Without future.get so that the activate
	 * method doesn't block until the future is reached.
	 * 
	 * @Activate method that should be called on component activation
	 * @throws Exception
	 */
	@Activate
	public void activate() throws Exception {

		logger.info("OPC-UA-Server wird aktiviert");

		server.startup().get();

		final CompletableFuture<Void> future = new CompletableFuture<>();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> future.complete(null)));
	}

	private final OpcUaServer server;

	/**
	 * Constructor uses/creates security file, loads keystoreloader, sets username
	 * and password and builds server while setting endpoints etc. and starts
	 * namespace
	 * 
	 * @throws Exception
	 */
	public Server() throws Exception {

		File securityTempDir = new File(System.getProperty("security-server"), "security");
		if (!securityTempDir.exists() && !securityTempDir.mkdirs()) {
			throw new Exception("unable to create security temp dir: " + securityTempDir);
		}
		LoggerFactory.getLogger(getClass()).info("security temp dir: {}", securityTempDir.getAbsolutePath());

		KeyStoreLoader loader = new KeyStoreLoader().load(securityTempDir);

		DefaultCertificateManager certificateManager = new DefaultCertificateManager(loader.getServerKeyPair(),
				loader.getServerCertificateChain());

		File pkiDir = securityTempDir.toPath().resolve("pki").toFile();
		DefaultTrustListManager trustListManager = new DefaultTrustListManager(pkiDir);
		LoggerFactory.getLogger(getClass()).info("pki dir: {}", pkiDir.getAbsolutePath());

		DefaultCertificateValidator certificateValidator = new DefaultCertificateValidator(trustListManager);

		UsernameIdentityValidator identityValidator = new UsernameIdentityValidator(true, authChallenge -> {
			String username = authChallenge.getUsername();
			String password = authChallenge.getPassword();

			boolean userOk = "user".equals(username) && "password1".equals(password);
			boolean adminOk = "admin".equals(username) && "password2".equals(password);

			return userOk || adminOk;
		});

		X509IdentityValidator x509IdentityValidator = new X509IdentityValidator(c -> true);

		// If you need to use multiple certificates you'll have to be smarter than this.
		X509Certificate certificate = certificateManager.getCertificates().stream().findFirst()
				.orElseThrow(() -> new UaRuntimeException(StatusCodes.Bad_ConfigurationError, "no certificate found"));

		// The configured application URI must match the one in the certificate(s)
		String applicationUri = CertificateUtil.getSanUri(certificate)
				.orElseThrow(() -> new UaRuntimeException(StatusCodes.Bad_ConfigurationError,
						"certificate is missing the application URI"));

		Set<EndpointConfiguration> endpointConfigurations = createEndpointConfigurations(certificate);

		OpcUaServerConfig serverConfig = OpcUaServerConfig.builder().setApplicationUri(applicationUri)
				.setApplicationName(LocalizedText.english("OPC UA Server")).setEndpoints(endpointConfigurations)
				.setBuildInfo(new BuildInfo("urn:my:server:", "HSU", "OPC UA Server", OpcUaServer.SDK_VERSION, "",
						DateTime.now()))
				.setCertificateManager(certificateManager).setTrustListManager(trustListManager)
				.setCertificateValidator(certificateValidator)
				.setIdentityValidator(new CompositeValidator(identityValidator, x509IdentityValidator))
				.setProductUri("urn:my:server").build();

		server = new OpcUaServer(serverConfig);

		this.namespace = new Namespace(server);
		namespace.startup();
	}

	/**
	 * Method creates TCP endpoints with and without security
	 * 
	 * @param certificate
	 * @return
	 */
	private Set<EndpointConfiguration> createEndpointConfigurations(X509Certificate certificate) {
		Set<EndpointConfiguration> endpointConfigurations = new LinkedHashSet<>();

		List<String> bindAddresses = newArrayList();
		bindAddresses.add("0.0.0.0");

		Set<String> hostnames = new LinkedHashSet<>();
		hostnames.add(HostnameUtil.getHostname());
		hostnames.addAll(HostnameUtil.getHostnames("0.0.0.0"));

		for (String bindAddress : bindAddresses) {
			for (String hostname : hostnames) {
				EndpointConfiguration.Builder builder = EndpointConfiguration.newBuilder().setBindAddress(bindAddress)
						.setHostname(hostname).setPath("/milo").setCertificate(certificate).addTokenPolicies(
								USER_TOKEN_POLICY_ANONYMOUS, USER_TOKEN_POLICY_USERNAME, USER_TOKEN_POLICY_X509);

				EndpointConfiguration.Builder noSecurityBuilder = builder.copy().setSecurityPolicy(SecurityPolicy.None)
						.setSecurityMode(MessageSecurityMode.None);

				endpointConfigurations.add(buildTcpEndpoint(noSecurityBuilder));

				// TCP Basic256Sha256 / SignAndEncrypt
				endpointConfigurations
						.add(buildTcpEndpoint(builder.copy().setSecurityPolicy(SecurityPolicy.Basic256Sha256)
								.setSecurityMode(MessageSecurityMode.SignAndEncrypt)));

				/*
				 * It's good practice to provide a discovery-specific endpoint with no security.
				 * It's required practice if all regular endpoints have security configured.
				 *
				 * Usage of the "/discovery" suffix is defined by OPC UA Part 6:
				 *
				 * Each OPC UA Server Application implements the Discovery Service Set. If the
				 * OPC UA Server requires a different address for this Endpoint it shall create
				 * the address by appending the path "/discovery" to its base address.
				 */

				EndpointConfiguration.Builder discoveryBuilder = builder.copy().setPath("/discovery")
						.setSecurityPolicy(SecurityPolicy.None).setSecurityMode(MessageSecurityMode.None);

				endpointConfigurations.add(buildTcpEndpoint(discoveryBuilder));
			}
		}

		return endpointConfigurations;
	}

	private static EndpointConfiguration buildTcpEndpoint(EndpointConfiguration.Builder base) {
		return base.copy().setTransportProfile(TransportProfile.TCP_UASC_UABINARY).setBindPort(TCP_BIND_PORT).build();
	}

	public OpcUaServer getServer() {
		return server;
	}

	public CompletableFuture<OpcUaServer> startup() {
		return server.startup();
	}

	public CompletableFuture<OpcUaServer> shutdown() {
		return server.shutdown();
	}

	/**
	 * Method gets the file from resources folder with method of smart module, reads
	 * it and converts it to a string
	 * 
	 * @param classLoader of class to get resources folder of the class
	 * @param fileName    the name of file which we want from resources folder
	 * @return returns given file as string
	 */
	public String getFileFromResources(ClassLoader classLoader, String fileName) {
		String file = null;
		try {
			file = module.getFileFromResources(classLoader, fileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return file;
	}

	@Deactivate
	public void deactivate() {
		logger.info("OPC-UA-Server wird deaktiviert");
	}
}