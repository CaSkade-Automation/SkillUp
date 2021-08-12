package skillup.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * SkillParameter annotation can have a name, description and option. Necessary
 * is a boolean value for isRequired which tells if this parameter is essential
 * for executing a skill method
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SkillParameter {

	public String name() default "";

	public String description() default "";

	public boolean isRequired();

	public String[] option() default "";
}
