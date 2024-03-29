package skill;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.RaspiPin;

import skillup.annotations.Aborting;
import skillup.annotations.Execute;
import skillup.annotations.OpcUaSkillType;
import skillup.annotations.Skill;
import skillup.annotations.SkillParameter;

/**
 * Simple LED Skill with an OPC UA skill interface. Switches on LED on execute and switches it off on abort
 */
@Skill(skillIri = "https://hsu-hh.de/skill/exampless#LedSkill", capabilityIri = "https://hsu-hh.de/capabilities/examples#LedCapability", moduleIri = "https://hsu-hh.de/modules#ExampleModule", type = OpcUaSkillType.class)
public class OpcUaLEDSkill {

	private final Logger logger = LoggerFactory.getLogger(OpcUaLEDSkill.class);

	// controller to have access to pins of raspberry pi
	private final GpioController gpio = GpioFactory.getInstance();

	/**
	 * time for the LED to glow
	 */
	@SkillParameter(isRequired = true)
	private int time;

	// pin to address the right pin of raspberry pi
	private final GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_29, "MyLED");

	/**
	 * When skill is in state execute (transition start must be triggered
	 * beforehand): LED turns on for x seconds given by parameter time
	 */
	@Execute
	public void execute() {
		// multiplication with 1000 to get unit seconds
		pin.pulse(time * 1000, true);
		logger.info("GPIO state should be ON for: " + time + " seconds");
	}

	/**
	 * Transition abort is fired: independent in which state skill is, the LED turns
	 * off
	 */
	@Aborting
	public void aborting() {
		pin.low();
		logger.info("GPIO state should be OFF");
	}
}
