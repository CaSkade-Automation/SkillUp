package skillup.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Skill Annotation needs skillIRI, moduleIRI and skill type which is an class
 * that extends from class SkillType. CapabilityIRI and description are
 * optional.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Skill {

	public Class<? extends SkillType> type();

	public String description() default "";

	public String skillIri();

	public String moduleIri();

	public String capabilityIri() default "";
}
