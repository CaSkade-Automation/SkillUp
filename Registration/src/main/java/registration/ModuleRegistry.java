package registration;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import skillup.annotations.Module;

/**
 * Class manages OPS list
 */
public class ModuleRegistry {

	private List<OpsDescription> opsDescriptionList = new ArrayList<OpsDescription>();
	private Logger logger = LoggerFactory.getLogger(ModuleRegistry.class);

	/**
	 * Method to get list of all OPS to which module is already registered
	 * 
	 * @param moduleIri module to which skill is connected
	 * @return list of OPS to which skill has to be registered
	 */
	public List<OpsDescription> skillRegisterOpsList(String moduleIri) {
		List<OpsDescription> opsDescriptionSkillList = new ArrayList<OpsDescription>();
		for (OpsDescription opsDescription : opsDescriptionList) {
			for (Object module : opsDescription.getModules()) {
				if (module.getClass().getAnnotation(Module.class).moduleIri().equals(moduleIri)) {
					opsDescriptionSkillList.add(opsDescription);
					break;
				}
			}
		}
		return opsDescriptionSkillList;
	}

	/**
	 * Checks if module to which skill is associated is already registered
	 * 
	 * @param moduleIri module which should be already registered
	 * @return boolean value, true if module already registered otherwise false
	 */
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
	 * Method to add new OPS to OPS-list (when broadcast successful)
	 * 
	 * @param opsDescription description of OPS
	 */
	public void addOps(OpsDescription opsDescription) {
		opsDescriptionList.add(opsDescription);
		opsListMessage();
	}

	/**
	 * Method to remove OPS from OPS-list (when OPS no longer available)
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
}
