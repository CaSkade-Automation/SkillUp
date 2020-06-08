package moduleRegistration;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import annotations.Module;
import moduleDescriptionGenerator.ModuleDescriptionGenerator;
import opsDescription.OpsDescription;
import registration.Registration;

@Component(immediate = true, service = ModuleRegistration.class)
public class ModuleRegistration extends Registration {

	private static DatagramSocket socket = null;
	private Logger logger = LoggerFactory.getLogger(ModuleRegistration.class);
	private Gson gson = new Gson();

	@Reference
	ModuleDescriptionGenerator moduleDescriptionGenerator;

	@Override
	public void register(String requestBody, Object object) {
		// TODO Auto-generated method stub

		for (OpsDescription myOpsDescription : getOpsDescriptionList()) {
			logger.info("Registering Module " + object.getClass().getAnnotation(Module.class).name()
					+ " with description in rdf syntax to " + myOpsDescription.getId());

			String moduleEndpoint = myOpsDescription.getModuleEndpoint();

			int responseStatusCode = opsRequest(myOpsDescription, "POST", moduleEndpoint, requestBody);

			if (responseStatusCode == 201) {
				logger.info("Module successfully registered...");
				getModules().put(object,
						moduleDescriptionGenerator.getModuleIri(object.getClass().getAnnotation(Module.class)));
			} else {
				logger.error("Module couldn't be registered...");
			}
		}
	}

	@Override
	public void delete(Object object) {
		// TODO Auto-generated method stub
		logger.info("Unregistering Module " + object.getClass().getAnnotation(Module.class).name());

		List<OpsDescription> delete = new ArrayList<OpsDescription>();

		for (OpsDescription myOps : getOpsDescriptionList()) {

			logger.info("Delete Module from " + myOps.getId());
			String moduleEndpoint = myOps.getModuleEndpoint();
			String moduleIriEncoded = encodeValue(
					moduleDescriptionGenerator.getModuleIri(object.getClass().getAnnotation(Module.class)));
			String location = moduleEndpoint + "/" + moduleIriEncoded;

			int responseStatusCode = opsRequest(myOps, "DELETE", location, "");

			if (responseStatusCode == 204) {
				delete.add(myOps);
				logger.info("Remove OPS " + myOps.getId() + " from OPS-Skill-List...");
				getOpsAndSkillList().remove(myOps.getId());
			}
		}
		logger.info("Remove every OPS which received DELETE from Module from OPS-List...");
		getOpsDescriptionList().removeAll(delete);
		opsListMessage();
		getModules().remove(object); 
	}

	/**
	 * Method to send broadcast message and to wait for answers of possible
	 * different OPSes.
	 * 
	 * @param broadcastMessage contains module description
	 * @throws IOException
	 */
	public void broadcast(Object module, String moduleInsert, String moduleIri) throws IOException {

		ModuleDescription moduleDescription = new ModuleDescription(
				module.getClass().getAnnotation(Module.class).name(),
				module.getClass().getAnnotation(Module.class).description());
		String broadcastMessage = gson.toJson(moduleDescription);
		logger.info("Broadcasting to get all OPS...");

		socket = new DatagramSocket(55555);
		socket.setBroadcast(true);

		byte[] buffer = broadcastMessage.getBytes();

		// get IP Address and convert it to binary form
		String[] str = new String[4];
		InetAddress localHost = InetAddress.getLocalHost();
		String ipAddress = localHost.getHostAddress();
		str = ipAddress.split("\\.");
		int[] ipAddressBinary = new int[32];
		ipAddressBinary = ipAddressToBinary(str);

		// get subnetmask and build broadcast address
		NetworkInterface networkInterface = NetworkInterface.getByInetAddress(localHost);
		int subnetMask = (int) networkInterface.getInterfaceAddresses().get(0).getNetworkPrefixLength();
		int[] broadcastAddress = new int[32];
		int t = 32 - subnetMask;

		for (int i = 0; i <= (31 - t); i++) {
			broadcastAddress[i] = ipAddressBinary[i];
		}

		for (int i = 31; i > (31 - t); i--) {
			broadcastAddress[i] = 1;
		}

		// converting broadcast address to decimal
		int[] broadcastAddressDecimal = convertAddressToDecimal(broadcastAddress);
		InetAddress address = InetAddress.getByName(broadcastAddressDecimal[0] + "." + broadcastAddressDecimal[1] + "."
				+ broadcastAddressDecimal[2] + "." + broadcastAddressDecimal[3]);

		logger.info("Broadcast-Address: " + address);
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, 15000);

		socket.send(packet);

		// Wait for a response
		byte[] recvBuf = new byte[15000];
		DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);

		Thread receivingBroadcastResponseThread = new Thread() {
			public void run() {
				try {
					socket.receive(receivePacket);
					broadcastResponse(receivePacket, moduleInsert, module);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};

		CompletableFuture.runAsync(receivingBroadcastResponseThread::run).orTimeout(20, TimeUnit.SECONDS)
				.exceptionally(throwable -> {
					logger.error("An error occured", throwable);
					return null;
				});

//		receivingBroadcastResponseThread.start();
//		try {
//			receivingBroadcastResponseThread.join(120000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		logger.info("Thread is closed...");
	}

	/**
	 * The description of OPS is converted from string to OpsDescription and handed
	 * over to method for registering the module
	 * 
	 * @param receivePacket response to broadcast
	 * @param moduleInsert  rdf description of module for registering it to OPS
	 */
	public void broadcastResponse(DatagramPacket receivePacket, String moduleInsert, Object module) {

		String ip = receivePacket.getAddress().getHostAddress();
		String opsDescriptionAsString = new String(receivePacket.getData()).trim();

		logger.info("OPS-IP: " + ip);
		logger.info("OPS-Description " + opsDescriptionAsString);

		// Create OPS-Description with IP!
		OpsDescription opsDescription = gson.fromJson(opsDescriptionAsString, OpsDescription.class);

		int port = opsDescription.getPort();
		String moduleEndpoint = opsDescription.getModuleEndpoint();
		String id = opsDescription.getId();
		String capabilityEndpoint = opsDescription.getCapabilityEndpoint();

		opsDescription = new OpsDescription(ip, id, port, moduleEndpoint, capabilityEndpoint);
		addOps(opsDescription);
		register(moduleInsert, module);
	}

	/**
	 * Converts IP address to the binary form
	 * 
	 * @param str IP address in String form
	 * @return IP address in binary form
	 */
	public static int[] ipAddressToBinary(String[] str) {
		int re[] = new int[32];
		int a, b, c, d, i, rem;
		a = b = c = d = 1;
		Stack<Integer> st = new Stack<Integer>();

		// Separate each number of the IP address
		if (str != null) {
			a = Integer.parseInt(str[0]);
			b = Integer.parseInt(str[1]);
			c = Integer.parseInt(str[2]);
			d = Integer.parseInt(str[3]);
		}

		// convert first number to binary
		for (i = 0; i <= 7; i++) {
			rem = a % 2;
			st.push(rem);
			a = a / 2;
		}

		// Obtain First octet
		for (i = 0; i <= 7; i++) {
			re[i] = st.pop();
		}

		// convert second number to binary
		for (i = 8; i <= 15; i++) {
			rem = b % 2;
			st.push(rem);
			b = b / 2;
		}

		// Obtain Second octet
		for (i = 8; i <= 15; i++) {
			re[i] = st.pop();
		}

		// convert Third number to binary
		for (i = 16; i <= 23; i++) {
			rem = c % 2;
			st.push(rem);
			c = c / 2;
		}

		// Obtain Third octet
		for (i = 16; i <= 23; i++) {
			re[i] = st.pop();
		}

		// convert fourth number to binary
		for (i = 24; i <= 31; i++) {
			rem = d % 2;
			st.push(rem);
			d = d / 2;
		}

		// Obtain Fourth octet
		for (i = 24; i <= 31; i++) {
			re[i] = st.pop();
		}
		return (re);
	}

	/**
	 * Converts address from binary to decimal form
	 * 
	 * @param bi address in binary form
	 * @return address in decimal form
	 */

	public static int[] convertAddressToDecimal(int[] bi) {

		int[] arr = new int[4];
		int a, b, c, d, i, j;
		a = b = c = d = 0;
		j = 7;

		for (i = 0; i < 8; i++) {
			a = a + (int) (Math.pow(2, j)) * bi[i];
			j--;
		}

		j = 7;
		for (i = 8; i < 16; i++) {
			b = b + bi[i] * (int) (Math.pow(2, j));
			j--;
		}

		j = 7;
		for (i = 16; i < 24; i++) {
			c = c + bi[i] * (int) (Math.pow(2, j));
			j--;
		}

		j = 7;
		for (i = 24; i < 32; i++) {
			d = d + bi[i] * (int) (Math.pow(2, j));
			j--;
		}

		arr[0] = a;
		arr[1] = b;
		arr[2] = c;
		arr[3] = d;
		return arr;
	}
}
