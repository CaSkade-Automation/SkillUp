package server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.milo.opcua.sdk.server.api.methods.AbstractMethodInvocationHandler;
import org.eclipse.milo.opcua.sdk.server.nodes.UaMethodNode;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.structured.Argument;
import opcuaSkillRegistration.OPCUASkillRegistration;
import statemachine.StateMachine;

/**
 * Class is used to reflect the skill to be added to the server <br>
 */
public class GenericMethod extends AbstractMethodInvocationHandler {

	OPCUASkillRegistration skillRegistration; 
	Method method;  
	Argument[] inputArguments; 
	Argument[] outputArguments; 
	
	StateMachine stateMachine; 

//	public GenericMethod(UaMethodNode node, OPCUASkillRegistration skillRegistration, Method method, Argument[] inputArguments, Argument[] outputArguments) {
//		super(node);
//		this.skillRegistration = skillRegistration; 
//		this.method = method;
//		this.inputArguments = inputArguments; 
//		this.outputArguments = outputArguments; 
//		// TODO Auto-generated constructor stub
//	}
	
	public GenericMethod(UaMethodNode node, StateMachine stateMachine, Method method) {
		super(node); 
		this.method = method; 
		this.stateMachine = stateMachine; 
	}

	@Override
	public Argument[] getInputArguments() {
		// TODO Auto-generated method stub
		return inputArguments;
	}

	@Override
	public Argument[] getOutputArguments() {
		// TODO Auto-generated method stub
		return outputArguments;
	}

	@Override
	protected Variant[] invoke(InvocationContext invocationContext, Variant[] inputs) throws UaException {
		// TODO Auto-generated method stub
		
		if(method.getName().equals("start")) {
			stateMachine.start(); 
		}
		
		return null; 
		
		
		
//		List<Object> inputValues = new ArrayList<>(); 
//		for (Variant input : inputs) {
//			inputValues.add(input.getValue());
//		}
//		try {
//			Object result = this.method.invoke(skillRegistration, inputValues.toArray());
//			
//			return new Variant[] { new Variant(result) };
//			
//		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return null;
//		}
	}
}
