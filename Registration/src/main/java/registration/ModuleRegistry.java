package registration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import annotations.Module;

public class ModuleRegistry {

	private List<OpsDescription> opsDescriptionList = new ArrayList<OpsDescription>();
	private Map<String, ArrayList<String>> opsAndSkillList = new HashMap<String, ArrayList<String>>();
	private Logger logger = LoggerFactory.getLogger(ModuleRegistry.class);

	public List<OpsDescription> skillRegisterOpsList(String moduleIri) {
		List<OpsDescription> opsDescriptionSkillList = new ArrayList<OpsDescription>();
		for (OpsDescription opsDescription : opsDescriptionList) {
			for (Object module : opsDescription.getModules()) {
				if (module.getClass().getAnnotation(Module.class).moduleIri().equals(moduleIri)) {
					opsDescriptionSkillList.add(opsDescription);
				}
			}
		}
		return opsDescriptionSkillList;
	}

	public boolean skillNeedsModule(String moduleIri) {
		for (OpsDescription opsDescription : opsDescriptionList) {
			for (Object module : opsDescription.getModules()) {
				if (module.getClass().getAnnotation(Module.class).moduleIri().equals(moduleIri)) {
					return true;
				}
			}
		}
		return false;
	}

	public List<OpsDescription> getOpsDescriptionList() {
		return opsDescriptionList;
	}

	public Map<String, ArrayList<String>> getOpsAndSkillList() {
		return opsAndSkillList;
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
	 * Method to add new OPS (when module is successfully registered to it) to
	 * OPS-list
	 * 
	 * @param opsDescription description of OPS
	 */
	public void addOps(OpsDescription opsDescription) {
		opsDescriptionList.add(opsDescription);
		opsListMessage();
		ArrayList<String> skills = new ArrayList<String>();
		opsAndSkillList.put(opsDescription.getId(), skills);
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

	public void addModule(OpsDescription opsDescription, Object module) {
		opsDescription.addModule(module);
	}

	public void deleteModule(OpsDescription opsDescription, Object module) {
		opsDescription.deleteModule(module);
	}

	public void addSkill(OpsDescription opsDescription, String skillIri) {
		ArrayList<String> skills = opsAndSkillList.get(opsDescription.getId());
		skills.add(skillIri);
		getOpsAndSkillList().put(opsDescription.getId(), skills);
		opsSkillListMessage();
	}
	
	public void deleteSkill(OpsDescription opsDescription, String skillIri) {
		ArrayList<String> skills = opsAndSkillList.get(opsDescription.getId());
		skills.remove(skillIri);
		getOpsAndSkillList().put(opsDescription.getId(), skills);
		opsSkillListMessage();
	}
}
