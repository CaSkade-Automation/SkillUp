package server;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.structured.Argument;

import annotations.InputArgument;
import annotations.OutputArgument;
import opcuaSkillRegistration.OPCUASkillRegistration;

public class AnnotationEvaluation {

	private UInteger[] arrayDimensions = null;

	private List<Argument> newInputArguments = new ArrayList<>();
	private List<Argument> newOutputArguments = new ArrayList<>();

	Method invokeMethod = null;

	public Map<String, Argument[]> evaluateAnnotation(OPCUASkillRegistration skillRegistration) {

		Method methods[] = skillRegistration.getClass().getMethods();

		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getName().equals("invoke")) {
				invokeMethod = methods[i];
			}
		}

		InputArgument[] arguments = invokeMethod.getAnnotationsByType(InputArgument.class);
		for (InputArgument argument : arguments) {

			if (argument.arrayDimensions().isBlank()) {
				arrayDimensions = null;
			}

			Argument newArgument = new Argument(argument.name(), new NodeId(0, argument.dataType().getKey()),
					argument.valueRank().getValue(), arrayDimensions, new LocalizedText(argument.description()));
			newInputArguments.add(newArgument);
		}

		final Argument[] inputArguments = newInputArguments.toArray(new Argument[newInputArguments.size()]);

		OutputArgument[] arguments2 = invokeMethod.getAnnotationsByType(OutputArgument.class);
		for (OutputArgument argument : arguments2) {

			if (argument.arrayDimensions().isBlank()) {
				arrayDimensions = null;
			}

			Argument newArgument = new Argument(argument.name(), new NodeId(0, argument.dataType().getKey()),
					argument.valueRank().getValue(), arrayDimensions, new LocalizedText(argument.description()));
			newOutputArguments.add(newArgument);
		}

		final Argument[] outputArguments = newOutputArguments.toArray(new Argument[newOutputArguments.size()]);

		Map<String, Argument[]> argumentsMap = new HashMap<>();
		argumentsMap.put("inputArguments", inputArguments);
		argumentsMap.put("outputArguments", outputArguments);

		return argumentsMap;
	}
}