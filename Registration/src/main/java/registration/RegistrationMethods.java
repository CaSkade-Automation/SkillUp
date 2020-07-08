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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RegistrationMethods {

	private Logger logger = LoggerFactory.getLogger(RegistrationMethods.class);

	public abstract void register(String requestBody, Object object, ModuleRegistry moduleRegistry);

	public abstract void delete(Object object, ModuleRegistry moduleRegistry);

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
		logger.info("URL: " + completeUrl);

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
}