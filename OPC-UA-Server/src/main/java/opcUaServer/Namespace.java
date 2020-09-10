package opcUaServer;

import java.lang.reflect.Field;
import java.util.List;

import org.eclipse.milo.opcua.sdk.core.AccessLevel;
import org.eclipse.milo.opcua.sdk.core.Reference;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.DataItem;
import org.eclipse.milo.opcua.sdk.server.api.ManagedNamespace;
import org.eclipse.milo.opcua.sdk.server.api.MonitoredItem;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaMethodNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.sdk.server.util.SubscriptionModel;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.BuiltinDataType;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;

import annotations.SkillParameter;
import annotations.SkillOutput;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.ubyte;

import statemachine.Isa88StateMachine;
import states.TransitionName;

public class Namespace extends ManagedNamespace {

	public static final String URI = "urn:my:server:namespace";

	private final SubscriptionModel subscriptionModel;

	UaFolderNode folder = null;

	private UaVariableNode skillOutput;
	private GenericMethod newSkill;

	public Namespace(final OpcUaServer opcUaServer) {
		super(opcUaServer, URI);
		subscriptionModel = new SubscriptionModel(opcUaServer, this);
	}

	/**
	 * When namespace is started a new folder for the server is created
	 */
	@Override
	protected void onStartup() {
		super.onStartup();

		subscriptionModel.startup(); 
		
		// create a folder and add it to the node manager
		NodeId folderNodeId = newNodeId("Skills");

		folder = new UaFolderNode(getNodeContext(), folderNodeId, newQualifiedName("Skills"),
				LocalizedText.english("Example Skills"));
		getNodeManager().addNode(folder);

		// make sure our new folder shows up under the server's Objects folder.
		folder.addReference(
				new Reference(folder.getNodeId(), Identifiers.Organizes, Identifiers.ObjectsFolder.expanded(), false));
	}

	public UaFolderNode addFolder(String folderName) {

		UaFolderNode parentFolder = getFolder();

		// create a folder and add it to the node manager
		NodeId folderNodeId = newNodeId("Skills/" + folderName);

		UaFolderNode folder = new UaFolderNode(getNodeContext(), folderNodeId, newQualifiedName(folderName),
				LocalizedText.english(folderName));
		getNodeManager().addNode(folder);
		// make sure our new folder shows up under the Skills folder.
		folder.addReference(
				new Reference(folder.getNodeId(), Identifiers.Organizes, parentFolder.getNodeId().expanded(), false));

		return folder;
	}

	public void addVariableNodes(Object skill, UaFolderNode folder) {

		Field[] fields = skill.getClass().getDeclaredFields();
		for (Field field : fields) {

			if (field.isAnnotationPresent(SkillParameter.class)) {

				OpcUaVariableDescription variableDescription = setVariableDescription(field, skill, true);

				UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
						.setNodeId(newNodeId(folder.getBrowseName() + "/" + variableDescription.getVariableName()))
						.setAccessLevel(ubyte(AccessLevel.getMask(AccessLevel.READ_WRITE)))
						.setUserAccessLevel(ubyte(AccessLevel.getMask(AccessLevel.READ_WRITE)))
						.setBrowseName(newQualifiedName(variableDescription.getVariableName()))
						.setDisplayName(LocalizedText.english(variableDescription.getVariableDescription()))
						.setDataType(variableDescription.getNodeId()).setTypeDefinition(Identifiers.BaseDataVariableType)
						.build();

				node.setValue(new DataValue(variableDescription.getVariant()));

				node.getFilterChain().addLast(new AttributeLoggingFilter(AttributeId.Value::equals, field, skill)); 

				getNodeManager().addNode(node);
				folder.addOrganizes(node);
			}

			if (field.isAnnotationPresent(SkillOutput.class)) {

				OpcUaVariableDescription variableDescription = setVariableDescription(field, skill, false);

				UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
						.setNodeId(newNodeId(folder.getBrowseName() + "/" + variableDescription.getVariableName()))
						.setAccessLevel(ubyte(AccessLevel.getMask(AccessLevel.READ_ONLY)))
						.setUserAccessLevel(ubyte(AccessLevel.getMask(AccessLevel.READ_ONLY)))
						.setBrowseName(newQualifiedName(variableDescription.getVariableName()))
						.setDisplayName(LocalizedText.english(variableDescription.getVariableDescription()))
						.setDataType(variableDescription.getNodeId()).setTypeDefinition(Identifiers.BaseDataVariableType)
						.build();

				node.setValue(new DataValue(variableDescription.getVariant()));

				getNodeManager().addNode(node);
				folder.addOrganizes(node);
				skillOutput = node;
			}
		}
	}

	public OpcUaVariableDescription setVariableDescription(Field field, Object skill, boolean skillInput) {
		Variant variant = null;
		field.setAccessible(true);
		Class<?> type = field.getType();

		NodeId typeId = new NodeId(0, BuiltinDataType.getBuiltinTypeId(type));

		try {
			variant = new Variant(field.get(skill));
		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		String fieldName;
		String fieldDescription;
		if (skillInput) {
			if (!field.getAnnotation(SkillParameter.class).name().isEmpty()) {
				fieldName = field.getAnnotation(SkillParameter.class).name();
			} else {
				fieldName = field.getName();
			}

			if (!field.getAnnotation(SkillParameter.class).description().isEmpty()) {
				fieldDescription = field.getAnnotation(SkillParameter.class).description();
			} else {
				fieldDescription = field.getName();
			}
		} else {
			if (!field.getAnnotation(SkillOutput.class).name().isEmpty()) {
				fieldName = field.getAnnotation(SkillOutput.class).name();
			} else {
				fieldName = field.getName();
			}

			if (!field.getAnnotation(SkillOutput.class).description().isEmpty()) {
				fieldDescription = field.getAnnotation(SkillOutput.class).description();
			} else {
				fieldDescription = field.getName();
			}
		}
		OpcUaVariableDescription variableDescription = new OpcUaVariableDescription(typeId, fieldName, fieldDescription, variant);
		return variableDescription;
	}

	/**
	 * SkillNode is added to the folder by getting input and output arguments as
	 * well as the invoke method
	 * 
	 * @param folder            folder to which skill should be added
	 * @param methodName        name of the skill to add
	 * @param skillRegistration instance of the skill to add
	 */
	public void addAllSkillMethods(UaFolderNode folder, Isa88StateMachine stateMachine, Object skill) {

		for (TransitionName transition : TransitionName.values()) {

			UaMethodNode skillNode = createMethodNode(folder, transition.toString());
			newSkill = new GenericMethod(skillNode, stateMachine, transition, skillOutput, skill);

			skillNode.setInvocationHandler(newSkill);
			getNodeManager().addNode(skillNode);

			skillNode.addReference(new Reference(skillNode.getNodeId(), Identifiers.HasComponent,
					folder.getNodeId().expanded(), false));
		}
		addGetResultMethod(folder, skill);
	}

	public void addGetResultMethod(UaFolderNode folder, Object skill) {

		UaMethodNode skillNode = createMethodNode(folder, "getOutputs");
		GetResultMethod newSkill = new GetResultMethod(skillNode, skill);
		skillNode.setOutputArguments(newSkill.getOutputArguments());
		skillNode.setInvocationHandler(newSkill);
		getNodeManager().addNode(skillNode);

		skillNode.addReference(
				new Reference(skillNode.getNodeId(), Identifiers.HasComponent, folder.getNodeId().expanded(), false));
	}

	public UaMethodNode createMethodNode(UaFolderNode folder, String methodName) {
		UaMethodNode skillNode = UaMethodNode.builder(getNodeContext())
				.setNodeId(newNodeId(folder.getBrowseName() + "/" + methodName))
				.setBrowseName(newQualifiedName(methodName)).setDisplayName(new LocalizedText(null, methodName))
				.setDescription(LocalizedText.english(methodName)).build();

		return skillNode;
	}

	@Override
	public void onDataItemsCreated(final List<DataItem> dataItems) {
		this.subscriptionModel.onDataItemsCreated(dataItems);
	}

	@Override
	public void onDataItemsModified(final List<DataItem> dataItems) {
		this.subscriptionModel.onDataItemsModified(dataItems);
	}

	@Override
	public void onDataItemsDeleted(final List<DataItem> dataItems) {
		this.subscriptionModel.onDataItemsDeleted(dataItems);
	}

	@Override
	public void onMonitoringModeChanged(final List<MonitoredItem> monitoredItems) {
		this.subscriptionModel.onMonitoringModeChanged(monitoredItems);
	}

	public UaFolderNode getFolder() {
		return folder;
	}

	public GenericMethod getGenericMethod() {
		return newSkill;
	}
}
