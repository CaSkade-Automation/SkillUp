package skill2;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import annotations.SkillParameter;
import annotations.SkillReturn;
import annotations.Starting;
import annotations.Stopping;
import opcuaSkillRegistration.OPCUASkillRegistration;
import statemachine.StateMachine;
import statemachine.StateMachineBuilder;

/**
 * @Component Indicates that annotated class is intended to be an OSGi
 *            component. <br>
 *            After becoming satisfied component is registered as a service
 *            under OPCUASkillRegistration
 */
@Component
public class SimpleSkill2 implements OPCUASkillRegistration {

	private final Logger logger = LoggerFactory.getLogger(SimpleSkill2.class);

	@Starting
	public @SkillReturn(name = "Ergebnis", description = "Differenz") double start(
			@SkillParameter(name = "1. Zahl", description = "Minuend") double a,
			@SkillParameter(name = "2. Zahl", description = "Substrahend") double b) {

		return a - b;
	}

	@Stopping
	public String stop(String stop) {

		return stop;
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
