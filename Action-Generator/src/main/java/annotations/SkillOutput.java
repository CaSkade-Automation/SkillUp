package annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * SkillOutput annotation can have a name and description. Necessary is a
 * boolean value for isRequired which tells if this parameter is essential for
 * executing a skill method
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SkillOutput {

	public String name() default "";

	public String description() default "";

	public boolean isRequired();
}
