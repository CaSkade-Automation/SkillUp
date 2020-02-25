package methodRegistration;

import org.eclipse.milo.opcua.sdk.server.api.methods.AbstractMethodInvocationHandler.InvocationContext;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;

public interface MethodRegistration {
	
	//public String getMethodName(); 
	//public String getMethodDescription(); 
	// oder public String getMethodDescription(); (enthält MethodeName und description)
	//public String getInputArguments();
	//public String getOutputArguments();
	public Variant[] invoke(InvocationContext context, Variant[] inputValues) throws UaException;

}
