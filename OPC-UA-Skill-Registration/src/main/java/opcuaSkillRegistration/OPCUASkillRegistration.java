package opcuaSkillRegistration;

import org.eclipse.milo.opcua.sdk.server.api.methods.AbstractMethodInvocationHandler.InvocationContext;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;

public interface OPCUASkillRegistration {
	
	public Variant[] invoke(InvocationContext context, Variant[] inputValues) throws UaException;

}
