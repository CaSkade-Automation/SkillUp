package server;

import java.lang.reflect.Field;
import java.util.List;

import org.eclipse.milo.opcua.sdk.core.AccessLevel;
import org.eclipse.milo.opcua.sdk.core.Reference;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.DataItem;
import org.eclipse.milo.opcua.sdk.server.api.ManagedNamespace;
import org.eclipse.milo.opcua.sdk.server.api.MonitoredItem;
import org.eclipse.milo.opcua.sdk.server.nodes.AttributeObserver;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaMethodNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.sdk.server.util.SubscriptionModel;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.BuiltinDataType;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;

import annotations.SkillInput;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.ubyte;

import statemachine.StateMachine;
import states.TransitionName;

public class Namespace extends ManagedNamespace {

	public static final String URI = "urn:my:server:namespace";

	private final SubscriptionModel subscriptionModel;

	UaFolderNode folder = null;

	public Namespace(final OpcUaServer server) {
		super(server, URI);
		subscriptionModel = new SubscriptionModel(server, this);
	}

	/**
	 * When namespace is started a new folder for the server is created
	 */
	@Override
	protected void onStartup() {
		super.onStartup();

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
			Variant variant = null;
			if (field.isAnnotationPresent(SkillInput.class)) {

				Class<?> type = field.getType();

				NodeId typeId = new NodeId(0, BuiltinDataType.getBuiltinTypeId(type));

				try {
					variant = getVariant(field, skill, type);
				} catch (IllegalArgumentException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IllegalAccessException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
						.setNodeId(newNodeId(folder.getBrowseName() + "/" + field.getName()))
						.setAccessLevel(ubyte(AccessLevel.getMask(AccessLevel.READ_WRITE)))
						.setUserAccessLevel(ubyte(AccessLevel.getMask(AccessLevel.READ_WRITE)))
						.setBrowseName(newQualifiedName(field.getName()))
						.setDisplayName(LocalizedText.english(field.getName())).setDataType(typeId)
						.setTypeDefinition(Identifiers.BaseDataVariableType).build();

				node.setValue(new DataValue(variant));
				node.addAttributeObserver(new AttributeObserver() {

					@Override
					public void attributeChanged(UaNode node, AttributeId attributeId, Object value) {
						// TODO Auto-generated method stub
						DataValue dataValue = (DataValue) value;
						try {
							setField(field, type, dataValue, skill);
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});

				getNodeManager().addNode(node);
				folder.addOrganizes(node);
			}
		}
	}

	public Variant getVariant(Field field, Object skill, Class<?> type)
			throws IllegalArgumentException, IllegalAccessException {
		if (type == boolean.class) {
			return new Variant(field.getBoolean(skill));
		} else if (type == byte.class) {
			return new Variant(field.getByte(skill));
		} else if (type == short.class) {
			return new Variant(field.getShort(skill));
		} else if (type == int.class) {
			return new Variant(field.getInt(skill));
		} else if (type == long.class) {
			return new Variant(field.getLong(skill));
		} else if (type == float.class) {
			return new Variant(field.getFloat(skill));
		} else if (type == double.class) {
			return new Variant(field.getDouble(skill));
		} else {
			return null;
		}
	}

	public void setField(Field field, Class<?> type, DataValue dataValue, Object skill)
			throws IllegalArgumentException, IllegalAccessException {
		if (type == boolean.class) {
			field.set(skill, (boolean) dataValue.getValue().getValue());
		} else if (type == byte.class) {
			field.set(skill, (byte) dataValue.getValue().getValue());
		} else if (type == short.class) {
			field.set(skill, (short) dataValue.getValue().getValue());
		} else if (type == int.class) {
			field.set(skill, (int) dataValue.getValue().getValue());
		} else if (type == long.class) {
			field.set(skill, (long) dataValue.getValue().getValue());
		} else if (type == float.class) {
			field.set(skill, (float) dataValue.getValue().getValue());
		} else if (type == double.class) {
			field.set(skill, (double) dataValue.getValue().getValue());
		}
	}

	/**
	 * SkillNode is added to the folder by getting input and output arguments as
	 * well as the invoke method
	 * 
	 * @param folder            folder to which skill should be added
	 * @param methodName        name of the skill to add
	 * @param skillRegistration instance of the skill to add
	 */
	public void addAllSkillMethods(UaFolderNode folder, StateMachine stateMachine) {

		for (TransitionName transition : TransitionName.values()) {

			UaMethodNode skillNode = UaMethodNode.builder(getNodeContext())
					.setNodeId(newNodeId(folder.getBrowseName() + "/" + transition.toString()))
					.setBrowseName(newQualifiedName(transition.toString()))
					.setDisplayName(new LocalizedText(null, transition.toString()))
					.setDescription(LocalizedText.english(transition.toString())).build();

			GenericMethod newSkill = new GenericMethod(skillNode, stateMachine, transition);

			skillNode.setInvocationHandler(newSkill);
			getNodeManager().addNode(skillNode);

			skillNode.addReference(new Reference(skillNode.getNodeId(), Identifiers.HasComponent,
					folder.getNodeId().expanded(), false));
		}
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
}
