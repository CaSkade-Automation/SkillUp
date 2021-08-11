package opcUaServer;

import java.lang.reflect.Field;
import java.util.List;

import org.eclipse.milo.opcua.sdk.server.api.methods.AbstractMethodInvocationHandler;
import org.eclipse.milo.opcua.sdk.server.nodes.UaMethodNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.structured.Argument;

import skillup.annotations.Helper;
import skillup.annotations.SkillOutput;
import statemachine.IStateChangeObserver;
import statemachine.Isa88StateMachine;
import states.IState;
import states.TransitionName;

/**
 * Class is used to reflect the skill method (like start, stop...) to be added
 * to the server <br>
 * Furthermore this class observes state of stateMachine
 */
public class GenericMethod extends AbstractMethodInvocationHandler implements IStateChangeObserver {

	private Isa88StateMachine stateMachine;
	private TransitionName transition;
	private List<UaVariableNode> outputNodes;
	private Object skill;
	private Helper helper = new Helper();

	/**
	 * Constructor of class {@link GenericMethod}
	 * 
	 * @param node         method node which should be added to server
	 * @param stateMachine skills stateMachine
	 * @param transition   method like (start etc.)
	 * @param outputNodes  skill outputs
	 * @param skill        skills object to which method belongs
	 */
	public GenericMethod(UaMethodNode node, Isa88StateMachine stateMachine, TransitionName transition,
			List<UaVariableNode> outputNodes, Object skill) {
		super(node);
		this.stateMachine = stateMachine;
		this.transition = transition;
		this.outputNodes = outputNodes;
		this.skill = skill;
	}

	@Override
	public Argument[] getInputArguments() {
		// important!! new Argument[0], because AbstractMethodInvocationHandler checks
		// in line 63 if length of inputValues == length of Argument
		// and by returning null it occurs an error. Then
		// AbstractMethodInvocationHandler sets inputValues to new Variant[0] if
		// inputValues are null
		return new Argument[0];
	}

	@Override
	public Argument[] getOutputArguments() {
		return new Argument[0];
	}

	/**
	 * When this method is invoked the corresponding transition of stateMachine is
	 * invoked (e.g. OpcUa method start is invoked then transition start of skills
	 * stateMachine is invoked)
	 */
	@Override
	protected Variant[] invoke(InvocationContext invocationContext, Variant[] inputs) throws UaException {

		stateMachine.invokeTransition(transition);
		return null;
	}

	/**
	 * When state of stateMachine changes, the values of output nodes are updated
	 */
	@Override
	public void onStateChanged(IState newState) {

		List<Field> outputFields = helper.getVariables(skill, false);

		for (Field field : outputFields) {
			field.setAccessible(true);

			UaVariableNode outputNode = outputNodes.stream().filter(
					output -> output.getBrowseName().getName().equals(field.getAnnotation(SkillOutput.class).name())
							|| output.getBrowseName().getName().equals(field.getName()))
					.findFirst().get();
			try {
				outputNode.setValue(new DataValue(new Variant(field.get(skill))));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
}
