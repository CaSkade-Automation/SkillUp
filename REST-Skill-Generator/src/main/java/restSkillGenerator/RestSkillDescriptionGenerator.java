package restSkillGenerator;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.Inet4Address;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import descriptionGenerator.SkillDescriptionGenerator;
import restResource.RestResource;
import skillup.annotations.Helper;
import skillup.annotations.Skill;
import skillup.annotations.SkillOutput;
import skillup.annotations.SkillParameter;
import statemachine.Isa88StateMachine;
import states.TransitionName;

public class RestSkillDescriptionGenerator extends SkillDescriptionGenerator {

	private Helper helper = new Helper();

	// find our local ip address(es) for generating url in the descriptions
	public ArrayList<String> getIpAddress() {
		Enumeration<NetworkInterface> n = null;
		try {
			n = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			e.printStackTrace();
		}

		ArrayList<String> addresses = new ArrayList<String>();

		for (; n.hasMoreElements();) {
			NetworkInterface e = n.nextElement();
			try {
				if (e.isVirtual() || e.isLoopback() || !e.isUp()) {
					continue;
				}
			} catch (SocketException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			List<InterfaceAddress> i = e.getInterfaceAddresses();
			for (InterfaceAddress intAddr : i) {
				if (intAddr.getAddress() instanceof Inet4Address) {
					addresses.add(intAddr.getAddress().toString().replace("/", ""));
				}
			}
		}
		return addresses;
	}

	public String generateRestDescription(RestResource restResource, Object skill, Isa88StateMachine stateMachine,
			Enumeration<String> userFiles) {

		Skill skillAnnotation = skill.getClass().getAnnotation(Skill.class);

		String stateMachineDescription = generateStateMachineDescription();

		String userSnippet = getUserSnippets(userFiles, skill.getClass().getClassLoader());

		StringBuilder restSkillDescription = new StringBuilder();
		restSkillDescription.append("\n");

		restSkillDescription.append("<${SkillIri}> a Cap:RestSkill ;\n");

		String encodedIri = null;
		try {
			encodedIri = URLEncoder.encode(skillAnnotation.skillIri(), StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ArrayList<String> ipAddresses = getIpAddress();
		for (String ipAddress : ipAddresses) {
			restSkillDescription
					.append("	WADL:hasBase \"http://" + ipAddress + ":8181/skills/" + encodedIri + "/\" ;\n");
		}
		restSkillDescription.append("	Cap:hasStateMachine <${SkillIri}_StateMachine> ;\n");
		restSkillDescription.append("	Cap:hasCurrentState <${SkillIri}_StateMachine_${InitialState}> .\n");

		restSkillDescription.append("<${CapabilityIri}> Cap:isExecutableViaRestSkill <${SkillIri}> .\n");
		restSkillDescription.append("<${ModuleIri}> Cap:providesRestSkill <${SkillIri}> .\n");

		restSkillDescription.append("<${SkillIri}_Representation> a WADL:Representation ;\n");
		restSkillDescription.append("	WADL:hasMediaType \"${MediaType}\" .\n");
		restSkillDescription.append("<${SkillIri}_Request> a WADL:Request ;\n");
		restSkillDescription.append("	WADL:hasRepresentation <${SkillIri}_Representation> .\n");
		restSkillDescription.append("<${SkillIri}_Response> a WADL:201 ;\n");
		restSkillDescription.append("	WADL:hasRepresentation <${SkillIri}_Representation> .\n");

		for (TransitionName transition : TransitionName.values()) {
			String transitionCapitalized = transition.toString().substring(0, 1).toUpperCase()
					+ transition.toString().substring(1);

			restSkillDescription.append("<${SkillIri}_" + transitionCapitalized + "Resource> a WADL:Resource ;\n");
			restSkillDescription.append("	WADL:hasPath \"" + transition.toString() + "\" .\n");
			restSkillDescription
					.append("<${SkillIri}> WADL:hasResource <${SkillIri}_" + transitionCapitalized + "Resource> .\n");
			restSkillDescription.append("<${SkillIri}_" + transitionCapitalized + "Method> a WADL:POST .\n");
			restSkillDescription.append("<${SkillIri}_" + transitionCapitalized
					+ "Resource> WADL:hasMethod <${SkillIri}_" + transitionCapitalized + "Method> .\n");
			restSkillDescription.append(
					"<${SkillIri}_" + transitionCapitalized + "Method> WADL:hasRequest <${SkillIri}_Request> .\n");
			restSkillDescription.append("<${SkillIri}_" + transitionCapitalized
					+ "Method> Cap:invokes <${SkillIri}_StateMachine_" + transitionCapitalized + "_Command> .\n");
		}
		// Resource for SkillParameter Queries
		restSkillDescription.append("<${SkillIri}_SkillParameter_Resource> a WADL:Resource ;\n");
		restSkillDescription.append("	WADL:hasPath \"skillParameters\" .\n");
		restSkillDescription.append("<${SkillIri}> WADL:hasResource <${SkillIri}_SkillParameter_Resource> .\n");
		restSkillDescription.append("<${SkillIri}_setSkillParameter_Method> a WADL:POST ;\n");
		restSkillDescription.append("	a Cap:SetParameters .\n");
		restSkillDescription.append("<${SkillIri}_getSkillParameter_Method> a WADL:GET .\n");
		restSkillDescription.append(
				"<${SkillIri}_SkillParameter_Resource> WADL:hasMethod <${SkillIri}_setSkillParameter_Method> .\n");
		restSkillDescription.append(
				"<${SkillIri}_SkillParameter_Resource> WADL:hasMethod <${SkillIri}_getSkillParameter_Method> .\n");
		restSkillDescription.append("<${SkillIri}_setSkillParameter_Method> WADL:hasRequest <${SkillIri}_Request> .\n");
		restSkillDescription
				.append("<${SkillIri}_getSkillParameter_Method> WADL:hasResponse <${SkillIri}Response> .\n");

		// Resource for SkillOutput Queries
		restSkillDescription.append("<${SkillIri}_SkillOutput_Resource> a WADL:Resource ;\n");
		restSkillDescription.append("	WADL:hasPath \"skillOutputs\" .\n");
		restSkillDescription.append("<${SkillIri}> WADL:hasResource <${SkillIri}_SkillOutput_Resource> .\n");
		restSkillDescription.append("<${SkillIri}_getSkillOutput_Method> a WADL:GET ;\n");
		restSkillDescription.append("	a Cap:GetOutputs .\n");
		restSkillDescription
				.append("<${SkillIri}_SkillOutput_Resource> WADL:hasMethod <${SkillIri}_getSkillOutput_Method> .\n");
		restSkillDescription.append("<${SkillIri}_getSkillOutput_Method> WADL:hasResponse <${SkillIri}_Response> .\n");

		// connect SkillParameters and SkillOutputs!
		int skillParamCounter = 0;
		int skillOutputCounter = 0;

		// fields are SkillParameter
		List<Field> paramFields = helper.getVariables(skill, true);

		for (Field field : paramFields) {

			field.setAccessible(true);
			skillParamCounter++;
			restSkillDescription.append("<${SkillIri}_Param" + skillParamCounter + "> a Cap:SkillParameter ;\n");
			restSkillDescription.append("	a WADL:QueryParameter ;\n");
			restSkillDescription.append("	Cap:hasVariableName \"" + field.getName() + "\" ;\n");
			restSkillDescription.append("	Cap:hasVariableType xsd:" + field.getType().getSimpleName() + " ;\n");
			restSkillDescription.append("	Cap:isRequired "
					+ Boolean.toString(field.getAnnotation(SkillParameter.class).isRequired()) + " ;\n");
			restSkillDescription.append(
					"	Cap:hasDescription \"" + field.getAnnotation(SkillParameter.class).description() + "\" .\n");
			try {
				String defaultFieldValue = field.get(skill).toString();
				restSkillDescription.append("<${SkillIri}_Param" + skillParamCounter + "> Cap:hasDefaultValue "
						+ defaultFieldValue + " .\n");
			} catch (IllegalArgumentException | IllegalAccessException | NullPointerException e) {
				e.printStackTrace();
			}

			// create connections to RestSkill and Representation
			restSkillDescription
					.append("<${SkillIri}> Cap:hasSkillParameter <${SkillIri}_Param" + skillParamCounter + "> .\n");
			restSkillDescription.append(
					"<${SkillIri}_Representation> WADL:hasParameter <${SkillIri}_Param" + skillParamCounter + "> .\n");

			// create ParameterOptions
			String options[] = field.getAnnotation(SkillParameter.class).option();
			int skillOptionCounter = 0;
			for (String option : options) {
				if (!option.isEmpty()) {
					skillOptionCounter++;
//					Object newOption = convertOption(option, field.getType());
					restSkillDescription.append("<${SkillIri}_Param" + skillParamCounter + "_Option"
							+ skillOptionCounter + "> a WADL:Option ;\n");
					restSkillDescription.append("	WADL:hasOptionValue " + option + " .\n");
					restSkillDescription.append(
							"<${SkillIri}_Param" + skillParamCounter + "> WADL:hasParameterOption <${SkillIri}_Param"
									+ skillParamCounter + "_Option" + skillOptionCounter + "> .\n");
				}
			}
		}

		// fields are SkillOutput
		List<Field> outputFields = helper.getVariables(skill, false);

		for (Field field : outputFields) {

			field.setAccessible(true);
			skillOutputCounter++;
			restSkillDescription.append("<${SkillIri}_Output" + skillOutputCounter + "> a Cap:SkillOutput ;\n");
			restSkillDescription.append("	a WADL:QueryParameter ;\n");
			restSkillDescription.append("	Cap:hasVariableName \"" + field.getName() + "\" ;\n");
			restSkillDescription.append("	Cap:hasVariableType xsd:" + field.getType().getSimpleName() + " ;\n");
			restSkillDescription.append("	Cap:isRequired "
					+ Boolean.toString(field.getAnnotation(SkillOutput.class).isRequired()) + " ;\n");
			restSkillDescription.append(
					"	Cap:hasDescription \"" + field.getAnnotation(SkillOutput.class).description() + "\" .\n");

			// create connections to RestSkill and Representation
			restSkillDescription
					.append("<${SkillIri}> Cap:hasSkillOutput <${SkillIri}_Output" + skillOutputCounter + "> .\n");
			restSkillDescription.append("<${SkillIri}_Representation> WADL:hasParameter <${SkillIri}_Output"
					+ skillOutputCounter + "> .\n");
		}

		String completeDescription;

		// put together
		completeDescription = getFileFromResources(null, "prefix.ttl") + restSkillDescription.toString()
				+ stateMachineDescription + userSnippet;

		// replace markers
		completeDescription = completeDescription.replace("${ModuleIri}", skillAnnotation.moduleIri())
				.replace("${CapabilityIri}", skillAnnotation.capabilityIri())
				.replace("${SkillIri}", skillAnnotation.skillIri())
				// Caution!: We have to cut the "state" from e.g. "IdleState" at the end of each
				// State class name
				.replace("${InitialState}",
						stateMachine.getState().getClass().getSimpleName().substring(0,
								stateMachine.getState().getClass().getSimpleName().length() - 5))
				.replace("${MediaType}", "application/json");

		try {
			// For Debugging...
			createFile(completeDescription, "restDescription.ttl");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return completeDescription;
	}
}
