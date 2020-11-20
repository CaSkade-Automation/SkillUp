package opcuaSkillGenerator;

import java.util.Enumeration;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;

import opcUaServer.Server;
import skillGeneratorInterface.SkillGeneratorInterface;
import statemachine.Isa88StateMachine;

/**
 * Skill Generator for OpcUa technology
 * 
 * @Component Indicates that annotated class is intended to be an OSGi
 *            component. <br>
 *            immediate=true, component configuration activates immediately <br>
 *            property type to differentiate the skill generators for different
 *            technologies (have to be a class that extends from
 *            actionGenerator.SkillType)
 */
@Component(immediate = true, property = "type:String=OpcUaSkillType")
public class OpcUaSkillGenerator implements SkillGeneratorInterface {

	private OpcUaSkillDescriptionGenerator opcUaSkillDescriptionGenerator = new OpcUaSkillDescriptionGenerator();
	private OpcUaMethodGenerator opcUaSkillGenerator = new OpcUaMethodGenerator();
	private Server opcUaServer;

	/**
	 * @Reference dependency to component OpcUa-Server. Necessary to generate
	 *            OpcUa-Skills
	 */
	@Reference(policy = ReferencePolicy.DYNAMIC)
	void addServer(Server opcUaServer) {
		this.opcUaServer = opcUaServer;
	};

	/**
	 * Whenn OpcUa-Server is deactivated the server description is deleted
	 * 
	 * @param opcUaServer
	 */
	void removeServer(Server opcUaServer) {
		opcUaSkillDescriptionGenerator.setServerDescriptionBoolBack();
	}

	public String generateDescription(Object skill, Isa88StateMachine stateMachine, Enumeration<String> userFiles) {
		String description = opcUaSkillDescriptionGenerator.generateOpcUaDescription(opcUaServer, skill, stateMachine,
				userFiles);
		return description;
	}

	public void generateSkill(Object skill, Isa88StateMachine stateMachine) {
		opcUaSkillGenerator.generateSkill(skill, stateMachine, opcUaServer);
	}

	public void deleteSkill(Object skill) {
		opcUaSkillGenerator.deleteSkill(skill, opcUaServer);
	}
}
