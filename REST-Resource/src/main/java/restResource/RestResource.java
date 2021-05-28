package restResource;

import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.HashMap;

import javax.json.Json;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.ws.rs.Consumes;
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

import com.google.gson.Gson;

import skillup.annotations.Skill;
import skillup.annotations.SkillOutput;
import skillup.annotations.SkillParameter;
import statemachine.Isa88StateMachine;
import states.TransitionName;

@Component(immediate = true, service = RestResource.class)
@JaxrsResource
@Path("skills")
public class RestResource {

	private static Logger logger;
	private HashMap<String, RestSkill> skillDirectory;

	public RestResource() {
		this.skillDirectory = new HashMap<String, RestSkill>();
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
		RestSkill newSkill = new RestSkill(stateMachine, skill);
		skillDirectory.put(skill.getClass().getAnnotation(Skill.class).skillIri(), newSkill);
	}

	public RestSkill getRestSkillBySkillObject(Object skillObject) {

		for (String key : skillDirectory.keySet()) {
			if (skillDirectory.get(key).getSkillObject().equals(skillObject)) {
				logger.info(getClass().getSimpleName() + ": Found skill in skill directory (" + key + ")");
				return skillDirectory.get(key);
			}
		}
		return null;
	}

	public void deleteSkill(Object skill) {
		logger.info(getClass().getSimpleName() + ": Deleting skill \"" + skill.toString() + "\"...");

		RestSkill restSkill = getRestSkillBySkillObject(skill);
		skillDirectory.remove(restSkill.getSkillIri());
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response landing() {
		logger.info(getClass().getSimpleName() + ": Landing page called!");

		JsonObjectBuilder objBuilder = Json.createObjectBuilder();

		for (String key : skillDirectory.keySet()) {
			objBuilder.add(key, Json.createObjectBuilder().add("state", skillDirectory.get(key).getState()));
		}

		String responseString = objBuilder.build().toString();
		// This may return an empty JSON object (with status code OK)!
		return Response.status(Response.Status.OK).entity(responseString).build();
	}

	@GET
	@Path("{skillIri}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response info(@PathParam("skillIri") String skillIri) {

		RestSkill skill = skillDirectory.get(skillIri);
		JsonObject obj = Json.createObjectBuilder()
				.add(skill.getSkillIri(), Json.createObjectBuilder().add("state", skill.getState())).build();

		String responseString = obj.toString();
		return Response.status(Response.Status.OK).entity(responseString).build();

	}

	@GET
	@Path("{skillIri}/skillOutputs")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSkillOutputs(@PathParam("skillIri") String skillIri) {

		RestSkill skill = skillDirectory.get(skillIri);
		// found the correct skill
		String json = null;
		// check every field of the skillObject for SkillOutput-Annotation
		Field[] fields = skill.getSkillObject().getClass().getDeclaredFields();
		for (Field field : fields) {
			if (field.isAnnotationPresent(SkillOutput.class)) {
				field.setAccessible(true);
				SkillOutput annotation = field.getAnnotation(SkillOutput.class);
				Gson gson = new Gson();
				SkillVariable output;
				try {
					output = new SkillVariable(annotation.name(), annotation.description(), annotation.isRequired(),
							field.getType().getSimpleName(), field.get(skill.getSkillObject()));
					json += gson.toJson(output);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		logger.info("Get Outputs of " + skillIri + ": " + json);
		return Response.status(Response.Status.OK).entity(json).build();
	}

	@GET
	@Path("{skillIri}/skillParameters")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSkillParameters(@PathParam("skillIri") String skillIri) {

		RestSkill skill = skillDirectory.get(skillIri);
		// found the correct skill
		String json = null;
		// check every field of the skillObject for SkillOutput-Annotation
		Field[] fields = skill.getSkillObject().getClass().getDeclaredFields();
		for (Field field : fields) {
			if (field.isAnnotationPresent(SkillParameter.class)) {
				field.setAccessible(true);
				SkillParameter annotation = field.getAnnotation(SkillParameter.class);
				Gson gson = new Gson();
				SkillVariable parameter;
				try {
					parameter = new SkillVariable(annotation.name(), annotation.description(), annotation.isRequired(),
							field.getType().getSimpleName(), field.get(skill.getSkillObject()));
					json += gson.toJson(parameter);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		logger.info("Get Parameter of " + skillIri + ": " + json);
		return Response.status(Response.Status.OK).entity(json).build();
	}

	// Caution: Matches the SkillParams by Cap:hasVariableName
	// Example Input = { "i": "HelloParam", "j": 42 }
	// Able to process:
	// Strings
	// Doubles
	// Ints
	// Booleans
	// TODO: Better way?
	@POST
	@Path("{skillIri}/skillParameters")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response setSkillParameters(@PathParam("skillIri") String skillIri, String body) {

		JsonObjectBuilder objBuilder = setSkillParameter(skillIri, body);

		String responseString = objBuilder.build().toString();
		return Response.status(Response.Status.OK).entity(responseString).build();
	}

	@POST
	@Path("{skillIri}/{transition}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response transition(@PathParam("skillIri") String skillIri, @PathParam("transition") String transition,
			String body) {

		RestSkill skill = skillDirectory.get(skillIri);
		// we found the correct skill by skillIri
		setSkillParameter(skillIri, body);
		for (TransitionName transitions : TransitionName.values()) {
			if (transition.equals(transitions.toString())) {
				skill.fireTransition(transitions);
				break;
			}
		}
		JsonObject obj = Json.createObjectBuilder()
				.add(skill.getSkillIri(),
						Json.createObjectBuilder().add("state", skill.getState()).add("skillIri", skill.getSkillIri()))
				.build();

		String responseString = obj.toString();
		return Response.status(Response.Status.OK).entity(responseString).build();
	}

	private JsonObjectBuilder setSkillParameter(String skillIri, String body) {

		JsonReader jsonReader = Json.createReader(new StringReader(body));
		JsonObject jobj = jsonReader.readObject();

		// idea: We iterate over all fields of the skill that matches the skillIri
		// check if the field is annotated with @SkillParameter
		// for every annotated field we iterate through JSON object
		// and check if the jsonKey matches with the variableName
		// match => update the variable's value

		// may need to do some casting magic...

		JsonObjectBuilder objBuilder = Json.createObjectBuilder();

		RestSkill skill = skillDirectory.get(skillIri);

		// we found the correct skill by UUID
		Field[] fields = skill.getSkillObject().getClass().getDeclaredFields();
		for (Field field : fields) {
			if (field.isAnnotationPresent(SkillParameter.class)) {
				field.setAccessible(true);
				// we found a SkillParameter
				// now go through all sent Params in JSON object
				// and see if there is match (variableName and JSON key)
				for (String jsonKey : jobj.keySet()) {
					if (jsonKey.equals(field.getName())) {
						// variableName matches JSON key
						// -> update that variable's value!
						logger.info(getClass().getSimpleName() + ": UPDATE SKILL PARAM (" + field.getName() + " to "
								+ jobj.get(jsonKey).toString() + ")");
						boolean success = updateSkillParam(skill.getSkillObject(), field, jobj.get(jsonKey));
						objBuilder.add(jsonKey, success);
						// break because we updated that field!
						break;
					}
				}
			}
		}
		return objBuilder;
	}

	// Idea: we cast any JsonValue to a String and then convert that String to the
	// Field's Type!
	private boolean updateSkillParam(Object skillObject, Field toSet, JsonValue jsonValue) {
		if (toSet.getType().equals(Integer.class) || toSet.getType().equals(int.class)) {
			logger.info("INTEGER");
			if (jsonValue instanceof JsonNumber) {
				try {
					toSet.setInt(skillObject, Integer.valueOf(((JsonNumber) jsonValue).toString()));
					return true;
				} catch (IllegalArgumentException | IllegalAccessException e) {
					logger.error("Error while casting JsonValue to Integer");
					logger.error(e.toString());
					return false;
				}
			} else {
				logger.error("JsonValue not instanceof JsonNumber!");
				return false;
			}
		}
		if (toSet.getType().equals(Double.class) || toSet.getType().equals(double.class)) {
			logger.info("DOUBLE");
			if (jsonValue instanceof JsonNumber) {
				try {
					toSet.setDouble(skillObject, Double.valueOf(((JsonNumber) jsonValue).toString()));
					return true;
				} catch (IllegalArgumentException | IllegalAccessException e) {
					logger.error("Error while casting JsonValue to Double");
					logger.error(e.toString());
					return false;
				}
			} else {
				logger.error("JsonValue not instanceof JsonNumber!");
				return false;
			}
		}
		if (toSet.getType().equals(Boolean.class) || toSet.getType().equals(boolean.class)) {
			logger.info("BOOLEAN");
			if (jsonValue == JsonValue.TRUE || jsonValue == JsonValue.FALSE) {
				try {
					toSet.setBoolean(skillObject, Boolean.getBoolean(jsonValue.toString()));
					return true;
				} catch (IllegalArgumentException | IllegalAccessException e) {
					logger.error("Error while casting JsonValue to Boolean");
					logger.error(e.toString());
					return false;
				}
			} else {
				logger.error("JsonValue not a Boolean!");
				return false;
			}
		}
		if (toSet.getType().equals(String.class)) {
			logger.info("STRING");
			if (jsonValue.getValueType() == JsonValue.ValueType.STRING) {
				try {
					String value = jsonValue.toString();
					// we strip first and last index to remove quotes!
					value = value.substring(1, value.length() - 1);
					toSet.set(skillObject, value);
					return true;
				} catch (IllegalArgumentException | IllegalAccessException e) {
					logger.error("Error while casting JsonValue to String");
					logger.error(e.toString());
					return false;
				}
			} else {
				logger.error("JsonValue not a String!");
				return false;
			}
		}
		logger.error("End of updateSkillParam -> unknown Datatype?");
		return false;
	}
}