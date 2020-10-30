package annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation Module needs a moduleIRI. Description and capabilityIRI are
 * optional.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Module {

	public String description() default "";

	public String moduleIri();

	public String capabilityIri() default "";
}
