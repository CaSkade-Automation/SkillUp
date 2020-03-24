package skillRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import actionGenerator.ActionGenerator;
import annotations.Skill;
import annotations.Skills;
import skillGeneratorInterface.SkillGeneratorInterface;
import statemachine.StateMachine;

@Component(immediate = true)
public class SkillRegistration {

	private final Logger logger = LoggerFactory.getLogger(SkillRegistration.class);
	private SkillGeneratorInterface opcuaSkillGenerator;
	private SkillGeneratorInterface webserviceSkillGenerator;
	private Map<String, List<Object>> skillMap = new HashMap<>();
	private List<Object> opcuaSkillList = new ArrayList<>();
	private List<Object> webserviceSkillList = new ArrayList<>();

	@Reference
	ActionGenerator actionGenerator;

	@Reference(cardinality = ReferenceCardinality.MULTIPLE)
	void bindSkillGeneratorInterface(SkillGeneratorInterface skillGenerator) {
		if (skillGenerator.getClass().getSimpleName().equals("OpcuaSkillGenerator")) {
			opcuaSkillGenerator = skillGenerator;
		} else if (skillGenerator.getClass().getSimpleName().equals("WebserviceGenerator")) {
			webserviceSkillGenerator = skillGenerator;
		}
	}

	@Reference
	SkillGeneratorInterface skillGenerator;

	/**
	 * This method is called to bind a new service to the component
	 * 
	 * @Reference used to specify dependency on other services, here:
	 *            MethodRegistration <br>
	 *            cardinality=MULTIPLE (0...n), reference is optional and multiple
	 *            bound services are supported <br>
	 *            policy=DYNAMIC, SCR(Service Component Runtime) can change the set
	 *            of bound services without deactivating the Component Configuration
	 *            -> method can be called while component is active and not only
	 *            before the activate method <br>
	 * @param skillRegistration Service instance of referenced skill is passed
	 */
	@Reference(target = "(component.factory=skill.factory)", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	void bindFactory(ComponentFactory factory) {

		Object skill = factory.newInstance(null).getInstance();
		Skill skillAnnotation = skill.getClass().getAnnotation(Skill.class);
		if (skillAnnotation != null) {
			StateMachine stateMachine = actionGenerator.generateAction(skill);

			if (skillAnnotation.value().equals(Skills.OPCUASkill)) {
				logger.info("OPC-UA-Skill found");

				opcuaSkillGenerator.generateSkill(skill, stateMachine);
				opcuaSkillList.add(skill);
				skillMap.put("OPCUA", opcuaSkillList);
			} else if (skillAnnotation.value().equals(Skills.WebserviceSkill)) {
				logger.info("Webservice-Skill found");

				webserviceSkillGenerator.generateSkill(skill, stateMachine);
				webserviceSkillList.add(skill);
				skillMap.put("Webservice", webserviceSkillList);
			}
		} else {
			logger.error("Skill-Annotation not set!");
		}
	}

	/**
	 * This method is called to unbind a (bound) service and deletes node of
	 * referenced skill from the server
	 * 
	 * @param skillRegistration skill instance of referenced skill is passed
	 */
	void unbindFactory(ComponentFactory factory) {

		String key = null;
		Object skill = null;

		loop: for (Map.Entry<String, List<Object>> entry : skillMap.entrySet()) {
			key = entry.getKey();
			for (Object value : entry.getValue()) {
				if (factory.toString().contains(value.getClass().getSimpleName())) {
					if (key.equals("OPCUA")) {
						logger.info("Delete OPC-UA-Skill");

						opcuaSkillGenerator.deleteSkill(value);
						skill = value;
						break loop;
					} else if (key.equals("Webservice")) {
						logger.info("Delete Webservice-Skill");

						webserviceSkillGenerator.deleteSkill(value);
						skill = value;
						break loop;
					}
				}
			}
		}
		removeFromList(key, skill);
	}

	public void removeFromList(String key, Object value) {
		if (key.equals("OPCUA")) {
			opcuaSkillList.remove(value);
			skillMap.put(key, opcuaSkillList);
		} else if (key.equals("Webservice")) {
			webserviceSkillList.remove(value);
			skillMap.put(key, webserviceSkillList);
		}
	}
}
