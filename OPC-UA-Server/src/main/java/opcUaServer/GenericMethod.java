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
		// TODO Auto-generated method stub
		// important!! new Argument[0], because AbstractMethodInvocationHandler checks
		// in line 63 if length of inputValues == length of Argument
		// and by returning null it occurs an error. Then
		// AbstractMethodInvocationHandler sets inputValues to new Variant[0] if
		// inputValues are null
		return new Argument[0];
	}

	@Override
	public Argument[] getOutputArguments() {
		// TODO Auto-generated method stub
		return new Argument[0];
	}

	/**
	 * When this method is invoked the corresponding transition of stateMachine is
	 * invoked (e.g. OpcUa method start is invoked then transition start of skills
	 * stateMachine is invoked)
	 */
	@Override
	protected Variant[] invoke(InvocationContext invocationContext, Variant[] inputs) throws UaException {
		// TODO Auto-generated method stub

		stateMachine.invokeTransition(transition);
		return null;
	}

	/**
	 * When state of stateMachine changes, the values of output nodes are updated
	 */
	@Override
	public void onStateChanged(IState newState) {
		// TODO Auto-generated method stub

		Field[] fields = skill.getClass().getDeclaredFields();
		for (Field field : fields) {
			if (field.isAnnotationPresent(SkillOutput.class)) {

				field.setAccessible(true);
				for (UaVariableNode outputNode : outputNodes) {
					if ((outputNode.getBrowseName().getName().equals(field.getAnnotation(SkillOutput.class).name()))
							|| (outputNode.getBrowseName().getName().equals(field.getName()))) {
						try {
							outputNode.setValue(new DataValue(new Variant(field.get(skill))));
						} catch (IllegalArgumentException | IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
}
