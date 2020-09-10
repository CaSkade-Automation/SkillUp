package opcuaSkillGenerator;

import java.util.Enumeration;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import opcUaServer.Server;
import skillGeneratorInterface.SkillGeneratorInterface;
import statemachine.Isa88StateMachine;

@Component(immediate = true, property = "name=OpcUa")
public class OpcUaSkillGenerator implements SkillGeneratorInterface {

	private OpcUaSkillDescriptionGenerator opcUaSkillDescriptionGenerator = new OpcUaSkillDescriptionGenerator();
	private OpcUaMethodGenerator opcUaSkillGenerator = new OpcUaMethodGenerator();

	@Reference
	Server opcUaServer;

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
