package opcUaServer;

import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig;
import org.eclipse.milo.opcua.sdk.server.identity.CompositeValidator;
import org.eclipse.milo.opcua.sdk.server.identity.UsernameIdentityValidator;
import org.eclipse.milo.opcua.sdk.server.identity.X509IdentityValidator;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.UaRuntimeException;
import org.eclipse.milo.opcua.stack.core.security.DefaultCertificateManager;
import org.eclipse.milo.opcua.stack.core.security.DefaultTrustListManager;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.transport.TransportProfile;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;
import org.eclipse.milo.opcua.stack.core.types.structured.BuildInfo;
import org.eclipse.milo.opcua.stack.core.util.CertificateUtil;
import org.eclipse.milo.opcua.stack.server.EndpointConfiguration;
import org.eclipse.milo.opcua.stack.server.security.DefaultServerCertificateValidator;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.collect.Lists.newArrayList;
import static org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig.USER_TOKEN_POLICY_ANONYMOUS;
import static org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig.USER_TOKEN_POLICY_USERNAME;
import static org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig.USER_TOKEN_POLICY_X509;

/**
 * OPC-UA-Server
 * 
 * @Component Indicates that annotated class is intended to be an OSGi
 *            component. <br>
 *            immediate=true, component configuration activates immediately <br>
 *            after becoming satisfied component is registered as a service
 */
@Component(immediate = true, service = Server.class)
public class Server {

	private static final int TCP_BIND_PORT = 4841;
	private Namespace namespace;
	private String userName = "user";
	private String userPassword = "password1";
	private final Logger logger = LoggerFactory.getLogger(Server.class);

	static {
		// Required for SecurityPolicy.Aes256_Sha256_RsaPss
		Security.addProvider(new BouncyCastleProvider());
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
	 * namespace. For more details take a look at eclipse milo on github.
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

		DefaultServerCertificateValidator certificateValidator = new DefaultServerCertificateValidator(
				trustListManager);

		UsernameIdentityValidator identityValidator = new UsernameIdentityValidator(true, authChallenge -> {
			String username = authChallenge.getUsername();
			String password = authChallenge.getPassword();

			boolean userOk = this.userName.equals(username) && this.userPassword.equals(password);
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
				.setApplicationName(LocalizedText.english("OPC_UA_Server")).setEndpoints(endpointConfigurations)
				.setBuildInfo(new BuildInfo("urn:my:server:", "HSU", "OPC_UA_Server", OpcUaServer.SDK_VERSION, "",
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
	 * Method creates TCP endpoints (without localhost because OPS have no access to
	 * OPC-UA Server via localhost) with and without security
	 * 
	 * @param certificate
	 * @return endpoint configurations
	 */
	private Set<EndpointConfiguration> createEndpointConfigurations(X509Certificate certificate) {
		Set<EndpointConfiguration> endpointConfigurations = new LinkedHashSet<>();

		List<String> bindAddresses = newArrayList();
		bindAddresses.add("0.0.0.0");

		Set<String> hostnames = new LinkedHashSet<>();
// 		hostnames.add(HostnameUtil.getHostname());
//		hostnames.addAll(HostnameUtil.getHostnames("0.0.0.0"));

		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface iface = interfaces.nextElement();
				// filters out 127.0.0.1 and inactive interfaces
				if (iface.isLoopback() || !iface.isUp())
					continue;

				Enumeration<InetAddress> addresses = iface.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress addr = addresses.nextElement();
					if (addr instanceof Inet4Address) {
						hostnames.add(addr.getHostAddress());
					}
				}
			}
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}

		// add every address to endpoint configuration
		for (String bindAddress : bindAddresses) {
			for (String hostname : hostnames) {
				EndpointConfiguration.Builder builder = EndpointConfiguration.newBuilder().setBindAddress(bindAddress)
						.setHostname(hostname).setPath("/hsu").setCertificate(certificate).addTokenPolicies(
								USER_TOKEN_POLICY_ANONYMOUS, USER_TOKEN_POLICY_USERNAME, USER_TOKEN_POLICY_X509);

				EndpointConfiguration.Builder noSecurityBuilder = builder.copy().setSecurityPolicy(SecurityPolicy.None)
						.setSecurityMode(MessageSecurityMode.None);

				// every address added as endpoint with and without security
				endpointConfigurations.add(buildTcpEndpoint(noSecurityBuilder));
				// TCP Basic256Sha256 / SignAndEncrypt
				endpointConfigurations
						.add(buildTcpEndpoint(builder.copy().setSecurityPolicy(SecurityPolicy.Basic256Sha256)
								.setSecurityMode(MessageSecurityMode.SignAndEncrypt)));

				/*
				 * discovery path taken out, because by creating description of server every
				 * endpoint is added and this endpoint is not usable for OPS to execute methods.
				 */

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

//				EndpointConfiguration.Builder discoveryBuilder = builder.copy().setPath("/discovery")
//						.setSecurityPolicy(SecurityPolicy.None).setSecurityMode(MessageSecurityMode.None);
//
//				endpointConfigurations.add(buildTcpEndpoint(discoveryBuilder));
			}
		}

		return endpointConfigurations;
	}

	/**
	 * Method to have access to user name for endpoint with security
	 * 
	 * @return userName
	 */
	public String getUserName() {
		return this.userName;
	}

	/**
	 * Method to have access to user password for endpoint with security
	 * 
	 * @return userPassword
	 */
	public String getUserPassword() {
		return this.userPassword;
	}

	/**
	 * Method to build tcp endpoint
	 * 
	 * @param base endpoint configuration builder
	 * @return endpoint configuration
	 */
	private static EndpointConfiguration buildTcpEndpoint(EndpointConfiguration.Builder base) {
		return base.copy().setTransportProfile(TransportProfile.TCP_UASC_UABINARY).setBindPort(TCP_BIND_PORT).build();
	}

	public OpcUaServer getServer() {
		return server;
	}

	public Namespace getNamespace() {
		return namespace;
	}

	public CompletableFuture<OpcUaServer> startup() {
		return server.startup();
	}

	public CompletableFuture<OpcUaServer> shutdown() {
		namespace.shutdown();
		return server.shutdown();
	}

	@Deactivate
	public void deactivate() {
		logger.info("OPC-UA-Server wird deaktiviert");
		server.shutdown();
	}
}