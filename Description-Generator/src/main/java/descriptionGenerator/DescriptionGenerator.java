package descriptionGenerator;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;

public class DescriptionGenerator {

	/**
	 * Method gets the file from resources folder, reads it and converts it to a
	 * string
	 * 
	 * @param fileName the name of file which we want from resources folder
	 * @return returns given file as string
	 */
	public String getFileFromResources(ClassLoader classLoader, String fileName) throws IOException {

		if (classLoader == null) {
			classLoader = DescriptionGenerator.class.getClassLoader();
		}
		URL resource = classLoader.getResource(fileName);
		BufferedReader reader = new BufferedReader(new InputStreamReader(resource.openStream()));
		StringBuilder file = new StringBuilder();
		String currentline = "";

		while ((currentline = reader.readLine()) != null) {
			file.append(currentline);
		}
		String fileString = file.toString();
		return fileString;
	}
	
	public String getUserSnippets(Enumeration<String> userFiles, ClassLoader classLoader) {
		String userSnippet = "";
		if (userFiles != null) {
			for (Iterator<String> it = userFiles.asIterator(); it.hasNext();) {
				try {
					userSnippet = userSnippet + getFileFromResources(classLoader, it.next());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return userSnippet; 
	}

	//nur zu Testzwecken, danach löschen 
	public void createFile(String turtleFile, String localFileName) throws IOException {

		FileOutputStream fileOutputStream = new FileOutputStream("turtle-files/" + localFileName);
		byte[] strToBytes = turtleFile.getBytes();
		fileOutputStream.write(strToBytes);

		fileOutputStream.close();
	}
}
