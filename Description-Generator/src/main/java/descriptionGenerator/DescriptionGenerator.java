package descriptionGenerator;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class with some methods that are required by
 * {@link SkillDescriptionGenerator} and ModuleGenerator. Also with turtle
 * snippets in resources folder (prefix.ttl and stateMachine.ttl) that are
 * relevant for all.
 */
public class DescriptionGenerator {

	private Logger logger = LoggerFactory.getLogger(DescriptionGenerator.class);

	/**
	 * Method gets the file from resources folder, reads it and converts it to a
	 * String
	 * 
	 * @param classLoader Necessary to get file from different class
	 * @param fileName    Name of the file which we want from resources folder
	 * @return given file as string
	 */
	public String getFileFromResources(ClassLoader classLoader, String fileName) {

		// if prefix.ttl or stateMachine.ttl is wanted, classLoader is set to null
		if (classLoader == null) {
			classLoader = DescriptionGenerator.class.getClassLoader();
		}
		URL resource = classLoader.getResource(fileName);
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(resource.openStream()));

			StringBuilder file = new StringBuilder();
			String currentline = "";

			while ((currentline = reader.readLine()) != null) {
				file.append(currentline);
			}

			String fileString = file.toString();
			return fileString;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Couldn't get file " + fileName + " from resources folder");
			return null;
		}
	}

	/**
	 * Method to get turtle snippets created by user and converts it to a String
	 * with {@link #getFileFromResources(ClassLoader, String)}
	 * 
	 * @param userFiles   Turtle snippets created by user
	 * @param classLoader To get files from different class
	 * @return All the user's turtle snippets bundled as a String.
	 */
	public String getUserSnippets(Enumeration<String> userFiles, ClassLoader classLoader) {
		String userSnippet = "";
		// if files from user don't exist an empty string is returned
		if (userFiles == null)
			return userSnippet;

		// iterate over every user file
		for (Iterator<String> it = userFiles.asIterator(); it.hasNext();) {
			userSnippet = userSnippet + getFileFromResources(classLoader, it.next());
		}
		return userSnippet;
	}

//	//nur zu Testzwecken, danach löschen 
	public void createFile(String turtleFile, String localFileName) throws IOException {

		FileOutputStream fileOutputStream = new FileOutputStream("turtle-files/" + localFileName);
		byte[] strToBytes = turtleFile.getBytes();
		fileOutputStream.write(strToBytes);

		fileOutputStream.close();
	}
}
