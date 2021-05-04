package opcUaServer;

import java.lang.reflect.Field;
import java.util.ArrayList;
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
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode.UaVariableNodeBuilder;
import org.eclipse.milo.opcua.sdk.server.util.SubscriptionModel;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.BuiltinDataType;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;

import skillup.annotations.SkillParameter;
import skillup.annotations.SkillOutput;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.ubyte;

import statemachine.Isa88StateMachine;
import states.TransitionName;

/**
 * Namespace of OPC-UA-Server with methods to add folders and nodes.
 */
public class Namespace extends ManagedNamespace {

	public static final String URI = "urn:my:server:namespace";
	private final SubscriptionModel subscriptionModel;

	private UaFolderNode parentFolder = null;
	private List<UaVariableNode> skillOutputs = new ArrayList<UaVariableNode>();
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

		FolderDescription folderDescription = new FolderDescription("Skills", "Skills", "Example Skills");
		parentFolder = addFolder(folderDescription);
	}

	/**
	 * Method creates a new folder.
	 * 
	 * @param folderDescription description of new folder with nodeId, name,
	 *                          description and perhaps its parent folder
	 * @return the created folder
	 */
	public UaFolderNode addFolder(FolderDescription folderDescription) {

		UaFolderNode parentFolder = folderDescription.getParentFolder();
		NodeId folderNodeId;

		// create a folder and add it to the node manager
		// if description contains parent folder, new folder appends on parent folder
		// else a new parent folder is created
		// make sure our new folder shows up under the server's Objects folder (if a new
		// parent folder) else under the skills folder.
		if (parentFolder == null) {
			folderNodeId = newNodeId(folderDescription.getNodeId());
			UaFolderNode folder = createFolder(folderDescription, folderNodeId);
			folder.addReference(new Reference(folder.getNodeId(), Identifiers.Organizes,
					Identifiers.ObjectsFolder.expanded(), false));
			return folder;
		} else {
			folderNodeId = newNodeId(folderDescription.getParentFolder().getNodeId().getIdentifier().toString() + "/"
					+ folderDescription.getNodeId());
			UaFolderNode folder = createFolder(folderDescription, folderNodeId);
			folder.addReference(new Reference(folder.getNodeId(), Identifiers.Organizes,
					parentFolder.getNodeId().expanded(), false));
			return folder;
		}
	}

	/**
	 * Method to create a new folder node and add it to the node manager
	 * 
	 * @param folderDescription description which contains name etc.
	 * @param folderNodeId      nodeId of new node
	 * @return created folder
	 */
	public UaFolderNode createFolder(FolderDescription folderDescription, NodeId folderNodeId) {
		UaFolderNode folder = new UaFolderNode(getNodeContext(), folderNodeId,
				newQualifiedName(folderDescription.getFolderName()), folderDescription.getFolderDescription());

		getNodeManager().addNode(folder);
		return folder;
	}

	/**
	 * Method to add variable nodes (skill parameter and output) to the
	 * corresponding skill folder
	 * 
	 * @param skill  Object to get parameter and outputs of skill
	 * @param folder variable nodes are added to this folder
	 */
	public void addVariableNodes(Object skill, UaFolderNode folder) {
		// for every new skill outputs are cleared
		skillOutputs.clear();

		Field[] fields = skill.getClass().getDeclaredFields();
		for (Field field : fields) {
			if (field.isAnnotationPresent(SkillParameter.class)) {

				OpcUaVariableDescription variableDescription = setVariableDescription(field, skill, true);
				UaVariableNode node = createVariableNode(variableDescription, folder, true);

				// node is monitored to change value of skill parameter when value of variable
				// node changes
				node.getFilterChain().addLast(new AttributeLoggingFilter(AttributeId.Value::equals, field, skill));
			}

			else if (field.isAnnotationPresent(SkillOutput.class)) {

				OpcUaVariableDescription variableDescription = setVariableDescription(field, skill, false);
				UaVariableNode node = createVariableNode(variableDescription, folder, false);

				// add this node to list with skills outputs
				skillOutputs.add(node);
			}
		}
	}

	/**
	 * Method sets variable description with typeId, name, description and value
	 * 
	 * @param field          skill parameter or output whose description should be
	 *                       set
	 * @param skill          object necessary to get value of skill parameter or
	 *                       output
	 * @param skillParameter boolean value to differentiate between parameter and
	 *                       output
	 * @return new object of OpcUaVariableDescription
	 */
	public OpcUaVariableDescription setVariableDescription(Field field, Object skill, boolean skillParameter) {
		Variant variant = null;
		field.setAccessible(true);
		Class<?> type = field.getType();

		// to build node id of right type
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
		// differentiate between parameter and output
		// set name and description of variable
		if (skillParameter) {
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
		OpcUaVariableDescription variableDescription = new OpcUaVariableDescription(typeId, fieldName, fieldDescription,
				variant);
		return variableDescription;
	}

	/**
	 * Method creates variable nodes for skill parameters or outputs.
	 * 
	 * @param variableDescription description with name, description, type and value
	 * @param folder              node to which variable should be added
	 * @param skillParameter      boolean value to differentiate between skill
	 *                            parameter and output
	 * @return the created variable node
	 */
	public UaVariableNode createVariableNode(OpcUaVariableDescription variableDescription, UaFolderNode folder,
			boolean skillParameter) {

		UaVariableNodeBuilder nodeBuilder = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
				.setNodeId(newNodeId(folder.getBrowseName() + "/" + variableDescription.getVariableName()))
				.setBrowseName(newQualifiedName(variableDescription.getVariableName()))
				.setDisplayName(LocalizedText.english(variableDescription.getVariableDescription()))
				.setDataType(variableDescription.getNodeId()).setTypeDefinition(Identifiers.BaseDataVariableType);

		UaVariableNode node;

		if (skillParameter) {
			// skill parameter can be read and written
			node = nodeBuilder.setAccessLevel(ubyte(AccessLevel.getMask(AccessLevel.READ_WRITE)))
					.setUserAccessLevel(ubyte(AccessLevel.getMask(AccessLevel.READ_WRITE))).build();
		} else {
			// skill output can only be read
			node = nodeBuilder.setAccessLevel(ubyte(AccessLevel.getMask(AccessLevel.READ_ONLY)))
					.setUserAccessLevel(ubyte(AccessLevel.getMask(AccessLevel.READ_ONLY))).build();
		}

		// value of variable node matches to value of skill parameter/output of skills
		// object
		node.setValue(new DataValue(variableDescription.getVariant()));

		getNodeManager().addNode(node);
		folder.addOrganizes(node);

		return node;
	}

	/**
	 * Every transition of stateMachine added as method nodes to skill folder and
	 * additionally added a method to get values of skill outputs
	 * 
	 * @param folder       skills folder to which method should be added
	 * @param stateMachine skills state machine
	 * @param skill        instance of the skill to get skill outputs
	 */
	public void addAllSkillMethods(UaFolderNode folder, Isa88StateMachine stateMachine, Object skill) {

		for (TransitionName transition : TransitionName.values()) {

			UaMethodNode skillNode = createMethodNode(folder, transition.toString());
			newSkill = new GenericMethod(skillNode, stateMachine, transition, skillOutputs, skill);

			skillNode.setInvocationHandler(newSkill);
			getNodeManager().addNode(skillNode);

			// make sure our new method node shows up as a component under the skills
			// folder.
			skillNode.addReference(new Reference(skillNode.getNodeId(), Identifiers.HasComponent,
					folder.getNodeId().expanded(), false));
		}
		addGetOutputs(folder, skill);
	}

	/**
	 * Method to get the actual results (value of skill output) as method node of
	 * skill folder.
	 * 
	 * @param folder skills folder to which method should be added
	 * @param skill  object of skill to get actual value of skill output (which is a
	 *               field of skills object)
	 */
	public void addGetOutputs(UaFolderNode folder, Object skill) {

		UaMethodNode skillNode = createMethodNode(folder, "getOutputs");
		GetOutputsMethod newSkill = new GetOutputsMethod(skillNode, skill);
		skillNode.setOutputArguments(newSkill.getOutputArguments());
		skillNode.setInvocationHandler(newSkill);
		getNodeManager().addNode(skillNode);

		// make sure our new method noded shows up as a component under the skills
		// folder.
		skillNode.addReference(
				new Reference(skillNode.getNodeId(), Identifiers.HasComponent, folder.getNodeId().expanded(), false));
	}

	/**
	 * Method to create node of a method by setting some properties like node id.
	 * 
	 * @param folder     skills folder to which method should be added.
	 * @param methodName name for this node
	 * @return created node
	 */
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

	/**
	 * Method to get parent folder
	 * 
	 * @return parent folder
	 */
	public UaFolderNode getFolder() {
		return parentFolder;
	}

	/**
	 * Method to get instance of GenericMethod. Necessary to add StateChangeObserver
	 * 
	 * @return instance of class GenericMethod
	 */
	public GenericMethod getGenericMethod() {
		return newSkill;
	}
}
