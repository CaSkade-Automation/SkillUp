package moduleGenerator;

import java.io.IOException;
import java.util.Enumeration;

import org.osgi.service.component.annotations.Component;

import annotations.Module;
import descriptionGenerator.DescriptionGenerator;

@Component(immediate = true, service = ModuleGenerator.class)
public class ModuleGenerator extends DescriptionGenerator {

	private String moduleSnippet = "<${ModuleIri}> a VDI2206:Module,\r\n" + "					owl:NamedIndividual. ";

	private String capabilitySnippet = "<${ModuleIri}> Cap:hasCapability <${CapabilityIri}> .";

	public String generateModuleDescription(Object module, Enumeration<String> userFiles) {

		try {
			String prefix = getFileFromResources(null, "prefix.ttl");

			Module moduleAnnotation = module.getClass().getAnnotation(Module.class);

			String userSnippet = getUserSnippets(userFiles, module.getClass().getClassLoader());

			String moduleDescription = prefix + moduleSnippet + userSnippet;

			if (!moduleAnnotation.capabilityIri().isEmpty()) {
				moduleDescription = moduleDescription + capabilitySnippet;
			}

			moduleDescription = moduleDescription.replace("${ModuleIri}", moduleAnnotation.moduleIri())
					.replace("${CapabilityIri}", moduleAnnotation.capabilityIri());
			try {
				createFile(moduleDescription, "module.ttl");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return moduleDescription;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
