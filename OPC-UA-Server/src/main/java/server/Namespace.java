package server;

import java.util.List;
import org.eclipse.milo.opcua.sdk.core.Reference;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.DataItem;
import org.eclipse.milo.opcua.sdk.server.api.ManagedNamespace;
import org.eclipse.milo.opcua.sdk.server.api.MonitoredItem;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaMethodNode;
import org.eclipse.milo.opcua.sdk.server.util.SubscriptionModel;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;

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

	/**
	 * SkillNode is added to the folder by getting input and output arguments as
	 * well as the invoke method
	 * 
	 * @param folder            folder to which skill should be added
	 * @param methodName        name of the skill to add
	 * @param skillRegistration instance of the skill to add
	 */
	public void addMethod(UaFolderNode folder, StateMachine stateMachine) {

		for(TransitionName transition : TransitionName.values()) {
			
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
