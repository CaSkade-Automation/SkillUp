package server;

import java.lang.reflect.Field;

import org.eclipse.milo.opcua.sdk.server.api.methods.AbstractMethodInvocationHandler;
import org.eclipse.milo.opcua.sdk.server.nodes.UaMethodNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.structured.Argument;

import annotations.SkillOutput;
import statemachine.StateMachine;
import states.TransitionName;

/**
 * Class is used to reflect the skill to be added to the server <br>
 */
public class GenericMethod extends AbstractMethodInvocationHandler {

	StateMachine stateMachine;
	TransitionName transition;
	UaVariableNode outputNode;
	Object skill;

	public GenericMethod(UaMethodNode node, StateMachine stateMachine, TransitionName transition,
			UaVariableNode outputNode, Object skill) {
		super(node);
		this.stateMachine = stateMachine;
		this.transition = transition;
		this.outputNode = outputNode;
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

	@Override
	protected Variant[] invoke(InvocationContext invocationContext, Variant[] inputs) throws UaException {
		// TODO Auto-generated method stub

		stateMachine.invokeTransition(transition);
		
		//wird bald ersetzt da Listener, wenn Zustand sich ändert!
		
		Field[] fields = skill.getClass().getDeclaredFields();
		for (Field field : fields) {
			if (field.isAnnotationPresent(SkillOutput.class)) {

				field.setAccessible(true);
				try {
					outputNode.setValue(new DataValue(new Variant(field.get(skill))));
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return null;
	}
}
