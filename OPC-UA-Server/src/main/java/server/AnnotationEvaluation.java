package server;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.milo.opcua.sdk.core.ValueRanks;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.structured.Argument;

import annotations.SkillParameter;
import annotations.SkillReturn;
import annotations.Starting;
import annotations.States;
import annotations.Stopping;
import annotations.UaTypes;
import opcuaSkillRegistration.OPCUASkillRegistration;
import statemachine.StateMachine;
import statemachine.StateMachineBuilder;

public class AnnotationEvaluation {

	private Map<String, Map<String, Argument[]>> skillMap = new HashMap<>();

	public Map<String, Map<String, Argument[]>> evaluateAnnotation(OPCUASkillRegistration skillRegistration) {

		StateMachineBuilder stateMachineBuilder = new StateMachineBuilder();

		Method[] methods = skillRegistration.getClass().getMethods();
		for (int i = 0; i < methods.length; i++) {

//			for(States state : States.values()) {
//				Map<String, Argument[]> argumentsMap = getArguments(methods[i], state.getKey());
//				
//				if (!argumentsMap.isEmpty()) {
//					skillMap.put(methods[i].getName(), argumentsMap);
//			
//					Parameter[] parameters = methods[i].getParameters(); 
//					List<Object> parameterObjects = new ArrayList<>(); 
//					for (Parameter parameter : parameters) {
//						parameterObjects.add(parameter);
//					}
//					
//					SkillAction action = new SkillAction(methods[i], skillRegistration, parameterObjects); 
//						if(state.toString().equals("Starting")) {
//							stateMachineBuilder.withActionInStarting(action); 
//					}
//				}
//			}

			Map<String, Argument[]> argumentsMap = getArguments(methods[i], Starting.class);
			if (!argumentsMap.isEmpty()) {
				skillMap.put(methods[i].getName(), argumentsMap);

				Parameter[] parameters = methods[i].getParameters(); 
				List<Object> parameterObjects = new ArrayList<>(); 
				for (Parameter parameter : parameters) {
					parameterObjects.add(parameter);
				}
				
				SkillAction action = new SkillAction(methods[i], skillRegistration, parameterObjects);
				stateMachineBuilder.withActionInStarting(action);
			}

			Map<String, Argument[]> argumentsMap2 = getArguments(methods[i], Stopping.class);
			if (!argumentsMap2.isEmpty()) {
				skillMap.put(methods[i].getName(), argumentsMap2);

				Parameter[] parameters = methods[i].getParameters(); 
				List<Object> parameterObjects = new ArrayList<>(); 
				for (Parameter parameter : parameters) {
					parameterObjects.add(parameter);
				}
				
				SkillAction action = new SkillAction(methods[i], skillRegistration, parameterObjects);
				stateMachineBuilder.withActionInStarting(action);
			}
		}
		StateMachine stateMachine = stateMachineBuilder.build();
		return skillMap;
	}

	public Map<String, Argument[]> getArguments(Method method, Class methodAnnotation) {
		List<Argument> newInputArguments = new ArrayList<>();
		List<Argument> newOutputArguments = new ArrayList<>();
		Map<String, Argument[]> argumentsMap = new HashMap<>();

		if (method.isAnnotationPresent(methodAnnotation)) {

			Parameter[] parameter = method.getParameters();
			Annotation[][] parameterAnnotations = method.getParameterAnnotations();
			SkillReturn returnAnnotation = method.getAnnotatedReturnType().getAnnotation(SkillReturn.class);

			int j = 0;
			for (Annotation[] annotations : parameterAnnotations) {
				Class parameterType = parameter[j++].getType();
				String parameterTypeName = parameterType.getSimpleName().toString().substring(0, 1).toUpperCase()
						+ parameterType.getSimpleName().toString().substring(1);

				if (annotations.length > 0) {
					for (Annotation annotation : annotations) {
						if (annotation instanceof SkillParameter) {
							SkillParameter skillParameter = (SkillParameter) annotation;

							Argument newInputArgument = new Argument(skillParameter.name(),
									new NodeId(0, UaTypes.getIfPresent(parameterTypeName).getKey()), ValueRanks.Scalar,
									null, new LocalizedText(skillParameter.description()));
							newInputArguments.add(newInputArgument);
						}
					}
				} else {
					Argument newInputArgument = new Argument("Input " + String.valueOf(j),
							new NodeId(0, UaTypes.getIfPresent(parameterTypeName).getKey()), ValueRanks.Scalar, null,
							new LocalizedText("Input " + String.valueOf(j)));
					newInputArguments.add(newInputArgument);
				}
			}

			Class returnType = method.getReturnType();
			String returnTypeName = returnType.getSimpleName().toString().substring(0, 1).toUpperCase()
					+ returnType.getSimpleName().toString().substring(1);

			if (returnAnnotation != null) {
				Argument newOutputArgument = new Argument(returnAnnotation.name(),
						new NodeId(0, UaTypes.getIfPresent(returnTypeName).getKey()), ValueRanks.Scalar, null,
						new LocalizedText(returnAnnotation.description()));
				newOutputArguments.add(newOutputArgument);
			} else {
				Argument newOutputArgument = new Argument("Output",
						new NodeId(0, UaTypes.getIfPresent(returnTypeName).getKey()), ValueRanks.Scalar, null,
						new LocalizedText("Output"));
				newOutputArguments.add(newOutputArgument);
			}

			final Argument[] inputArguments = newInputArguments.toArray(new Argument[newInputArguments.size()]);
			final Argument[] outputArguments = newOutputArguments.toArray(new Argument[newOutputArguments.size()]);

			argumentsMap.put("inputArguments", inputArguments);
			argumentsMap.put("outputArguments", outputArguments);
		}
		return argumentsMap;
	}
}