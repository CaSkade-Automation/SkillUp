package restResource;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import annotations.Skill;
import statemachine.StateMachine;

@Component(immediate = true, service = RestResource.class)
@JaxrsResource
@Path("skills")
public class RestResource {

	private static Logger logger = LoggerFactory.getLogger(RestResource.class);

	private HashMap<UUID, RestSkill> skillTable = new HashMap<UUID, RestSkill>();

	@Activate
	public void activate() {
		logger.info("Activating " + getClass().getSimpleName());
	}

	@Deactivate
	public void deactivate() {
		logger.info("Deactivating " + getClass().getSimpleName());
	}

	public void generateSkill(Object skill, StateMachine stateMachine) {
		String skillIri = null;
		if (skill.getClass().isAnnotationPresent(Skill.class)) {
			skillIri = skill.getClass().getAnnotation(Skill.class).skillIri();
		} else {
			logger.info(getClass().getSimpleName()
					+ ": ERR while generating new skill: Object does not have \"Skill\"-Annotation.");
			return;
		}
		RestSkill newSkill = new RestSkill(stateMachine, skillIri);
		skillTable.put(newSkill.getUUID(), newSkill);
	}

	// we identify the skill by its "Skill"-Annotation (see Action-Generator:
	// annotations) -> skillIri
	public void deleteSkill(Object skill) {
		logger.info(getClass().getSimpleName() + ": Deleting skill \"" + skill.getClass().toString() + "\"...");

		// get iri of the skill we need to delete
		if (skill.getClass().isAnnotationPresent(Skill.class)) {
			String skillIri = skill.getClass().getAnnotation(Skill.class).skillIri();
			logger.info(getClass().getSimpleName() + ": skillIri=" + skillIri);

			// check if iri is present in skillTable
			Set<UUID> setOfKeys = skillTable.keySet();
			for (UUID key : setOfKeys) {

				// match
				if (skillIri.equals(skillTable.get(key).getSkillIri())) {
					logger.info(
							getClass().getSimpleName() + ": Found match (uuid=" + skillTable.get(key).getUUID() + ").");
					skillTable.remove(key);
					logger.info(getClass().getSimpleName() + ": Skill (uuid=" + skillTable.get(key).getUUID()
							+ ") removed.");

					// TODO: can we omit the rest? (break) is there a check we do not have multiple
					// skills with the same iri?
					break;
				}
			}

		} else {
			logger.info(getClass().getSimpleName() + ": ERR while deleting skill: Object ("
					+ skill.getClass().toString() + ") does not have \"Skill\"-Annotation.");
			return;
		}
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	public String landing() {
		logger.info(getClass().getSimpleName() + ": Landing page called!");

		StringBuilder sb = new StringBuilder("<html><body>");
		sb.append("<h1>Simple RESTful OSGi StateMachine</h1>");
		sb.append("<h2>These are all available RestSkills</h2>");

		Set<UUID> setOfKeys = skillTable.keySet();
		for (UUID key : setOfKeys) {
			sb.append("<p>");
			sb.append("Key: \"" + key + "\", UUID: \"" + skillTable.get(key).getUUID() + "\", skillIri: \""
					+ skillTable.get(key).getSkillIri() + "\", State: \"" + skillTable.get(key).getState() + "\"");
			sb.append("</p>");
		}

		sb.append("</body></html>");
		return sb.toString();
	}

	@GET
	@Path("{uid}")
	@Produces(MediaType.TEXT_HTML)
	public String info(@PathParam("uid") String uid) {
		UUID key = UUID.fromString(uid);
		logger.info(getClass().getSimpleName() + ": RestSkill # " + key + ": Info page called!");

		StringBuilder sb = new StringBuilder("<html><body>");
		sb.append("<h1>Simple RESTful OSGi StateMachine</h1>");
		sb.append("<h2>Info page for RestSkill # " + key + "</h2>");

		if (skillTable.containsKey(key)) {
			// key exists
			sb.append("<p>");
			sb.append("Key: \"" + key + "\", UUID: \"" + skillTable.get(key).getUUID() + "\", skillIri: \""
					+ skillTable.get(key).getSkillIri() + "\", State: \"" + skillTable.get(key).getState() + "\"");
			sb.append("</p>");
		} else {
			// key does not exist
			sb.append("<p>");
			sb.append("Key: \"" + key + "\" is not a valid key. (Instance not found!)");
			sb.append("</p>");
		}

		sb.append("</body></html>");
		return sb.toString();
	}

	@GET
	@Path("{uid}/start")
	@Produces(MediaType.TEXT_HTML)
	public String start(@PathParam("uid") String uid) {
		UUID key = UUID.fromString(uid);
		logger.info(getClass().getSimpleName() + ": RestSkill # " + key + " START page called!");

		StringBuilder sb = new StringBuilder("<html><body>");
		sb.append("<h1>Simple RESTful OSGi StateMachine</h1>");
		sb.append("<h2>You called the START page for RestSkill # " + key + "</h2>");

		if (skillTable.containsKey(key)) {
			sb.append("<p>");
			sb.append("Key: \"" + key + "\", UUID: \"" + skillTable.get(key).getUUID() + "\", skillIri: \""
					+ skillTable.get(key).getSkillIri() + "\", State: \"" + skillTable.get(key).getState() + "\"");
			sb.append("</p>");

			sb.append("<p>");
			sb.append("You can check the skill again to see if you were successful.");
			sb.append("</p>");

			// actually start the skill
			skillTable.get(key).start();

			sb.append("<p>");
			sb.append("http://localhost:8181/skills/" + key);
			sb.append("</p>");
		} else {
			// key does not exist
			sb.append("<p>");
			sb.append("Key: \"" + key + "\" is not a valid key. (Instance not found!)");
			sb.append("</p>");
		}

		sb.append("</body></html>");
		return sb.toString();
	}

	@GET
	@Path("{uid}/reset")
	@Produces(MediaType.TEXT_HTML)
	public String reset(@PathParam("uid") String uid) {
		UUID key = UUID.fromString(uid);
		logger.info(getClass().getSimpleName() + ": RestSkill # " + key + " RESET page called!");

		StringBuilder sb = new StringBuilder("<html><body>");
		sb.append("<h1>Simple RESTful OSGi StateMachine</h1>");
		sb.append("<h2>You called the RESET page for RestSkill # " + key + "</h2>");

		if (skillTable.containsKey(key)) {
			sb.append("<p>");
			sb.append("Key: \"" + key + "\", UUID: \"" + skillTable.get(key).getUUID() + "\", skillIri: \""
					+ skillTable.get(key).getSkillIri() + "\", State: \"" + skillTable.get(key).getState() + "\"");
			sb.append("</p>");

			sb.append("<p>");
			sb.append("You can check the skill again to see if you were successful.");
			sb.append("</p>");

			// actually reset the skill
			skillTable.get(key).reset();

			sb.append("<p>");
			sb.append("http://localhost:8181/skills/" + key);
			sb.append("</p>");
		} else {
			// key does not exist
			sb.append("<p>");
			sb.append("Key: \"" + key + "\" is not a valid key. (Instance not found!)");
			sb.append("</p>");
		}

		sb.append("</body></html>");
		return sb.toString();
	}

		}
	}

}