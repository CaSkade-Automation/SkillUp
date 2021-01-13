package moduleGenerator;

import java.io.IOException;
import java.util.Enumeration;

import org.osgi.service.component.annotations.Component;

import annotations.Module;
import descriptionGenerator.DescriptionGenerator;

/**
 * Class to generate module description
 * 
 * @Component Indicates that annotated class is intended to be an OSGi
 *            component. <br>
 *            immediate=true, component configuration activates immediately.
 *            <br>
 *            After becoming satisfied component is registered as a service.
 */
@Component(immediate = true, service = ModuleGenerator.class)
public class ModuleGenerator extends DescriptionGenerator {

	// neessary/possible snippets for a module description
	private String moduleSnippet = "<${ModuleIri}> a VDI2206:Module,\r\n" + "					owl:NamedIndividual. ";
	private String capabilitySnippet = "<${ModuleIri}> Cap:hasCapability <${CapabilityIri}> .\r\n"
			+ "<${CapabilityIri}> a Cap:Capability,\r\n" + "					owl:NamedIndividual. ";

	/**
	 * Method to generate rdf description. Takes moduleSnippet and replaces dummies
	 * like module IRI.
	 * 
	 * @param module    object necessary to e.g. get module IRI
	 * @param userFiles if user has it own snippets, they are added to the
	 *                  description
	 * @return
	 */
	public String generateModuleDescription(Object module, Enumeration<String> userFiles) {

		// first get all prefixes
		String prefix = getFileFromResources(null, "prefix.ttl");

		Module moduleAnnotation = module.getClass().getAnnotation(Module.class);

		String userSnippet = getUserSnippets(userFiles, module.getClass().getClassLoader());

		// whole module description
		String moduleDescription = prefix + moduleSnippet + userSnippet;

		if (!moduleAnnotation.capabilityIri().isEmpty()) {
			moduleDescription = moduleDescription + capabilitySnippet;
		}

		// replace dummies
		moduleDescription = moduleDescription.replace("${ModuleIri}", moduleAnnotation.moduleIri())
				.replace("${CapabilityIri}", moduleAnnotation.capabilityIri());
		
		try {
			createFile(moduleDescription, "moduleDescription.ttl");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return moduleDescription;
	}
}
