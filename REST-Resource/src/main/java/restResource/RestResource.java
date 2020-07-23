package restResource;

import java.util.HashMap;
import java.util.Set;

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

import statemachine.StateMachine;

@Component(immediate = true, service = RestResource.class)
@JaxrsResource
@Path("skills")
public class RestResource {

	private static Logger logger = LoggerFactory.getLogger(RestResource.class);
	private HashMap<Integer, RestSkill> skillTable = new HashMap<Integer, RestSkill>();

	@Activate
	public void activate() {
		logger.info("Activating " + getClass().getSimpleName());
	}

	@Deactivate
	public void deactivate() {
		logger.info("Deactivating " + getClass().getSimpleName());
	}

	public void generateSkill(Object skill, StateMachine stateMachine) {
		logger.info(getClass().getSimpleName() + ": Generating new RestSkill");
		RestSkill newSkill = new RestSkill(stateMachine);
		skillTable.put(newSkill.getInstanceNo(), newSkill);
	}

	// Does not work yet. how to identify skill?
	// integers would be nice. compared to opcua skills. to go only by
	// names...difficult
	// how to make sure there are no duplicates?
	public void deleteSkill(Object skill) {
		logger.info(getClass().getSimpleName() + ": Deleting RestSkill \"" + skill.getClass().toString() + "\"");
		logger.info("Deletion of RestSkills not yet implemented!");
		// String skillname = skill.getClass().getSimpleName();
		// object hat skill-annotation und dann über die uri in der annotation gehen.
		// is annotation present von skill object machen...

	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	public String landing() {
		logger.info(getClass().getSimpleName() + ": Landing page called!");

		StringBuilder sb = new StringBuilder("<html><body>");
		sb.append("<h1>Simple RESTful OSGi StateMachine</h1>");
		sb.append("<h2>These are all available RestSkills</h2>");

		Set<Integer> setOfKeys = skillTable.keySet();
		for (Integer key : setOfKeys) {
			sb.append("<p>");
			sb.append("Key: \"" + key + "\", InstanceNo: \"" + skillTable.get(key).getInstanceNo() + "\", State: \""
					+ skillTable.get(key).getState() + "\"");
			sb.append("</p>");
		}

		sb.append("</body></html>");
		return sb.toString();
	}

	@GET
	@Path("{instanceNo}")
	@Produces(MediaType.TEXT_HTML)
	public String info(@PathParam("instanceNo") String instance) {
		logger.info(getClass().getSimpleName() + ": RestSkill # " + instance + " info page called!");

		StringBuilder sb = new StringBuilder("<html><body>");
		sb.append("<h1>Simple RESTful OSGi StateMachine</h1>");
		sb.append("<h2>You called the info page for RestSkill # " + instance + "</h2>");

		if (!isInteger(instance)) {
			sb.append("<p>");
			sb.append("InstanceNo: \"" + instance + "\" is not a valid instance number. (NaN)");
			sb.append("</p>");
		} else {
			if (Integer.parseInt(instance) < 0) {
				sb.append("<p>");
				sb.append("InstanceNo: \"" + instance
						+ "\" is not a valid instance number. (Instance numbers must be >= 0)");
				sb.append("</p>");
			} else {
				int key = Integer.parseInt(instance);

				if (skillTable.containsKey(key)) {
					// key exists
					sb.append("<p>");
					sb.append("Key: \"" + key + "\", InstanceNo: \"" + skillTable.get(key).getInstanceNo()
							+ "\", State: \"" + skillTable.get(key).getState() + "\"");
					sb.append("</p>");
				} else {
					sb.append("<p>");
					sb.append("InstanceNo: \"" + instance + "\" is not a valid instance number. (Instance not found!)");
					sb.append("</p>");
				}
			}
		}

		sb.append("</body></html>");
		return sb.toString();
	}

	@GET
	@Path("{instanceNo}/start")
	@Produces(MediaType.TEXT_HTML)
	public String start(@PathParam("instanceNo") String instance) {
		logger.info(getClass().getSimpleName() + ": RestSkill # " + instance + " START page called!");

		StringBuilder sb = new StringBuilder("<html><body>");
		sb.append("<h1>Simple RESTful OSGi StateMachine</h1>");
		sb.append("<h2>You called the START page for RestSkill # " + instance + "</h2>");

		if (!isInteger(instance)) {
			sb.append("<p>");
			sb.append("InstanceNo: \"" + instance + "\" is not a valid instance number. (NaN)");
			sb.append("</p>");
		} else {
			if (Integer.parseInt(instance) < 0) {
				sb.append("<p>");
				sb.append("InstanceNo: \"" + instance
						+ "\" is not a valid instance number. (Instance numbers must be >= 0)");
				sb.append("</p>");
			} else {
				int key = Integer.parseInt(instance);

				if (skillTable.containsKey(key)) {
					sb.append("<p>");
					sb.append("Key: \"" + key + "\", InstanceNo: \"" + skillTable.get(key).getInstanceNo()
							+ "\", State: \"" + skillTable.get(key).getState() + "\"");
					sb.append("</p>");

					sb.append("<p>");
					sb.append(
							"We are trying to START the skill now! You can check the landing page of the skill to see if you were successful.");
					sb.append("</p>");

					skillTable.get(key).start();

					sb.append("<p>");
					sb.append("http://localhost:8181/skills/" + instance);
					sb.append("</p>");
				} else {
					sb.append("<p>");
					sb.append("InstanceNo: \"" + instance + "\" is not a valid instance number. (Instance not found!)");
					sb.append("</p>");
				}
			}
		}

		sb.append("</body></html>");
		return sb.toString();
	}

	@GET
	@Path("{instanceNo}/reset")
	@Produces(MediaType.TEXT_HTML)
	public String reset(@PathParam("instanceNo") String instance) {
		logger.info(getClass().getSimpleName() + ": RestSkill # " + instance + " RESET page called!");

		StringBuilder sb = new StringBuilder("<html><body>");
		sb.append("<h1>Simple RESTful OSGi StateMachine</h1>");
		sb.append("<h2>You called the RESET page for RestSkill # " + instance + "</h2>");

		if (!isInteger(instance)) {
			sb.append("<p>");
			sb.append("InstanceNo: \"" + instance + "\" is not a valid instance number. (NaN)");
			sb.append("</p>");
		} else {
			if (Integer.parseInt(instance) < 0) {
				sb.append("<p>");
				sb.append("InstanceNo: \"" + instance
						+ "\" is not a valid instance number. (Instance numbers must be >= 0)");
				sb.append("</p>");
			} else {
				int key = Integer.parseInt(instance);

				if (skillTable.containsKey(key)) {
					sb.append("<p>");
					sb.append("Key: \"" + key + "\", InstanceNo: \"" + skillTable.get(key).getInstanceNo()
							+ "\", State: \"" + skillTable.get(key).getState() + "\"");
					sb.append("</p>");

					sb.append("<p>");
					sb.append(
							"We are trying to RESET the skill now! You can check the landing page of the skill to see if you were successful.");
					sb.append("</p>");

					skillTable.get(key).reset();

					sb.append("<p>");
					sb.append("http://localhost:8181/skills/" + instance);
					sb.append("</p>");
				} else {
					sb.append("<p>");
					sb.append("InstanceNo: \"" + instance + "\" is not a valid instance number. (Instance not found!)");
					sb.append("</p>");
				}
			}
		}

		sb.append("</body></html>");
		return sb.toString();
	}

	public boolean isInteger(String string) {
		// TODO: make one large Check method that checks if instance is an int/is a
		// valid int/is a valid instance -> only one check!
		try {
			Integer.valueOf(string);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

}