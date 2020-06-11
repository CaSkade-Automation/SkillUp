package registration;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import annotations.Module;
import opsDescription.OpsDescription;

public abstract class Registration {

	private Logger logger = LoggerFactory.getLogger(Registration.class);

	private List<OpsDescription> opsDescriptionList = new ArrayList<OpsDescription>();
	private Map<String, ArrayList<String>> opsAndSkillList = new HashMap<String, ArrayList<String>>();
	private List<Object> modules = new ArrayList<Object>();

	public abstract void register(String requestBody, Object object);

	public abstract void delete(Object object);

	public List<OpsDescription> getOpsDescriptionList() {
		return opsDescriptionList;
	}

	public Map<String, ArrayList<String>> getOpsAndSkillList() {
		return opsAndSkillList;
	}

	public List<Object> getModules() {
		return modules;
	}

	/**
	 * Method to get the whole OPS description by its IP address
	 * 
	 * @param ip IP address of OPS which is searched
	 * @return if its found: OPS description <br>
	 *         else: null
	 */
	public OpsDescription getOpsDescriptionByIp(String ip) {
		OpsDescription opsDescription = null;
		for (OpsDescription ops : opsDescriptionList) {
			if (ops.getIp().equals(ip)) {
				opsDescription = ops;
			}
		}
		return opsDescription;
	}

	/**
	 * Method to put URL string from OPS together
	 * 
	 * @param host corresponds to IP address of OPS
	 * @param port port of OPS
	 * @param path corresponds to the moduleEndpoint of OPS
	 * @return if its successful: URL of OPS <br>
	 *         else: null
	 */
	public String buildUrlString(String host, int port, String path) {

		String completeUrl = null;
		try {
			URL url = new URL("http", host, port, path);
			completeUrl = url.toString();
			return completeUrl;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return completeUrl;
	}

	/**
	 * Method to encode IRI of the module in URL
	 * 
	 * @param value IRI of module
	 * @return encoded IRI of module
	 */
	public String encodeValue(String value) {
		try {
			return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex.getCause());
		}
	}

	/**
	 * Method to add new OPS (when module is successfully registered to it) to
	 * OPS-list
	 * 
	 * @param opsDescription description of OPS
	 */
	public void addOps(OpsDescription opsDescription) {
		opsDescriptionList.add(opsDescription);
		logger.info("new OPS in OPS-List: " + opsDescription.getId());
		opsListMessage();
		ArrayList<String> skills = new ArrayList<String>();
		opsAndSkillList.put(opsDescription.getId(), skills);
	}

	/**
	 * Method to log OPS-List as message
	 */
	public void opsListMessage() {
		logger.info("new OPS-List: ");
		for (OpsDescription ops : opsDescriptionList) {
			logger.info(ops.getId());
		}
	}

	/**
	 * Method to log OPS-Skill-List as message
	 */
	public void opsSkillListMessage() {
		logger.info("New OPS-Skill-List: ");
		for (Map.Entry<String, ArrayList<String>> me : opsAndSkillList.entrySet()) {
			logger.info("OPS: " + me.getKey() + " & Skills: " + me.getValue());
		}
	}

	/**
	 * Method to remove OPS (when OPS deleted module) from OPS-list
	 * 
	 * @param remoteAddr IP address of OPS
	 * @return If OPS could be removed: true <br>
	 *         else: false
	 */
	public void removeOps(String remoteAddr) {
		OpsDescription deleteOps = getOpsDescriptionByIp(remoteAddr);

		opsDescriptionList.remove(deleteOps);
		logger.info("Removed OPS from OPS-List: " + deleteOps.getId());
		opsListMessage();
	}

	/**
	 * Method to send SPARQL query to OPS
	 * 
	 * @param opsDescription OPS description
	 * @param requestType    POST, DELETE etc.
	 * @param location       path of OPS
	 * @param requestBody    String which is send in the body of the request
	 * @return status code of the received response
	 */
	public int opsRequest(OpsDescription opsDescription, String requestType, String location, String requestBody) {

		String ip = opsDescription.getIp();
		int port = opsDescription.getPort();

		// Using new JDK 11 HttpClient -> could be moved to constructor
		HttpClient httpClient = HttpClient.newBuilder().build();

		// get the complete URL including encoded parameters
		String completeUrl = buildUrlString(ip, port, location);

		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(completeUrl))
				.headers("Accept", "application/json").headers("Content-Type", "text/plain")
				.method(requestType, BodyPublishers.ofString(requestBody)).version(Version.HTTP_1_1).build();

		HttpResponse<String> response = null;
		try {
			response = httpClient.send(request, BodyHandlers.ofString());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int responseStatusCode = response.statusCode();
		logger.info("Response status code: " + responseStatusCode);
		logger.info("Response body: " + response.body());

		return responseStatusCode;
	}

	public Object skillNeedsModule(String moduleIri) {
		for (Object module : modules) {
			if (module.getClass().getAnnotation(Module.class).moduleIri().equals(moduleIri)) {
				return module;
			}
		}
		return null;
	}
}