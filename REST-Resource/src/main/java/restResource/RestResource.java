package restResource;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import annotations.Skill;
import statemachine.Isa88StateMachine;

@Component(immediate = true, service = RestResource.class)
@JaxrsResource
@Path("skills")
public class RestResource {

	private static Logger logger;
	private HashMap<UUID, RestSkill> skillDirectory;

	public RestResource() {
		this.skillDirectory = new HashMap<UUID, RestSkill>();
		RestResource.logger = LoggerFactory.getLogger(RestResource.class);
	}

	@Activate
	public void activate() {
		logger.info("Activating " + getClass().getSimpleName());
	}

	@Deactivate
	public void deactivate() {
		logger.info("Deactivating " + getClass().getSimpleName());
	}

	public void generateSkill(Object skill, Isa88StateMachine stateMachine) {
		if (!skill.getClass().isAnnotationPresent(Skill.class)) {
			logger.info(getClass().getSimpleName()
					+ ": ERR while generating new skill: Object does not have \"Skill\"-Annotation.");
			return;
		}
		if (!skill.getClass().getAnnotation(Skill.class).type().equals("RestSkill")) {
			logger.info(getClass().getSimpleName()
					+ ": ERR while generating new skill: Object's \"Skill\"-Annotation Type is not \"RestSkill\".");
			return;
		}
		RestSkill newSkill = null;
		do {
			newSkill = new RestSkill(stateMachine, skill);
		} while (skillDirectory.containsKey(newSkill.getUUID()));
		skillDirectory.put(newSkill.getUUID(), newSkill);
	}

	public RestSkill getRestSkillBySkillObject(Object skillObject) {
		for (RestSkill rSkill : skillDirectory.values()) {
			if (rSkill.getSkillObject().equals(skillObject)) {
				return rSkill;
			}
		}
		return null;
	}

	public void deleteSkill(Object skill) {
		logger.info(getClass().getSimpleName() + ": Deleting skill \"" + skill.toString() + "\"...");

		for (UUID key : skillDirectory.keySet()) {
			if (skillDirectory.get(key).getSkillObject().equals(skill)) {
				logger.info(getClass().getSimpleName() + ": Found skill in skill directory ("
						+ skillDirectory.get(key).getSkillIri() + ", " + key.toString() + ")");
				skillDirectory.remove(key);
			}
		}
	}

	public boolean isValidUUID(String toCheck) {
		try {
			UUID.fromString(toCheck);
			return true;
		} catch (Exception e) {
			logger.error(getClass().getSimpleName() + ": ERR: (" + toCheck + ") is not a valid UUID.");
			return false;
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response landing() {
		logger.info(getClass().getSimpleName() + ": Landing page called!");

		JsonObjectBuilder objBuilder = Json.createObjectBuilder();

		for (RestSkill rSkill : skillDirectory.values()) {
			objBuilder.add(rSkill.getSkillIri(), Json.createObjectBuilder().add("state", rSkill.getState()).add("uuid",
					rSkill.getUUID().toString()));
		}
		
		String responseString = objBuilder.build().toString();
		return Response.status(Response.Status.OK).entity(responseString).build();
	}

	@GET
	@Path("{uid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response info(@PathParam("uid") String uid) {

		if (!isValidUUID(uid)) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
		UUID key = UUID.fromString(uid);
		
		for (RestSkill rSkill : skillDirectory.values()) {
			if (rSkill.getUUID().equals(key)) {
				JsonObject obj = Json.createObjectBuilder().add(rSkill.getSkillIri(), Json.createObjectBuilder().add("state", rSkill.getState()).add("uuid",
						rSkill.getUUID().toString())).build();
				
				String responseString = obj.toString();
				return Response.status(Response.Status.OK).entity(responseString).build();
			}
		}
		
		// no match found!
		return Response.status(Response.Status.NOT_FOUND).build();
	}

	@POST
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

	@POST
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

	@POST
	@Path("{uid}/hold")
	@Produces(MediaType.TEXT_HTML)
	public String hold(@PathParam("uid") String uid) {
		UUID key = UUID.fromString(uid);
		logger.info(getClass().getSimpleName() + ": RestSkill # " + key + " HOLD page called!");

		StringBuilder sb = new StringBuilder("<html><body>");
		sb.append("<h1>Simple RESTful OSGi StateMachine</h1>");
		sb.append("<h2>You called the HOLD page for RestSkill # " + key + "</h2>");

		if (skillTable.containsKey(key)) {
			sb.append("<p>");
			sb.append("Key: \"" + key + "\", UUID: \"" + skillTable.get(key).getUUID() + "\", skillIri: \""
					+ skillTable.get(key).getSkillIri() + "\", State: \"" + skillTable.get(key).getState() + "\"");
			sb.append("</p>");

			sb.append("<p>");
			sb.append("You can check the skill again to see if you were successful.");
			sb.append("</p>");

			// actually hold the skill
			skillTable.get(key).hold();

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

	@POST
	@Path("{uid}/unhold")
	@Produces(MediaType.TEXT_HTML)
	public String unhold(@PathParam("uid") String uid) {
		UUID key = UUID.fromString(uid);
		logger.info(getClass().getSimpleName() + ": RestSkill # " + key + " UNHOLD page called!");

		StringBuilder sb = new StringBuilder("<html><body>");
		sb.append("<h1>Simple RESTful OSGi StateMachine</h1>");
		sb.append("<h2>You called the UNHOLD page for RestSkill # " + key + "</h2>");

		if (skillTable.containsKey(key)) {
			sb.append("<p>");
			sb.append("Key: \"" + key + "\", UUID: \"" + skillTable.get(key).getUUID() + "\", skillIri: \""
					+ skillTable.get(key).getSkillIri() + "\", State: \"" + skillTable.get(key).getState() + "\"");
			sb.append("</p>");

			sb.append("<p>");
			sb.append("You can check the skill again to see if you were successful.");
			sb.append("</p>");

			// actually unhold the skill
			skillTable.get(key).unhold();

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

	@POST
	@Path("{uid}/suspend")
	@Produces(MediaType.TEXT_HTML)
	public String suspend(@PathParam("uid") String uid) {
		UUID key = UUID.fromString(uid);
		logger.info(getClass().getSimpleName() + ": RestSkill # " + key + " SUSPEND page called!");

		StringBuilder sb = new StringBuilder("<html><body>");
		sb.append("<h1>Simple RESTful OSGi StateMachine</h1>");
		sb.append("<h2>You called the SUSPEND page for RestSkill # " + key + "</h2>");

		if (skillTable.containsKey(key)) {
			sb.append("<p>");
			sb.append("Key: \"" + key + "\", UUID: \"" + skillTable.get(key).getUUID() + "\", skillIri: \""
					+ skillTable.get(key).getSkillIri() + "\", State: \"" + skillTable.get(key).getState() + "\"");
			sb.append("</p>");

			sb.append("<p>");
			sb.append("You can check the skill again to see if you were successful.");
			sb.append("</p>");

			// actually suspend the skill
			skillTable.get(key).suspend();

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

	@POST
	@Path("{uid}/unsuspend")
	@Produces(MediaType.TEXT_HTML)
	public String unsuspend(@PathParam("uid") String uid) {
		UUID key = UUID.fromString(uid);
		logger.info(getClass().getSimpleName() + ": RestSkill # " + key + " UNSUSPEND page called!");

		StringBuilder sb = new StringBuilder("<html><body>");
		sb.append("<h1>Simple RESTful OSGi StateMachine</h1>");
		sb.append("<h2>You called the UNSUSPEND page for RestSkill # " + key + "</h2>");

		if (skillTable.containsKey(key)) {
			sb.append("<p>");
			sb.append("Key: \"" + key + "\", UUID: \"" + skillTable.get(key).getUUID() + "\", skillIri: \""
					+ skillTable.get(key).getSkillIri() + "\", State: \"" + skillTable.get(key).getState() + "\"");
			sb.append("</p>");

			sb.append("<p>");
			sb.append("You can check the skill again to see if you were successful.");
			sb.append("</p>");

			// actually unsuspend the skill
			skillTable.get(key).unsuspend();

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

	@POST
	@Path("{uid}/stop")
	@Produces(MediaType.TEXT_HTML)
	public String stop(@PathParam("uid") String uid) {
		UUID key = UUID.fromString(uid);
		logger.info(getClass().getSimpleName() + ": RestSkill # " + key + " STOP page called!");

		StringBuilder sb = new StringBuilder("<html><body>");
		sb.append("<h1>Simple RESTful OSGi StateMachine</h1>");
		sb.append("<h2>You called the STOP page for RestSkill # " + key + "</h2>");

		if (skillTable.containsKey(key)) {
			sb.append("<p>");
			sb.append("Key: \"" + key + "\", UUID: \"" + skillTable.get(key).getUUID() + "\", skillIri: \""
					+ skillTable.get(key).getSkillIri() + "\", State: \"" + skillTable.get(key).getState() + "\"");
			sb.append("</p>");

			sb.append("<p>");
			sb.append("You can check the skill again to see if you were successful.");
			sb.append("</p>");

			// actually stop the skill
			skillTable.get(key).stop();

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

	@POST
	@Path("{uid}/abort")
	@Produces(MediaType.TEXT_HTML)
	public String abort(@PathParam("uid") String uid) {
		UUID key = UUID.fromString(uid);
		logger.info(getClass().getSimpleName() + ": RestSkill # " + key + " ABORT page called!");

		StringBuilder sb = new StringBuilder("<html><body>");
		sb.append("<h1>Simple RESTful OSGi StateMachine</h1>");
		sb.append("<h2>You called the ABORT page for RestSkill # " + key + "</h2>");

		if (skillTable.containsKey(key)) {
			sb.append("<p>");
			sb.append("Key: \"" + key + "\", UUID: \"" + skillTable.get(key).getUUID() + "\", skillIri: \""
					+ skillTable.get(key).getSkillIri() + "\", State: \"" + skillTable.get(key).getState() + "\"");
			sb.append("</p>");

			sb.append("<p>");
			sb.append("You can check the skill again to see if you were successful.");
			sb.append("</p>");

			// actually abort the skill
			skillTable.get(key).abort();

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

	@POST
	@Path("{uid}/clear")
	@Produces(MediaType.TEXT_HTML)
	public String clear(@PathParam("uid") String uid) {
		UUID key = UUID.fromString(uid);
		logger.info(getClass().getSimpleName() + ": RestSkill # " + key + " CLEAR page called!");

		StringBuilder sb = new StringBuilder("<html><body>");
		sb.append("<h1>Simple RESTful OSGi StateMachine</h1>");
		sb.append("<h2>You called the CLEAR page for RestSkill # " + key + "</h2>");

		if (skillTable.containsKey(key)) {
			sb.append("<p>");
			sb.append("Key: \"" + key + "\", UUID: \"" + skillTable.get(key).getUUID() + "\", skillIri: \""
					+ skillTable.get(key).getSkillIri() + "\", State: \"" + skillTable.get(key).getState() + "\"");
			sb.append("</p>");

			sb.append("<p>");
			sb.append("You can check the skill again to see if you were successful.");
			sb.append("</p>");

			// actually clear the skill
			skillTable.get(key).clear();

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