package opcUaServer;

import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;

/**
 * Class of OpcUa Folders with every necessary information for them
 */
public class FolderDescription {

	private String nodeId;
	private String folderName;
	private LocalizedText folderDescription;
	private UaFolderNode parentFolder;

	public FolderDescription() {
	}

	/**
	 * Constructor of class {@link FolderDescription} without parent folder (to
	 * create superior folder)
	 * 
	 * @param nodeId            String which will be nodeId
	 * @param folderName        name of folder
	 * @param folderDescription description of folder
	 */
	public FolderDescription(String nodeId, String folderName, String folderDescription) {
		this.nodeId = nodeId;
		this.folderName = folderName;
		this.folderDescription = LocalizedText.english(folderDescription);
	}

	/**
	 * Constructor of class {@link FolderDescription} with parent folder (to create
	 * skill folder)
	 * 
	 * @param nodeId            String which will be nodeId
	 * @param folderName        name of folder
	 * @param folderDescription description of folder
	 * @param parentFolder      folder to which new folder should be added
	 */
	public FolderDescription(String nodeId, String folderName, String folderDescription, UaFolderNode parentFolder) {
		this.nodeId = nodeId;
		this.folderName = folderName;
		this.folderDescription = LocalizedText.english(folderDescription);
		this.parentFolder = parentFolder;
	}

	public String getNodeId() {
		return nodeId;
	}

	public String getFolderName() {
		return folderName;
	}

	public LocalizedText getFolderDescription() {
		return folderDescription;
	}

	public UaFolderNode getParentFolder() {
		return parentFolder;
	}
}
