package skillRegistration;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import actionGenerator.ActionGenerator;
import opcuaSkillGenerator.OpcuaSkillGenerator;
import opcuaSkillRegistrationInterface.OPCUASkillRegistrationInterface;
import statemachine.StateMachine;

@Component(immediate=true)
public class SkillRegistration {
	
	private final Logger logger = LoggerFactory.getLogger(SkillRegistration.class);
	
	@Reference
	OpcuaSkillGenerator opcuaSkillGenerator; 
	
	@Reference
	ActionGenerator actionGenerator; 
	
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
	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	void bindOPCUASkillRegistrationInterface(OPCUASkillRegistrationInterface skillRegistration) {

		logger.info("OPC-UA-Skill found");

		StateMachine stateMachine = actionGenerator.generateAction(skillRegistration); 
		opcuaSkillGenerator.generateOpcUaSkill(skillRegistration, stateMachine);
	}

	/**
	 * This method is called to unbind a (bound) service and deletes node of
	 * referenced skill from the server
	 * 
	 * @param skillRegistration skill instance of referenced skill is passed
	 */
	void unbindOPCUASkillRegistrationInterface(OPCUASkillRegistrationInterface skillRegistration) {
		
		opcuaSkillGenerator.deleteOpcUaSkill(skillRegistration);
	}
}
