package actionGenerator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import states.IStateAction;

public class SkillAction implements IStateAction {

	Method method;
	Object object;

	public SkillAction(Method method, Object object) {
		this.method = method;
		this.object = object;
	}

	@Override
	public void execute() {
		// TODO Auto-generated method stub
		try {
			this.method.invoke(object);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
