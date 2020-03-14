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
import org.eclipse.milo.opcua.stack.core.types.structured.Argument;

import opcuaSkillRegistration.OPCUASkillRegistration;

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

	/**
	 * SkillNode is added to the folder by getting input and output arguments as
	 * well as the invoke method
	 * 
	 * @param folder     folder to which skill should be added
	 * @param methodName name of the skill to add
	 * @param skillRegistration     instance of the skill to add
	 */
	public void addMethod(UaFolderNode folder, String methodName, OPCUASkillRegistration skillRegistration, Argument[] inputArguments, Argument[] outputArguments) {
		UaMethodNode skillNode = UaMethodNode.builder(getNodeContext()).setNodeId(newNodeId("Skills/" + methodName))
				.setBrowseName(newQualifiedName(methodName)).setDisplayName(new LocalizedText(null, methodName))
				.setDescription(LocalizedText.english("This is an simple skill.")).build();

		GenericMethod newSkill = new GenericMethod(skillNode, skillRegistration, inputArguments, outputArguments);
		skillNode.setProperty(UaMethodNode.InputArguments, newSkill.getInputArguments());
		skillNode.setProperty(UaMethodNode.OutputArguments, newSkill.getOutputArguments());
		skillNode.setInvocationHandler(newSkill);

		getNodeManager().addNode(skillNode);

		skillNode.addReference(
				new Reference(skillNode.getNodeId(), Identifiers.HasComponent, folder.getNodeId().expanded(), false));
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
