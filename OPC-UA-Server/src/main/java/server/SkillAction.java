package server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import states.IStateAction;

public class SkillAction implements IStateAction{

	Method method; 
	Object object; 
	Object args; 
	
	public SkillAction(Method method, Object object, Object args) {
		this.method = method; 
		this.object	= object; 
		this.args = args; 
	}
	
	
	@Override
	public void execute() {
		// TODO Auto-generated method stub
		try {
			method.invoke(object, args);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}	
}
