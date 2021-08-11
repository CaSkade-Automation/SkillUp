package skillup.annotations;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Helper {

	public List<Field> getVariables(Object skill, boolean skillParameter) {
		Field[] fieldArray = skill.getClass().getDeclaredFields();
		List<Field> fields = Arrays.asList(fieldArray);

		List<Field> variableFields;

		if (skillParameter) {
			variableFields = fields.stream().filter(field -> field.isAnnotationPresent(SkillParameter.class))
					.collect(Collectors.toList());
		} else {
			variableFields = fields.stream().filter(field -> field.isAnnotationPresent(SkillOutput.class))
					.collect(Collectors.toList());
		}

		return variableFields;
	}
}
