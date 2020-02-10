package method;

import org.eclipse.milo.opcua.sdk.core.ValueRanks;
import org.eclipse.milo.opcua.sdk.server.api.methods.AbstractMethodInvocationHandler.InvocationContext;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.structured.Argument;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import methodRegistration.MethodRegistration;

/**
 * @Component Indicates that annotated class is intended to be an OSGi
 *            component. <br>
 *            After becoming satisfied component is registered as a service under MethodRegistration
 */
@Component
public class SimpleMethod implements MethodRegistration {
	
	public static final Argument[] inputArguments = {
			new Argument("1. Zahl", Identifiers.Double, ValueRanks.Scalar, null, new LocalizedText("1. Summand")),
			new Argument("2. Zahl", Identifiers.Double, ValueRanks.Scalar, null, new LocalizedText("2. Summand")) };

	public static final Argument outputArgument = new Argument("Ergebnis", Identifiers.Double, ValueRanks.Scalar, null,
			new LocalizedText("Summe"));

	private final Logger logger = LoggerFactory.getLogger(SimpleMethod.class);

	@Override
	public Argument[] getInputArguments() {
		// TODO Auto-generated method stub
		return inputArguments;
	}

	@Override
	public Argument[] getOutputArguments() {
		// TODO Auto-generated method stub
		return new Argument[] { outputArgument };
	}

	/**
	 * Method that turns input arguments into output argument
	 */
	@Override
	public Variant[] invoke(InvocationContext context, Variant[] inputValues) throws UaException {
		logger.debug("Invoking simpleMethod of objectId={}", context.getObjectId());

		double firstNumber = (double) inputValues[0].getValue();
		double secondNumber = (double) inputValues[1].getValue();
		double sum = firstNumber + secondNumber;

		return new Variant[] { new Variant(sum) };
	}

	@Activate
	public void activate() {
		logger.info("Methode wird aktiviert.");
	}

	@Deactivate
	public void deactivate() {
		logger.info("Methode wird deaktiviert.");
	}
}
