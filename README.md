# Skill-Up
*Skill-based engineering* is a term used in automation research for a couple of approaches trying to describe machines and their functionalities as skills. *Semantic Web Technologies* like ontologies using the Web Ontology Language (OWL) are seen as a promising solution to create such descriptions in a machine-readable and vendor-neutral way. But using OWL models places high demands on machine engineers and software developers involved in developing a machine. In addition to programming the machine behavior - which has to be done anyway - they have to provide an interface to invoke this machine behavior and they have to create an ontological description for this interface. This leads to a lot of challenges: There are very few ontology experts in automation industry and these tasks are both tedious and error-prone when done manually.<br>
**This is where Skill-Up comes into play.** <br>
Skill-Up is a Java-framework which can be used to develop machine skills without any additional effort. You can implement your machine behavior as plain Java classes only adding a few annotations, everything else will be generated automatically. In a bit more detail, these things that are generated are:
* A [state machine according to ISA 88]([https://link](https://github.com/aljoshakoecher/ISA88-StateMachine)) containing your behavior. The state machine makes sure the behavior is only executed in the right state.
* An invocation interface (either via RESTful webservices or OPC UA) which can be used to interact with the state machine (trigger commands, get current state)
* An ontological description of the state machine and the invocation interface. This description can be consumed by other systems that want to interact with a machine.

Skill-Up is based on this [Machine Capability and Skill Model](https://github.com/aljoshakoecher/machine-capability-model). For more details about the model, please visit this repository. 

## Setup
Skill-Up is implemented as a collection of OSGi bundles. You can start a *Skill Runtime* (i.e. an OSGi container running certain necessary bundles as well as Skill-Up) on a machine's controller and manage (add, start, stop) skills while others are running.

We will provide a ready-to-use *Skill-Runtime* very soon... 

## Usage
We recommend to use one of the example projects as a template to get started. A detailed step-by-step guide will be coming soon...
After setting up your IDE, programming a skill is actually quite simple. If you look at the examples below, you can see that you just have to add some annotations to your otherwise conventional Java class. These annotations are:

### @Skill
`@Skill` is a class annotation which is mandatory in order to mark your class as a skill. It is needed by Skill-Up to get notified about a newly deployed skill in the Skill-Runtime.
You can pass in additional arguments to `@Skill`:
* type (string): Can either be "OpcUaSkill" or "RestSkill" 

### @SkillParameter
This is a field annotation that can be used to mark certain fields as parameters of your skill. Specifying fields as `@SkillParameter` creates an interface to change these later on when the skill is used.

### @SkillOutput
`@SkillOutput` is another field annotation that defines which fields will be returned by the skill. Note that skill execution is asynchronous and that other systems can get result information when a skill is completed

### ISA88 state annotations
Together with `@Skill` these annotations are important to create a functioning skill. There is a total of 11 annotations in this group that represent so-called active states of the ISA 88 state machine. These annotations have to be placed in front of a method. Using these annotations makes sure that this method is executed in the corresponding state of the ISA 88 state machine.

* @Aborting: Ensures that the method following this annotation is executed in Aborting state
* @Clearing: Ensures that the method following this annotation is executed in Clearing state
* @Completing: Ensures that the method following this annotation is executed in Completing state
* @Execute: Ensures that the method following this annotation is executed in Execute state
* @Holding: Ensures that the method following this annotation is executed in Holding state
* @Resetting: Ensures that the method following this annotation is executed in Resetting state
* @Starting: Ensures that the method following this annotation is executed in Starting state
* @Stopping: Ensures that the method following this annotation is executed in Stopping state
* @Suspending: Ensures that the method following this annotation is executed in Suspending state
* @Unholding: Ensures that the method following this annotation is executed in Unholding state
* @Unsuspending: Ensures that the method following this annotation is executed in Unsuspending state


If you have your *Skill-Runtime* up and running, you can deploy skills by simply dropping them into the *include*-directory of your skill runtime. Skill-Up will pick up the new skill and publish it as a new skill.

## Examples
You can already find examples in the examples folder. They will be explained here soon...

```Java
@Skill(type = "OpcUaSkill")
public class AdditionSkill {
	@SkillParameter
	int a,b;
	
	@SkillOutput
	int result;
	
	@Execute
	public void aPlusB() {
		this.result = this.a + this.b;
	}
}
```

## How it works
One bundle in our Skill-Runtime is responsible for tracking new bundles that contain a @Skill-Annoation
