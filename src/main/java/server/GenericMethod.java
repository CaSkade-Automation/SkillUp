package server;

import org.eclipse.milo.opcua.sdk.server.api.methods.AbstractMethodInvocationHandler;
import org.eclipse.milo.opcua.sdk.server.nodes.UaMethodNode;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.structured.Argument;

import methodRegistration.MethodRegistration;

public class GenericMethod extends AbstractMethodInvocationHandler {
	
	MethodRegistration method; 

	public GenericMethod(UaMethodNode node, MethodRegistration method) {
		super(node);
		this.method = method; 
		// TODO Auto-generated constructor stub
	}

	@Override
	public Argument[] getInputArguments() {
		// TODO Auto-generated method stub
		return this.method.getInputArguments();
	}

	@Override
	public Argument[] getOutputArguments() {
		// TODO Auto-generated method stub
		return this.method.getOutputArguments();
	}

	@Override
	protected Variant[] invoke(InvocationContext invocationContext, Variant[] inputValues) throws UaException {
		// TODO Auto-generated method stub
		return this.method.invoke(invocationContext, inputValues);
	}

}
