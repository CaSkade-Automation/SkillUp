package skill;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.RaspiPin;

import annotations.Aborting;
import annotations.Execute;
import annotations.Skill;
import annotations.SkillParameter;

@Skill(skillIri = "https://siemens.de/skills#SwitchOnLED", capabilityIri = "https://siemens.de/capabilites#bestCapability", moduleIri = "https://siemens.de/modules#ModuleA")
public class LEDSkill {

	private final Logger logger = LoggerFactory.getLogger(LEDSkill.class);
	private final GpioController gpio = GpioFactory.getInstance(); 
	
	@SkillParameter(isRequired = true)
	private int time;

	private final GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_29, "MyLED"); 

	@Execute
	public void execute() {
		pin.pulse(time*1000, true);
		logger.info("GPIO state should be ON for: " + time + " seconds");
	}

	@Aborting
	public void aborting() {
		pin.low();
		logger.info("GPIO state should be OFF");
	}
}

