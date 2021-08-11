package actionGenerator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import states.IStateAction;

/**
 * Class which represents an skill method of stateMachine like starting, execute
 * etc.
 */
public class SkillAction implements IStateAction {

	private Method method;
	private Object object;

	/**
	 * Constructor for new object of this class
	 * 
	 * @param method the actual method that represents method of stateMachine
	 * @param object skills object to be able to invoke method
	 */
	public SkillAction(Method method, Object object) {
		this.method = method;
		this.object = object;
	}

	/**
	 * When action of state has to be executed the method of the skill is executed
	 */
	@Override
	public void execute() {
		// TODO Auto-generated method stub
		try {
			this.method.invoke(object);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
