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

import methodRegistration.MethodRegistration;

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
		NodeId folderNodeId = newNodeId("Example");

		folder = new UaFolderNode(getNodeContext(), folderNodeId, newQualifiedName("Example"),
				LocalizedText.english("Example Method"));
		getNodeManager().addNode(folder);

		// make sure our new folder shows up under the server's Objects folder.
		folder.addReference(
				new Reference(folder.getNodeId(), Identifiers.Organizes, Identifiers.ObjectsFolder.expanded(), false));
	}

	/**
	 * MethodNode is added to the folder by getting input and output arguments as
	 * well as the invoke method
	 * 
	 * @param folder     folder to which method should be added
	 * @param methodName name of the method to add
	 * @param method     instance of the method to add
	 */
	public void addMethod(UaFolderNode folder, String methodName, MethodRegistration method) {
		UaMethodNode methodNode = UaMethodNode.builder(getNodeContext()).setNodeId(newNodeId("Example/" + methodName))
				.setBrowseName(newQualifiedName(methodName)).setDisplayName(new LocalizedText(null, methodName))
				.setDescription(LocalizedText.english("This is an simple method.")).build();

		GenericMethod newMethod = new GenericMethod(methodNode, method);
		methodNode.setProperty(UaMethodNode.InputArguments, newMethod.getInputArguments());
		methodNode.setProperty(UaMethodNode.OutputArguments, newMethod.getOutputArguments());
		methodNode.setInvocationHandler(newMethod);

		getNodeManager().addNode(methodNode);

		methodNode.addReference(
				new Reference(methodNode.getNodeId(), Identifiers.HasComponent, folder.getNodeId().expanded(), false));
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
