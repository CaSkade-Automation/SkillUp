package registration;

import java.util.ArrayList;
import java.util.List;

/**
 * OPS class with every necessary property
 */
public class OpsDescription {

	private String ip;
	private String id;
	private int port;
	private String basePath;
	private String moduleEndpoint;
	private String capabilityEndpoint;
	private String skillEndpoint;
	// list of registered modules/skills
	private List<Object> modules = new ArrayList<Object>();
	private List<Object> skills = new ArrayList<Object>();

	public OpsDescription() {
	}

	public OpsDescription(String ip, String id, int port, String basePath, String moduleEndpoint,
			String capabilityEndpoint, String skillEndpoint) {
		this.ip = ip;
		this.id = id;
		this.port = port;
		this.basePath = basePath;
		this.moduleEndpoint = moduleEndpoint;
		this.capabilityEndpoint = capabilityEndpoint;
		this.skillEndpoint = skillEndpoint;
	}

	public OpsDescription(String id, int port, String basePath, String moduleEndpoint, String capabilityEndpoint,
			String skillEndpoint) {
		this.id = id;
		this.port = port;
		this.basePath = basePath;
		this.moduleEndpoint = moduleEndpoint;
		this.capabilityEndpoint = capabilityEndpoint;
		this.skillEndpoint = skillEndpoint;
	}

	public int getPort() {
		return this.port;
	}

	public String getModuleEndpoint() {
		return this.moduleEndpoint;
	}

	public String getBasePath() {
		return this.basePath;
	}

	public String getIp() {
		return this.ip;
	}

	public String getId() {
		return this.id;
	}

	public String getCapabilityEndpoint() {
		return this.capabilityEndpoint;
	}

	public String getSkillEndpoint() {
		return this.skillEndpoint;
	}

	public List<Object> getModules() {
		return this.modules;
	}

	public void addModule(Object module) {
		this.modules.add(module);
	}

	public void deleteModule(Object module) {
		this.modules.remove(module);
	}

	public List<Object> getSkills() {
		return this.skills;
	}

	public void addSkill(Object skill) {
		this.skills.add(skill);
	}

	public void deleteSkill(Object skill) {
		this.skills.remove(skill);
	}

}
