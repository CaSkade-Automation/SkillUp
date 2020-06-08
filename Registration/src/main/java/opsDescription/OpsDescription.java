package opsDescription;

public class OpsDescription {

	private String ip;
	private String id;
	private int port;
	private String moduleEndpoint;
	private String capabilityEndpoint;

	public OpsDescription() {

	}

	public OpsDescription(String ip, String id, int port, String moduleEndpoint, String capabilityEndpoint) {
		this.ip = ip;
		this.id = id;
		this.port = port;
		this.moduleEndpoint = moduleEndpoint;
		this.capabilityEndpoint = capabilityEndpoint;
	}

	public OpsDescription(String id, int port, String moduleEndpoint, String capabilityEndpoint) {
		this.id = id;
		this.port = port;
		this.moduleEndpoint = moduleEndpoint;
		this.capabilityEndpoint = capabilityEndpoint;
	}

	public int getPort() {
		return port;
	}

	public String getModuleEndpoint() {
		return moduleEndpoint;
	}

	public String getIp() {
		return ip;
	}

	public String getId() {
		return id;
	}

	public String getCapabilityEndpoint() {
		return capabilityEndpoint;
	}
}
