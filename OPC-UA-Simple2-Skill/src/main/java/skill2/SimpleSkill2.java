package skill2;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import annotations.Completing;
import annotations.Execute;
import annotations.Skill;
import annotations.Starting;

/**
 * @Component Indicates that annotated class is intended to be an OSGi
 *            component. <br>
 *            After becoming satisfied component is registered as a service
 *            under OPCUASkillRegistration
 */
@Skill("OpcUaSkill")
@Component(factory = "skill.factory")
public class SimpleSkill2 {

	private final Logger logger = LoggerFactory.getLogger(SimpleSkill2.class);
	private int i = 0;

	@Starting
	public void starting() {
		System.out.println("Starting, i = " + i);
	}

	@Execute
	public void execute() {
		i = i + 5;
	}

	@Completing
	public void completing() {
		System.out.println("Completing, i = " + i);
	}

	@Activate
	public void activate() {
		logger.info("OPC-UA-Skill zum Subtrahieren wird aktiviert.");
	}

	@Deactivate
	public void deactivate() {
		logger.info("OPC-UA-Skill zum Subtrahieren wird deaktiviert.");
	}
}
