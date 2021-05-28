package restResource;

import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.ArrayList;

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

import skillup.annotations.SkillOutput;
import skillup.annotations.SkillParameter;
import statemachine.Isa88StateMachine;
import states.TransitionName;

@Component(immediate = true, service = RestResource.class)
@JaxrsResource
@Path("skills")
public class RestResource {

	private static Logger logger;
	private ArrayList<RestSkill> skillDirectory;

	public RestResource() {
		this.skillDirectory = new ArrayList<RestSkill>();
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
		skillDirectory.add(newSkill);
	}

	public RestSkill getRestSkillBySkillObject(Object skillObject) {
		for (RestSkill rSkill : skillDirectory) {
			if (rSkill.getSkillObject().equals(skillObject)) {
				return rSkill;
			}
		}
		return null;
	}

	public void deleteSkill(Object skill) {
		logger.info(getClass().getSimpleName() + ": Deleting skill \"" + skill.toString() + "\"...");

		for (RestSkill rSkill : skillDirectory) {
			if (rSkill.getSkillObject().equals(skill)) {
				logger.info(
						getClass().getSimpleName() + ": Found skill in skill directory (" + rSkill.getSkillIri() + ")");
				skillDirectory.remove(rSkill);
			}
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response landing() {
		logger.info(getClass().getSimpleName() + ": Landing page called!");

		JsonObjectBuilder objBuilder = Json.createObjectBuilder();

		for (RestSkill rSkill : skillDirectory) {
			objBuilder.add(rSkill.getSkillIri(), Json.createObjectBuilder().add("state", rSkill.getState()));
		}

		String responseString = objBuilder.build().toString();
		// This may return an empty JSON object (with status code OK)!
		return Response.status(Response.Status.OK).entity(responseString).build();
	}

	@GET
	@Path("{skillIri}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response info(@PathParam("skillIri") String skillIri) {

		for (RestSkill rSkill : skillDirectory) {
			if (rSkill.getSkillIri().equals(skillIri)) {
				JsonObject obj = Json.createObjectBuilder()
						.add(rSkill.getSkillIri(), Json.createObjectBuilder().add("state", rSkill.getState())).build();

				String responseString = obj.toString();
				return Response.status(Response.Status.OK).entity(responseString).build();
			}
		}

		// no match found!
		return Response.status(Response.Status.NOT_FOUND).build();
	}

	@GET
	@Path("{skillIri}/skillOutputs")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSkillOutputs(@PathParam("skillIri") String skillIri) {

		for (RestSkill rSkill : skillDirectory) {
			if (rSkill.getSkillIri().equals(skillIri)) {
				// found the correct skill
				JsonObjectBuilder objBuilder = Json.createObjectBuilder();
				// check every field of the skillObject for SkillOutput-Annotation
				Field[] fields = rSkill.getSkillObject().getClass().getDeclaredFields();
				for (Field field : fields) {
					if (field.isAnnotationPresent(SkillOutput.class)) {
						field.setAccessible(true);
						JsonObjectBuilder jsonField = Json.createObjectBuilder();
						jsonField.add("name", field.getAnnotation(SkillOutput.class).name())
								.add("type", field.getType().getSimpleName())
								.add("isRequired",
										Boolean.toString(field.getAnnotation(SkillOutput.class).isRequired()))
								.add("description", field.getAnnotation(SkillOutput.class).description());
						try {
							String fieldValue = field.get(rSkill.getSkillObject()).toString();
							jsonField.add("value", fieldValue);
						} catch (IllegalArgumentException e) {
							logger.error(getClass().getSimpleName()
									+ ": ERR on generating skillOutput (value of output, IllegalArgumentException)"
									+ e);
						} catch (IllegalAccessException e) {
							logger.error(getClass().getSimpleName()
									+ ": ERR on generating skillOutput (value of output, IllegalAccessException)" + e);
						} catch (NullPointerException e) {
							logger.error(getClass().getSimpleName()
									+ ": ERR on generating skillOutput (value of output, NullPointerException)" + e);
							jsonField.add("value", "null");
						}
						objBuilder.add(field.getName(), jsonField);
					}
				}

				String responseString = objBuilder.build().toString();
				return Response.status(Response.Status.OK).entity(responseString).build();
			}
		}

		// no match found!
		return Response.status(Response.Status.NOT_FOUND).build();
	}

	@GET
	@Path("{skillIri}/skillParameters")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSkillParameters(@PathParam("skillIri") String skillIri) {

		for (RestSkill rSkill : skillDirectory) {
			if (rSkill.getSkillIri().equals(skillIri)) {
				// found the correct skill
				JsonObjectBuilder objBuilder = Json.createObjectBuilder();
				// check every field of the skillObject for SkillOutput-Annotation
				Field[] fields = rSkill.getSkillObject().getClass().getDeclaredFields();
				for (Field field : fields) {
					if (field.isAnnotationPresent(SkillParameter.class)) {
						field.setAccessible(true);
						JsonObjectBuilder jsonField = Json.createObjectBuilder();
						jsonField.add("name", field.getAnnotation(SkillParameter.class).name())
								.add("type", field.getType().getSimpleName())
								.add("isRequired",
										Boolean.toString(field.getAnnotation(SkillParameter.class).isRequired()))
								.add("description", field.getAnnotation(SkillParameter.class).description());
						try {
							String fieldValue = field.get(rSkill.getSkillObject()).toString();
							jsonField.add("value", fieldValue);
						} catch (IllegalArgumentException e) {
							logger.error(getClass().getSimpleName()
									+ ": ERR on generating skillParameter (value of param, IllegalArgumentException)"
									+ e);
						} catch (IllegalAccessException e) {
							logger.error(getClass().getSimpleName()
									+ ": ERR on generating skillParameter (value of param, IllegalAccessException)"
									+ e);
						} catch (NullPointerException e) {
							logger.error(getClass().getSimpleName()
									+ ": ERR on generating skillParameter (value of param, NullPointerException)" + e);
							jsonField.add("value", "null");
						}
						objBuilder.add(field.getName(), jsonField);
					}
				}

				String responseString = objBuilder.build().toString();
				return Response.status(Response.Status.OK).entity(responseString).build();
			}
		}

		// no match found!
		return Response.status(Response.Status.NOT_FOUND).build();
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

		for (RestSkill rSkill : skillDirectory) {
			if (rSkill.getSkillIri().equals(skillIri)) {

				// we found the correct skill by skillIri
				setSkillParameter(skillIri, body);
				for (TransitionName transitions : TransitionName.values()) {
					if (transition.equals(transitions.toString())) {
						rSkill.fireTransition(transitions);
						break;
					}
				}
				JsonObject obj = Json.createObjectBuilder().add(rSkill.getSkillIri(), Json.createObjectBuilder()
						.add("state", rSkill.getState()).add("skillIri", rSkill.getSkillIri())).build();

				String responseString = obj.toString();
				return Response.status(Response.Status.OK).entity(responseString).build();
			}
		}

		// no match found!
		return Response.status(Response.Status.NOT_FOUND).build();
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

		for (RestSkill rSkill : skillDirectory) {
			if (rSkill.getSkillIri().equals(skillIri)) {
				// we found the correct skill by UUID
				Field[] fields = rSkill.getSkillObject().getClass().getDeclaredFields();
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
								logger.info(getClass().getSimpleName() + ": UPDATE SKILL PARAM (" + field.getName()
										+ " to " + jobj.get(jsonKey).toString() + ")");
								boolean success = updateSkillParam(rSkill.getSkillObject(), field, jobj.get(jsonKey));
								objBuilder.add(jsonKey, success);
								// break because we updated that field!
								break;
							}
						}
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