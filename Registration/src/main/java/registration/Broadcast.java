package registration;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class Broadcast {

	private Logger logger = LoggerFactory.getLogger(Broadcast.class);
	private Gson gson = new Gson();
	private static DatagramSocket socket = null;
	private List<String> opsIDs = new ArrayList<String>();

	/**
	 * Method to send broadcast message and to wait for answers of possible
	 * different OPSes.
	 * 
	 * @param broadcastMessage contains module description
	 * @throws IOException
	 */
	public void broadcast(ModuleRegistry moduleRegistry) throws IOException {

		String broadcastMessage = "Hello World!";
		broadcastMessage = gson.toJson(broadcastMessage);
		logger.info("Broadcasting to get all OPS...");

		socket = new DatagramSocket(55555);
		socket.setBroadcast(true);

		byte[] buffer = broadcastMessage.getBytes();

		// get all possible IP broadcast addresses
		ArrayList<InetAddress> broadcasts = new ArrayList<InetAddress>();

		Enumeration<NetworkInterface> n = null;
		try {
			n = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		for (; n.hasMoreElements();) {
			NetworkInterface e = n.nextElement();
			if (e.isVirtual()) {
				continue;
			}
			List<InterfaceAddress> i = e.getInterfaceAddresses();
			for (InterfaceAddress intAddr : i) {
				if (intAddr.getAddress() instanceof Inet4Address) {
					if (intAddr.getBroadcast() != null) {
						broadcasts.add(intAddr.getBroadcast());
						logger.info("Found broadcast address " + intAddr.getBroadcast());

					}
				}
			}
		}

		// send broadcast on all found addresses
		for (InetAddress b : broadcasts) {
			DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, b, 15000);
			logger.info("Sending broadcast on " + b.toString());
			socket.send(sendPacket);
		}

		// wait for a response
		byte[] receiveData = new byte[2048];
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

		Thread receivingBroadcastResponseThread = new Thread() {
			public void run() {
				try {
					while (true) {
						socket.receive(receivePacket);
						broadcastResponse(receivePacket, moduleRegistry);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};

		CompletableFuture.runAsync(receivingBroadcastResponseThread::run).orTimeout(20, TimeUnit.SECONDS)
				.exceptionally(throwable -> {
					logger.info("No more OPS found");
					opsIDs.clear();
					return null;
				});
	}

	/**
	 * The description of OPS is converted from string to OpsDescription and handed
	 * over to method for registering the module
	 * 
	 * @param receivePacket response to broadcast
	 * @param moduleInsert  rdf description of module for registering it to OPS
	 */
	public void broadcastResponse(DatagramPacket receivePacket, ModuleRegistry moduleRegistry) {

		String ip = receivePacket.getAddress().getHostAddress();
		String opsDescriptionAsString = new String(receivePacket.getData()).trim();

		logger.info("OPS-IP: " + ip);
		logger.info("OPS-Description " + opsDescriptionAsString);

		// Create OPS-Description with IP!
		OpsDescription opsDescription = gson.fromJson(opsDescriptionAsString, OpsDescription.class);

		boolean sameOps = false;
		for (String opsID : opsIDs) {
			if (opsDescription.getId().equals(opsID)) {
				sameOps = true;
			}
		}

		if (sameOps) {
			logger.info("Response from same OPS in a different broadcast range");
		} else {
			opsIDs.add(opsDescription.getId());

			int port = opsDescription.getPort();
			String basePath = opsDescription.getBasePath();
			String moduleEndpoint = opsDescription.getModuleEndpoint();
			String id = opsDescription.getId();
			String capabilityEndpoint = opsDescription.getCapabilityEndpoint();
			String skillEndpoint = opsDescription.getSkillEndpoint();

			opsDescription = new OpsDescription(ip, id, port, basePath, moduleEndpoint, capabilityEndpoint,
					skillEndpoint);
			moduleRegistry.addOps(opsDescription);
		}
	}
}
