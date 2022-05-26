<div align="center">
    <img width="400px" src="https://github.com/aljoshakoecher/skill-up/blob/documentation/images/images/SkillUp-Icon.png?raw=true">
</div>
<h1 align="center">Skill Development Framework</h1>


*Skill-based engineering* is a term used in automation research for a couple of approaches trying to describe machines and their functionalities as skills. *Semantic Web Technologies* like ontologies using the Web Ontology Language (OWL) are seen as a promising solution to create such descriptions in a machine-readable and vendor-neutral way. But using OWL models places high demands on machine engineers and software developers involved in developing a machine. In addition to programming the machine behavior - which has to be done anyway - they have to provide an interface to invoke this machine behavior and they have to create an ontological description for this interface. This leads to many challenges: There are very few ontology experts in automation industry and these tasks are both tedious and error-prone when done manually. **This is where Skill-Up comes into play.**

Skill-Up is a Java-framework which can be used to develop machine skills without any additional effort. You can implement your machine behavior as plain Java classes only adding a few annotations, everything else will be generated automatically. In a bit more detail, these things that are generated are:

* A [state machine according to ISA 88]([https://link](https://github.com/aljoshakoecher/ISA88-StateMachine)) containing your behavior. The state machine makes sure the behavior is only executed in the right state.
* An invocation interface (either via RESTful webservices or OPC UA) which can be used to interact with the state machine (trigger commands, get current state)
* An ontological description of the state machine and the invocation interface. This description can be consumed by other systems that want to interact with a machine.

Skill-Up is based on this [Machine Capability and Skill Model](https://github.com/aljoshakoecher/machine-capability-model). For more details about the model, please visit this repository.
SkillUp is best used in combination with SkillMEx which is also available on Github: https://github.com/aljoshakoecher/SkillMEx

## Develop Modules and Skills
SkillUp distinguishes between *modules* (i.e., machines that may provide skills) and *skills* (i.e., machine functionality that is provided by a module and can be executed). 
In order to develop your own modules and skills, all you need to do is create a Maven project and import the following dependency in order to use the necessary annotations:

```xml
<dependency>
	<groupId>io.github.aljoshakoecher.skillup</groupId>
	<artifactId>skillup.annotations</artifactId>
	<version>1.0.0</version>
</dependency>
```
After building a module or skill, you can deploy both`inside a SkillUp runtime. Note that you have to have an OSGi bundle of a module running inside your SkillUp runtime before you can add skills.
After setting up your OSGi container, you can develop your own modules and skills and deploy them inside a SkillUp runtime. Please check the wiki articles on [how to develop your own module](https://github.com/aljoshakoecher/skill-up/wiki/Step-by-step-instructions-for-creating-a-module) as well as [how to develop your own skill](https://github.com/aljoshakoecher/skill-up/wiki/Step-by-step-instructions-for-creating-a-skill)
Furthermore, there are example projects to help you get started. If you look at these examples, you can see that you just have to add some annotations to your otherwise conventional Java class. There are annotations to create modules and skills that are shown below.

### Module
`@Module` is the only annotation that is needed in order to decorate a module class. It is a class annotation that accepts two arguments
* `moduleIri`: This IRI will be used for the module individual when creating the semantic description
* `capabilityIri`(optional): Currently not used
* `description` (optional): A human-readable description of this module


### @Skill
`@Skill` is a class annotation which is mandatory in order to mark your class as a skill. It is needed by Skill-Up to get notified about a newly deployed skill in a SkillUp runtime.
You can pass in additional arguments to `@Skill`:
* `skillIri`: This IRI will be used for the skill individual when creating the semantic description
* `capabilityIri`: IRI of the capability that this skill is able to execute
* `moduleIri`: IRI of the module that this skill belongs to. Important: A skill can only be activated if a module bundle is already deployed
* `type`: This is used to specify the implementation of a skill. A class extending `SkillType` has to be passed as a value. Currently, there are only two possible candidates: `OpcUaSkillType` (for skills implemented via OPC UA) and `RestSkillType` (for skills implemented as web services).
* `description` (optional): A human-readable description of this skill

### @SkillParameter
This is a field annotation that can be used to mark certain fields as parameters of your skill. Specifying fields as `@SkillParameter` creates an interface to change these parameters at runtime when the skill is used. This annotation accecpts arguments:
* `name` (optional): Typically, the variable name will be used when creating the semantic skill mode. This argument can be used to use a different name in the model.
* `description` (optional): A human-readable description of this skill
* `isRequired`: Specifies whether or not this parameter has to be set on skill invocations
* `option` (optional): A string array of possible options. Can be used in case a parameter should only accept certain values.

### @SkillOutput
`@SkillOutput` is another field annotation that defines which fields will be returned by the skill. Note that skill execution is asynchronous and that other systems can get result information when a skill is completed. This annotation accecpts arguments:
* `name` (optional): Typically, the variable name will be used when creating the semantic skill mode. This argument can be used to use a different name in the model.
* `description` (optional): A human-readable description of this skill
* `isRequired`(optional): Specifies whether or not this parameter has to be set on skill invocations

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


Build your module and skill classes as OSGi bundles and fire up your SkillUp runtime. You can then deploy a module by dropping the according bundle into the *include*-directory of your SkillUp runtime. Skill-Up will pick up the module and register it to SkillMEx.
After deploying a module, you can deploy skills of that module (i.e., with the same `moduleIri`). SkillUp will pick up every skill and register it to SkillMEx from where skills can be executed.

## Deployment
Skill-Up is implemented as a collection of OSGi bundles that create a runtime in which modules and skills can be deployed dynamically. A preconfigured *SkillUp-Runtime* (i.e. an OSGi container running certain necessary bundles as well as Skill-Up) is provided with the releases. You can use this preconfigured runtime on a machine's controller and manage (add, start, stop) skills while others are running.

If you want to setup your own OSGi container to be used as a runtime for SkillUp, you need to compile all the bundles of SkillUp. This is best done by executing a `mvn install` from the project root. Furthermore, the dependencies of the SkillUP bundles are needed. A list of required bundles will be provided soon.

## Examples
Check out the example module and skills in the examples folder to get started. Feel free to create an issue if necessary. 

## How it works
:construction: The way SkillUp works will be explained soon :construction:

## How to cite
We are excited about everyone using SkillUp in their own applications. If you use SkillUp in research, please consider giving credit by citing the following paper that initially introduced SkillUp:

```
@inproceedings{KHC+_AutomatingtheDevelopmentof_2020,
 author = {Köcher, Aljosha and Hildebrandt, Constantin and Caesar, Birte and Bakakeu, Jupiter and Peschke, Joern and Scholz, Andre and Fay, Alexander},
 title = {{Automating the Development of Machine Skills and their Semantic Description}},
 pages = {1013--1018},
 publisher = {IEEE},
 isbn = {978-1-7281-8956-7},
 booktitle = {{2020 25th IEEE International Conference on Emerging Technologies and Factory Automation (ETFA)}},
 year = {9/8/2020 - 9/11/2020},
 doi = {10.1109/ETFA46521.2020.9211933},
 shorthand = {KHC+20}
}
```
