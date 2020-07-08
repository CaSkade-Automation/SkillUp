package opcUaServer;

import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;

public class MethodDescription {

	NodeId typeId; 
	String methodName; 
	String methodDescription; 
	Variant variant; 
	
	public MethodDescription() {
		
	}
	
	public MethodDescription(NodeId typeId, String fieldName, String fieldDescription, Variant variant) {
		this.typeId = typeId; 
		this.methodName = fieldName; 
		this.methodDescription = fieldDescription; 
		this.variant = variant; 
	}
	
	public NodeId getNodeId() {
		return typeId; 
	}
	
	public String getMethodName() {
		return methodName; 
	}
	
	public String getMethodDescription() {
		return methodDescription; 
	}
	
	public Variant getVariant() {
		return variant; 
	}
}
