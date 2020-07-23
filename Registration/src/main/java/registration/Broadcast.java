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
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class Broadcast {

	private Logger logger = LoggerFactory.getLogger(Broadcast.class);
	private Gson gson = new Gson();
	private static DatagramSocket socket = null;

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

		// TODO: wait for possibly multiple responses
		socket.receive(receivePacket);
		logger.info(receivePacket.toString());
		logger.info(receivePacket.getData().toString());

		broadcastResponse(receivePacket, moduleRegistry);

		socket.close();
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

		int port = opsDescription.getPort();
		String basePath = opsDescription.getBasePath(); 
		String moduleEndpoint = opsDescription.getModuleEndpoint();
		String id = opsDescription.getId();
		String capabilityEndpoint = opsDescription.getCapabilityEndpoint();
		String skillEndpoint = opsDescription.getSkillEndpoint();

		opsDescription = new OpsDescription(ip, id, port, basePath, moduleEndpoint, capabilityEndpoint, skillEndpoint);
		moduleRegistry.addOps(opsDescription);
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
