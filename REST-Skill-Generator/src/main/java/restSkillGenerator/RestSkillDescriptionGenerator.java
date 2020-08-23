package restSkillGenerator;

import java.lang.reflect.Field;
import java.net.Inet4Address;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import annotations.Skill;
import annotations.SkillOutput;
import annotations.SkillParameter;
import descriptionGenerator.SkillDescriptionGenerator;
import restResource.RestResource;
import statemachine.StateMachine;
import states.TransitionName;

public class RestSkillDescriptionGenerator extends SkillDescriptionGenerator {

	String restMainSkillSnippet = "";
	String restActionSkillSnippet = "";

	// find our local ip address for generating url in the descriptions
	public String getIpAddress() {
		Enumeration<NetworkInterface> n = null;
		try {
			n = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			e.printStackTrace();
		}

		LinkedList<String> addresses = new LinkedList<String>();

		for (; n.hasMoreElements();) {
			NetworkInterface e = n.nextElement();
			if (e.isVirtual()) {
				continue;
			}
			List<InterfaceAddress> i = e.getInterfaceAddresses();
			for (InterfaceAddress intAddr : i) {
				if (intAddr.getAddress() instanceof Inet4Address) {
					addresses.add(intAddr.getAddress().toString().replace("/", ""));
				}
			}
		}
		// TODO is there a better way?
		return addresses.getFirst();
	}

	public String generateRestDescription(RestResource restResource, Object skill, StateMachine stateMachine,
			Enumeration<String> userFiles) {

		Skill skillAnnotation = skill.getClass().getAnnotation(Skill.class);

		String stateMachineDescription = generateStateMachineDescription(stateMachine);

		String userSnippet = getUserSnippets(userFiles, skill.getClass().getClassLoader());

		StringBuilder restSkillDescription = new StringBuilder();
		// TODO: providesSkill and connection to module?
		restSkillDescription.append("<${SkillIri}_RestSkill> a Cap:RestSkill ;\n");
		restSkillDescription.append("	WADL:hasBase \"http://${IpAddress}:8181/skills/${UUID}/\" ;\n");
		restSkillDescription.append("	Cap:hasStateMachine <${SkillIri}_StateMachine> ;\n");
		restSkillDescription.append("	Cap:hasCurrentState <${SkillIri}_StateMachine_${InitialState}> .\n");

		restSkillDescription.append("<${SkillIri}_Representation> a WADL:Representation ;\n");
		restSkillDescription.append("	WADL:hasMediaType \"${MediaType}\" ;\n");
		restSkillDescription.append("<${SkillIri}_Request> a WADL:Request ;\n");
		restSkillDescription.append("	WADL:hasRepresentation <${SkillIri}_Representation> .\n");

		for (TransitionName transition : TransitionName.values()) {
			// TODO does this work as intended? (abort -> Abort)
			String transitionCapitalized = transition.toString().substring(0, 1).toUpperCase()
					+ transition.toString().substring(1);

			restSkillDescription.append("<${SkillIri}_" + transitionCapitalized + "Resource> a WADL:Resource ;\n");
			restSkillDescription.append("	WADL:hasPath \"" + transition.toString() + "\" .\n");
			restSkillDescription.append(
					"<${SkillIri}_RestSkill> WADL:hasResource <${SkillIri}_" + transitionCapitalized + "Resource> .\n");
			restSkillDescription.append("<${SkillIri}_" + transitionCapitalized + "Method> a WADL:POST .\n");
			restSkillDescription.append("<${SkillIri}_" + transitionCapitalized
					+ "Resource> WADL:hasMethod <${SkillIri}_" + transitionCapitalized + "Method> .\n");
			restSkillDescription.append(
					"<${SkillIri}_" + transitionCapitalized + "Method> WADL:hasRequest <${SkillIri}_Request> .\n");
			// TODO does this work correctly?
			// wires up the invokes-connection from the method to the stateMachine
			restSkillDescription.append("<${SkillIri}_" + transitionCapitalized
					+ "Method> Cap:invokes <${SkillIri}_StateMachine_" + transitionCapitalized + "_Command> .\n");
		}

		// wire up skill parameters and skill outputs!

		int skillParamCounter = 0;
		int skillOutputCounter = 0;

		Field[] fields = skill.getClass().getDeclaredFields();
		for (Field field : fields) {
			// field is a SkillParameter
			if (field.isAnnotationPresent(SkillParameter.class)) {
				field.setAccessible(true);
				skillParamCounter++;
				restSkillDescription.append("<${SkillIri}_Param" + skillParamCounter + "> a Cap:SkillParameter ;\n");
				restSkillDescription.append("	Cap:hasVariableName \"" + field.getName() + "\" ;\n");
				restSkillDescription.append("	Cap:hasVariableType \"" + field.getType().getSimpleName() + "\" ;\n");
				restSkillDescription.append("	Cap:isRequired \""
						+ Boolean.toString(field.getAnnotation(SkillParameter.class).isRequired()) + "\" ;\n");
				// TODO wie soll hasDefaultValue ausgewertet werden??
				// field.get(skill).toString()
				restSkillDescription.append("	Cap:hasDefaultValue \"" + "ERR: NOT IMPLEMENTED" + "\" .\n");

				// create connections to RestSkill and Representation
				restSkillDescription.append("<${SkillIri}_RestSkill> Cap:hasSkillParameter <${SkillIri}_Param"
						+ skillParamCounter + "> .\n");
				restSkillDescription.append("<${SkillIri}_Representation> WADL:hasParameter <${SkillIri}_Param"
						+ skillParamCounter + "> .\n");

				// create ParameterOptions
				String options[] = field.getAnnotation(SkillParameter.class).option();
				int skillOptionCounter = 0;
				for (String option : options) {
					if (!option.isEmpty()) {
						skillOptionCounter++;
						restSkillDescription.append("<${SkillIri}_Param" + skillParamCounter + "_Option"
								+ skillOptionCounter + "> a WADL:Option ;\n");
						restSkillDescription.append("	WADL:hasOptionValue \"" + option + "\" .\n");
						restSkillDescription.append("<${SkillIri}_Param" + skillParamCounter
								+ "> WADL:hasParameterOption <${SkillIri}_Param" + skillParamCounter + "_Option"
								+ skillOptionCounter + "> .\n");
					}
				}

				// field is a SkillOutput
			} else if (field.isAnnotationPresent(SkillOutput.class)) {
				field.setAccessible(true);
				skillOutputCounter++;
				restSkillDescription.append("<${SkillIri}_Output" + skillOutputCounter + "> a Cap:SkillOutput ;\n");
				restSkillDescription.append("	Cap:hasVariableName \"" + field.getName() + "\" ;\n");
				restSkillDescription.append("	Cap:hasVariableType \"" + field.getType().getSimpleName() + "\" ;\n");
				restSkillDescription.append("	Cap:isRequired \""
						+ Boolean.toString(field.getAnnotation(SkillParameter.class).isRequired()) + "\" ;\n");
				// TODO wie soll hasDefaultValue ausgewertet werden??
				// field.get(skill).toString()
				restSkillDescription.append("	Cap:hasDefaultValue \"" + "ERR: NOT IMPLEMENTED" + "\" .\n");

				// create connections to RestSkill and Representation
				restSkillDescription.append("<${SkillIri}_RestSkill> Cap:hasSkillOutput <${SkillIri}_Output"
						+ skillOutputCounter + "> .\n");
				// TODO how to connect representation with the output? what kind of object
				// property?
				restSkillDescription.append("<${SkillIri}_Representation> WADL:hasParameter <${SkillIri}_Output"
						+ skillOutputCounter + "> .\n");

				// TODO parameter options??

			}
		}

		String completeDescription;
		try {
			// put together
			completeDescription = getFileFromResources(null, "prefix.ttl") + restSkillDescription.toString()
					+ stateMachineDescription + userSnippet;

			// replace markers
			completeDescription = completeDescription.replace("${ModuleIri}", skillAnnotation.moduleIri())
					.replace("${CapabilityIri}", skillAnnotation.capabilityIri())
					.replace("${SkillIri}", skillAnnotation.skillIri())
					// TODO: does this return the correct uuid?
					.replace("${UUID}", restResource.getUuidByIri(skillAnnotation.skillIri()))
					// TODO: setting initial state to idle - is this ok?
					.replace("${InitialState}", "Idle")
					// TODO: ALWAYS setting mediaType to html - change?
					.replace("${MediaType}", "application/html")
					.replace("${IpAddress}", getIpAddress());
			
			// TODO: need to create a file?
			createFile(completeDescription, "restDescription.ttl");

			return completeDescription;

		} catch (Exception e1) {
			e1.printStackTrace();
		}

		return null;
	}

}
