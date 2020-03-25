package skill;

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
@Skill
@Component(factory = "skill.factory")
public class SimpleSkill {

	private final Logger logger = LoggerFactory.getLogger(SimpleSkill.class);
	private int j = 0;

	@Starting
	public void starting() {
		System.out.println("Starting, j = " + j);
	}

	@Execute
	public void execute() {
		j = j + 9;
	}

	@Completing
	public void completing() {
		System.out.println("Completing, j = " + j);
	}


	@Activate
	public void activate() {
		logger.info("OPC-UA-Skill zum Addieren wird aktiviert.");
	}

	@Deactivate
	public void deactivate() {
		logger.info("OPC-UA-Skill zum Addieren wird deaktiviert.");
	}
}
