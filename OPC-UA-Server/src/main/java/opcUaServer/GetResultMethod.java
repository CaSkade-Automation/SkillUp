package opcUaServer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.milo.opcua.sdk.core.ValueRanks;
import org.eclipse.milo.opcua.sdk.server.api.methods.AbstractMethodInvocationHandler;
import org.eclipse.milo.opcua.sdk.server.nodes.UaMethodNode;
import org.eclipse.milo.opcua.stack.core.BuiltinDataType;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.structured.Argument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import annotations.SkillOutput;

public class GetResultMethod extends AbstractMethodInvocationHandler {

	private final Logger logger = LoggerFactory.getLogger(GetResultMethod.class);
	private Object skill;

	public GetResultMethod(UaMethodNode node, Object skill) {
		super(node);
		// TODO Auto-generated constructor stub
		this.skill = skill;
	}

	@Override
	public Argument[] getInputArguments() {
		// TODO Auto-generated method stub
		return new Argument[0];
	}

	@Override
	public Argument[] getOutputArguments() {
		// TODO Auto-generated method stub
		List<Argument> outputArgumentsList = createOutputArgument();
		final Argument[] outputArguments = outputArgumentsList.toArray(new Argument[outputArgumentsList.size()]);
		return outputArguments;
	}

	public List<Argument> createOutputArgument() {
		List<Argument> outputArgumentsList = new ArrayList<Argument>();
		Field[] fields = skill.getClass().getDeclaredFields();
		for (Field field : fields) {
			if (field.isAnnotationPresent(SkillOutput.class)) {

				field.setAccessible(true);
				Class<?> type = field.getType();
				NodeId typeId = new NodeId(0, BuiltinDataType.getBuiltinTypeId(type));
				Argument outputArgument = new Argument(field.getName(), typeId, ValueRanks.Scalar, null,
						new LocalizedText(field.getName()));

				outputArgumentsList.add(outputArgument);
			}
		}
		return outputArgumentsList;
	}

	@Override
	protected Variant[] invoke(InvocationContext invocationContext, Variant[] inputValues) throws UaException {
		// TODO Auto-generated method stub
		Field[] fields = skill.getClass().getDeclaredFields();
		List<Variant> variants = new ArrayList<Variant>();

		for (Field field : fields) {
			if (field.isAnnotationPresent(SkillOutput.class)) {

				field.setAccessible(true);
				try {
					logger.info("Output: " + field.get(skill));
					Variant variant = new Variant(field.get(skill));
					variants.add(variant);
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return variants.toArray(new Variant[variants.size()]);
	}

}
