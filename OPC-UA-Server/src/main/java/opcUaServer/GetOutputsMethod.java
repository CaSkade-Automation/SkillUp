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

import skillup.annotations.Helper;

/**
 * Class is used to reflect the skill method getOutputs to be added to the
 * server
 */
public class GetOutputsMethod extends AbstractMethodInvocationHandler {

	private final Logger logger = LoggerFactory.getLogger(GetOutputsMethod.class);
	private Object skill;
	private Helper helper = new Helper();

	/**
	 * Constructor of class {@link GetOutputsMethod}
	 * 
	 * @param node  method node
	 * @param skill instance of skill
	 */
	public GetOutputsMethod(UaMethodNode node, Object skill) {
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

	/**
	 * Method to create an output argument for every skill output
	 * 
	 * @return list of output arguments
	 */
	public List<Argument> createOutputArgument() {
		List<Argument> outputArgumentsList = new ArrayList<Argument>();

		List<Field> outputFields = helper.getVariables(skill, false);

		for (Field field : outputFields) {
			field.setAccessible(true);
			Class<?> type = field.getType();
			NodeId typeId = new NodeId(0, BuiltinDataType.getBuiltinTypeId(type));
			Argument outputArgument = new Argument(field.getName(), typeId, ValueRanks.Scalar, null,
					new LocalizedText(field.getName()));

			outputArgumentsList.add(outputArgument);
		}
		return outputArgumentsList;
	}

	/**
	 * When this method is invoked, values of skill outputs are shown
	 */
	@Override
	protected Variant[] invoke(InvocationContext invocationContext, Variant[] inputValues) throws UaException {

		List<Field> outputFields = helper.getVariables(skill, false);
		List<Variant> variants = new ArrayList<Variant>();

		for (Field field : outputFields) {

			field.setAccessible(true);
			try {
				logger.info("Output: " + field.get(skill));
				Variant variant = new Variant(field.get(skill));
				variants.add(variant);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		logger.info("Get Outputs" + variants.toString());
		return variants.toArray(new Variant[variants.size()]);
	}
}