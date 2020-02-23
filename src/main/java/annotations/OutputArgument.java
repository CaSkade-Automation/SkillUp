package annotations;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.eclipse.milo.opcua.sdk.core.ValueRank;

@Retention(RetentionPolicy.RUNTIME)
@Repeatable(value = OutputArguments.class)
public @interface OutputArgument {
	
	String name();
	UaTypes dataType(); 
	ValueRank valueRank(); 
	String arrayDimensions(); 
	String description(); 
}
