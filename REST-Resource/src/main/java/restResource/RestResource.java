package restResource;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
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
import com.google.gson.reflect.TypeToken;

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
		// check every field of the skillObject for SkillOutput-Annotation
		Field[] fieldArray = skill.getSkillObject().getClass().getDeclaredFields();
		List<Field> fields = Arrays.asList(fieldArray);

		List<Field> paramFields = fields.stream().filter(field -> field.isAnnotationPresent(SkillOutput.class))
				.collect(Collectors.toList());

		List<SkillVariable> skillVariables = new ArrayList<SkillVariable>();

		for (Field field : paramFields) {
			field.setAccessible(true);
			SkillOutput annotation = field.getAnnotation(SkillOutput.class);
			SkillVariable output;
			try {
				output = new SkillVariable(annotation.name(), annotation.description(), annotation.isRequired(),
						field.getType().getSimpleName(), field.get(skill.getSkillObject()));
				skillVariables.add(output);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NullPointerException e) {
				output = new SkillVariable(annotation.name(), annotation.description(), annotation.isRequired(),
						field.getType().getSimpleName(), null);
			}
		}

		Gson gson = new Gson();
		String json = gson.toJson(skillVariables);
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
				} catch (NullPointerException e) {
					parameter = new SkillVariable(annotation.name(), annotation.description(), annotation.isRequired(),
							field.getType().getSimpleName(), null);
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

		RestSkill skill = skillDirectory.get(skillIri);

		Gson gson = new Gson();

		Type listType = new TypeToken<ArrayList<SkillVariable>>() {
		}.getType();

		ArrayList<SkillVariable> skillVariables = gson.fromJson(body, listType);

		setSkillParameter(skill, skillVariables);

//		String responseString = objBuilder.build().toString();
		return Response.status(Response.Status.OK).build();
	}

	@POST
	@Path("{skillIri}/{transition}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response transition(@PathParam("skillIri") String skillIri, @PathParam("transition") String transition,
			String body) {

		RestSkill skill = skillDirectory.get(skillIri);
		// we found the correct skill by skillIri

		Gson gson = new Gson();

		Type listType = new TypeToken<ArrayList<SkillVariable>>() {
		}.getType();

		ArrayList<SkillVariable> skillVariables = gson.fromJson(body, listType);

		setSkillParameter(skill, skillVariables);
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

	private void setSkillParameter(RestSkill skill, ArrayList<SkillVariable> skillVariables) {

		// idea: We iterate over all fields of the skill that matches the skillIri
		// check if the field is annotated with @SkillParameter
		// for every annotated field we iterate through JSON object
		// and check if the jsonKey matches with the variableName
		// match => update the variable's value

		// may need to do some casting magic...

		// we found the correct skill by UUID
		Field[] fieldArray = skill.getSkillObject().getClass().getDeclaredFields();
		List<Field> fields = Arrays.asList(fieldArray);

		List<Field> paramFields = fields.stream().filter(field -> field.isAnnotationPresent(SkillParameter.class))
				.collect(Collectors.toList());

		for (Field paramField : paramFields) {
			paramField.setAccessible(true);
			// we found a SkillParameter
			// now go through all sent Params in JSON object
			// and see if there is match (variableName and JSON key)

			SkillVariable variable = skillVariables.stream()
					.filter(skillVar -> skillVar.getName().equals(paramField.getName())).findFirst().get();

			logger.info(getClass().getSimpleName() + ": UPDATE SKILL PARAM (" + paramField.getName() + " to "
					+ variable.getValue().toString() + ")");
			updateSkillParam(skill.getSkillObject(), paramField, variable.getValue());
		}
	}

	// Idea: we cast any JsonValue to a String and then convert that String to the
	// Field's Type!
	private void updateSkillParam(Object skillObject, Field fieldToSet, Object newValue) {
		try {
			fieldToSet.set(skillObject, newValue);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		if (fieldToSet.getType().equals(Integer.class) || fieldToSet.getType().equals(int.class)) {
//			logger.info("INTEGER");
//			if (newValue.) {
//				try {
//					fieldToSet.setInt(skillObject, Integer.valueOf(((JsonNumber) newValue).toString()));
//					return true;
//				} catch (IllegalArgumentException | IllegalAccessException e) {
//					logger.error("Error while casting JsonValue to Integer");
//					logger.error(e.toString());
//					return false;
//				}
//			} else {
//				logger.error("JsonValue not instanceof JsonNumber!");
//				return false;
//			}
//		}
//		if (fieldToSet.getType().equals(Double.class) || fieldToSet.getType().equals(double.class)) {
//			logger.info("DOUBLE");
//			if (newValue instanceof JsonNumber) {
//				try {
//					fieldToSet.setDouble(skillObject, Double.valueOf(((JsonNumber) newValue).toString()));
//					return true;
//				} catch (IllegalArgumentException | IllegalAccessException e) {
//					logger.error("Error while casting JsonValue to Double");
//					logger.error(e.toString());
//					return false;
//				}
//			} else {
//				logger.error("JsonValue not instanceof JsonNumber!");
//				return false;
//			}
//		}
//		if (fieldToSet.getType().equals(Boolean.class) || fieldToSet.getType().equals(boolean.class)) {
//			logger.info("BOOLEAN");
//			if (newValue == JsonValue.TRUE || newValue == JsonValue.FALSE) {
//				try {
//					fieldToSet.setBoolean(skillObject, Boolean.getBoolean(newValue.toString()));
//					return true;
//				} catch (IllegalArgumentException | IllegalAccessException e) {
//					logger.error("Error while casting JsonValue to Boolean");
//					logger.error(e.toString());
//					return false;
//				}
//			} else {
//				logger.error("JsonValue not a Boolean!");
//				return false;
//			}
//		}
//		if (fieldToSet.getType().equals(String.class)) {
//			logger.info("STRING");
//			if (newValue.getValueType() == JsonValue.ValueType.STRING) {
//				try {
//					String value = newValue.toString();
//					// we strip first and last index to remove quotes!
//					value = value.substring(1, value.length() - 1);
//					fieldToSet.set(skillObject, value);
//					return true;
//				} catch (IllegalArgumentException | IllegalAccessException e) {
//					logger.error("Error while casting JsonValue to String");
//					logger.error(e.toString());
//					return false;
//				}
//			} else {
//				logger.error("JsonValue not a String!");
//				return false;
//			}
//		}
//		logger.error("End of updateSkillParam -> unknown Datatype?");
//		return false;
	}
}