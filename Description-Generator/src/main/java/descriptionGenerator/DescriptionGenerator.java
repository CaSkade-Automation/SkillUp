package descriptionGenerator;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import annotations.Module;

public class DescriptionGenerator {

	private Logger logger = LoggerFactory.getLogger(DescriptionGenerator.class);

	private String modulePrefixSnippet = "@prefix module: <${Namespace}/modules#${MACAddress}_${ModuleName}> .";

	private String macAddress;

	/**
	 * Method gets the file from resources folder, reads it and converts it to a
	 * string
	 * 
	 * @param fileName the name of file which we want from resources folder
	 * @return returns given file as string
	 */
	public String getFileFromResources(ClassLoader classLoader, String fileName) throws IOException {

		if (classLoader == null) {
			classLoader = DescriptionGenerator.class.getClassLoader();
		}
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

	public String getModuleIri(Module module) {
		String moduleIri = modulePrefixSnippet.substring(modulePrefixSnippet.indexOf("<") + 1,
				modulePrefixSnippet.indexOf(">"));
		moduleIri = moduleIri.replace("${MACAdress}", macAddress).replace("${ModuleName}", module.name())
				.replace("${Namespace}", module.namespace());
		return moduleIri;
	}

	public String getModulePrefixSnippet() {
		return modulePrefixSnippet;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	public String getThisMacAddress() {
		return macAddress;
	}
}
