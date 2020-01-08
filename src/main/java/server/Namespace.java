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

public class Namespace extends ManagedNamespace {

	public static final String URI = "urn:my:server:namespace";

	private final SubscriptionModel subscriptionModel;

	public Namespace(final OpcUaServer server) {
		super(server, URI);
		subscriptionModel = new SubscriptionModel(server, this);
	}

	@Override
	protected void onStartup() {
		super.onStartup();

		// create a folder and add it to the node manager
		NodeId folderNodeId = newNodeId("Beispiel");

		UaFolderNode folder = new UaFolderNode(getNodeContext(), folderNodeId, newQualifiedName("Beispiel"),
				LocalizedText.english("Beispiel Ordner"));
		getNodeManager().addNode(folder);

		// make sure our new folder shows up under the server's Objects folder.
		folder.addReference(
				new Reference(folder.getNodeId(), Identifiers.Organizes, Identifiers.ObjectsFolder.expanded(), false));

		// Add the rest of nodes
		addSimpleMethod(folder);
	}

	private void addSimpleMethod(UaFolderNode folder) {
		UaMethodNode methodNode = UaMethodNode.builder(getNodeContext()).setNodeId(newNodeId("Beispiel/SimpleMethode"))
				.setBrowseName(newQualifiedName("SimpleMethode"))
				.setDisplayName(new LocalizedText(null, "SimpleMethode"))
				.setDescription(LocalizedText.english("This is an simple method.")).build();

		SimpleMethod simpleMethod = new SimpleMethod(methodNode);
		methodNode.setProperty(UaMethodNode.InputArguments, simpleMethod.getInputArguments());
		methodNode.setProperty(UaMethodNode.OutputArguments, simpleMethod.getOutputArguments());
		methodNode.setInvocationHandler(simpleMethod);

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
}
