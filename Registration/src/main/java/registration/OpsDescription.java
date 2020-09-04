package registration;

import java.util.ArrayList;
import java.util.List;

public class OpsDescription {

	private String ip;
	private String id;
	private int port;
	private String basePath; 
	private String moduleEndpoint;
	private String capabilityEndpoint;
	private String skillEndpoint; 
	private List<Object> modules = new ArrayList<Object>(); 

	public OpsDescription() {

	}

	public OpsDescription(String ip, String id, int port, String basePath, String moduleEndpoint, String capabilityEndpoint, String skillEndpoint) {
		this.ip = ip;
		this.id = id;
		this.port = port;
		this.basePath = basePath; 
		this.moduleEndpoint = moduleEndpoint;
		this.capabilityEndpoint = capabilityEndpoint;
		this.skillEndpoint = skillEndpoint; 
	}

	public OpsDescription(String id, int port, String basePath, String moduleEndpoint, String capabilityEndpoint, String skillEndpoint) {
		this.id = id;
		this.port = port;
		this.basePath = basePath; 
		this.moduleEndpoint = moduleEndpoint;
		this.capabilityEndpoint = capabilityEndpoint;
		this.skillEndpoint = skillEndpoint; 
	}

	public int getPort() {
		return port;
	}

	public String getModuleEndpoint() {
		return moduleEndpoint;
	}
	
	public String getBasePath() {
		return basePath; 
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
	
	public String getSkillEndpoint() {
		return skillEndpoint; 
	}
	
	public List<Object> getModules() {
		return modules; 
	}
	
	public void addModule(Object module) {
		this.modules.add(module); 
	}
	
	public void deleteModule(Object module) {
		this.modules.remove(module); 
	}
}
