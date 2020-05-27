package annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import enums.DIN8580;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Skill {

	public String value() default "OpcUaSkill"; 
	public String namespace(); 
	public String capabilityName() default ""; 
	public DIN8580 capabilityType() default DIN8580.None; 
}
