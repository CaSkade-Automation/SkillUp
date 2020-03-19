package server;



import org.eclipse.milo.opcua.sdk.server.api.methods.AbstractMethodInvocationHandler;
import org.eclipse.milo.opcua.sdk.server.nodes.UaMethodNode;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.structured.Argument;
import statemachine.StateMachine;

/**
 * Class is used to reflect the skill to be added to the server <br>
 */
public class GenericMethod extends AbstractMethodInvocationHandler {
 
	StateMachine stateMachine; 
	String transitionType; 
	
	public GenericMethod(UaMethodNode node, StateMachine stateMachine, String transitionType) {
		super(node); 
		this.stateMachine = stateMachine;  
		this.transitionType = transitionType; 
	}

	@Override
	public Argument[] getInputArguments() {
		// TODO Auto-generated method stub
		//important!! new Argument[0], because AbstractMethodInvocationHandler checks in line 63 if length of inputValues == length of Argument
		//and by returning null it occurs an error. Then AbstractMethodInvocationHandler sets inputValues to new Variant[0] if inputValues are null  
		return new Argument[0];
	}

	@Override
	public Argument[] getOutputArguments() {
		// TODO Auto-generated method stub
		return new Argument[0];
	}

	@Override
	protected Variant[] invoke(InvocationContext invocationContext, Variant[] inputs) throws UaException {
		// TODO Auto-generated method stub
		
		if(transitionType.equals("Start")) {
			stateMachine.start(); 
		}
		else if(transitionType.equals("Stop")) {
			stateMachine.stop();
		}
		else if(transitionType.equals("Clear")) {
			stateMachine.clear(); 
		}
		else if(transitionType.equals("Abort")) {
			stateMachine.abort(); 
		}
		else if(transitionType.equals("Hold")) {
			stateMachine.hold();
		}
		else if(transitionType.equals("Reset")) {
			stateMachine.reset();
		}
		else if(transitionType.equals("Suspend")) {
			stateMachine.suspend();
		}
		else if(transitionType.equals("Unhold")) {
			stateMachine.unhold();
		}
		else if(transitionType.equals("Unsuspsend")) {
			stateMachine.unsuspend();
		}
		
		//stateMachine.invokeTransition(transitionType); 
		
		return null;  
	}
}
