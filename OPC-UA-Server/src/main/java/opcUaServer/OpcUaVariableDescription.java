package opcUaServer;

import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;

/**
 * Class of OpcUa Variables with every necessary information for them
 */
public class OpcUaVariableDescription {

	private NodeId typeId;
	private String variableName;
	private String variableDescription;
	private Variant variant;

	public OpcUaVariableDescription() {
	}

	/**
	 * Constructor of class {@link OpcUaVariableDescription}
	 * 
	 * @param typeId           data type of variable
	 * @param fieldName        variable name
	 * @param fieldDescription variable description
	 * @param variant          default (start) value of variable
	 */
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
