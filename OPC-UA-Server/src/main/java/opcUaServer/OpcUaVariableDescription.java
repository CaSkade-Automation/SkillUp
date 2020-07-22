package opcUaServer;

import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;

public class OpcUaVariableDescription {

	NodeId typeId; 
	String variableName; 
	String variableDescription; 
	Variant variant; 
	
	public OpcUaVariableDescription() {
		
	}
	
	public OpcUaVariableDescription(NodeId typeId, String fieldName, String fieldDescription, Variant variant) {
		this.typeId = typeId; 
		this.variableName = fieldName; 
		this.variableDescription = fieldDescription; 
		this.variant = variant; 
	}
	
	public NodeId getNodeId() {
		return typeId; 
	}
	
	public String getVariableName() {
		return variableName; 
	}
	
	public String getVariableDescription() {
		return variableDescription; 
	}
	
	public Variant getVariant() {
		return variant; 
	}
}
