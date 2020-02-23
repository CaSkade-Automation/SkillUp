package server;

import org.eclipse.milo.opcua.sdk.server.api.methods.AbstractMethodInvocationHandler;
import org.eclipse.milo.opcua.sdk.server.nodes.UaMethodNode;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.structured.Argument;
import methodRegistration.MethodRegistration;

/**
 * Class is used to reflect the method to be added to the server <br>
 */
public class GenericMethod extends AbstractMethodInvocationHandler {

	MethodRegistration method; 
	Argument[] inputArguments; 
	Argument[] outputArguments; 

	public GenericMethod(UaMethodNode node, MethodRegistration method, Argument[] inputArguments, Argument[] outputArguments) {
		super(node);
		this.method = method;
		this.inputArguments = inputArguments; 
		this.outputArguments = outputArguments; 
		// TODO Auto-generated constructor stub
	}

	@Override
	public Argument[] getInputArguments() {
		// TODO Auto-generated method stub
		return inputArguments;
	}

	@Override
	public Argument[] getOutputArguments() {
		// TODO Auto-generated method stub
		return outputArguments;
	}

	@Override
	protected Variant[] invoke(InvocationContext invocationContext, Variant[] inputValues) throws UaException {
		// TODO Auto-generated method stub
		return this.method.invoke(invocationContext, inputValues);
	}
}
