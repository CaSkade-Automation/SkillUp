package smartTracker;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import actionGenerator.ActionGenerator;
import moduleGenerator.ModuleGenerator;
import registration.Registration;
import skillGeneratorInterface.SkillGeneratorInterface;
import skillup.annotations.Module;
import skillup.annotations.Skill;
import statemachine.Isa88StateMachine;

/**
 * Superior class which tracks new bundles and generates/deletes modules/skills
 * by using references to necessary components
 * 
 * @Component Indicates that annotated class is intended to be an OSGi
 *            component. <br>
 *            immediate=true, component configuration activates immediately
 *            after becoming satisfied
 */
@Component(immediate = true)
public class SmartTracker {

	private final Logger logger = LoggerFactory.getLogger(SmartTracker.class);
	private org.osgi.util.tracker.BundleTracker<Bundle> bundleTracker;

	// class objects from a skill/module bundle to use method to delete skill/module
	// before removing bundle
	private Map<Bundle, Object> skillClassObjects = new HashMap<Bundle, Object>();
	private Map<Bundle, Object> moduleClassObjects = new HashMap<Bundle, Object>();

	// Bundle of skill xy is waiting for module with moduleIri z
	private Map<Bundle, String> waitingSkills = new HashMap<Bundle, String>();

	@Reference
	ActionGenerator actionGenerator;

	/**
	 * List of skill generators for different technologies like OpcUa or WADL
	 */
	private Map<SkillGeneratorInterface, Class<?>> skillGeneratorPropertyList = new HashMap<>();

	/**
	 * Adds new skill generator to list
	 * 
	 * @param skillGenerator
	 * @param properties     to get technology type of skill generator
	 */
	@Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE)
	void addSkillGeneratorInterface(SkillGeneratorInterface skillGenerator, Map<String, Object> properties) {
		try {
			this.skillGeneratorPropertyList.put(skillGenerator,
					Class.forName("skillup.annotations." + properties.get("type").toString()));
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Removes skill generator from list and stops every skill bundle corresponding
	 * to this skill generator (same technology type)
	 * 
	 * @param skillGenerator
	 * @param properties     to get technology type of skill generator
	 */
	void removeSkillGeneratorInterface(SkillGeneratorInterface skillGenerator, Map<String, Object> properties) {
		this.skillGeneratorPropertyList.remove(skillGenerator);
		for (Map.Entry<Bundle, Object> me : skillClassObjects.entrySet()) {
			try {
				if (me.getValue().getClass().getAnnotation(Skill.class).type()
						.equals(Class.forName("actionGenerator." + properties.get("type").toString()))) {

					me.getKey().stop();
				}
			} catch (ClassNotFoundException | BundleException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Reference
	Registration registration;

	@Reference
	ModuleGenerator moduleGenerator;

	/**
	 * When all references are met an broadcast is send to get every OPS <br>
	 * Method tracks new bundles and generates modules or skills. By removing a
	 * bundle which is an module or skill, it will be deleted
	 * 
	 * @param context
	 */
	@Activate
	public void activate(BundleContext context) {

		registration.broadcast();

		this.bundleTracker = new org.osgi.util.tracker.BundleTracker<Bundle>(context, Bundle.ACTIVE,
				new org.osgi.util.tracker.BundleTrackerCustomizer<Bundle>() {

					@Override
					public Bundle addingBundle(Bundle bundle, BundleEvent event) {
						// TODO Auto-generated method stub

						if (bundle.getLocation().contains("include")) {

							BundleWiring wiring = bundle.adapt(BundleWiring.class);

							Collection<String> resources = wiring.listResources("/", "*class",
									BundleWiring.LISTRESOURCES_RECURSE);

							for (String resource : resources) {

								try {
									resource = resource.replace("/", ".");
									resource = resource.substring(0, resource.lastIndexOf("."));
									Class<?> bundleClass = bundle.loadClass(resource);

									addModule(bundleClass, bundle);
									addSkill(bundleClass, bundle);

								} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
										| IllegalArgumentException | InvocationTargetException | NoSuchMethodException
										| SecurityException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
						return bundle;
					}

					@Override
					public void modifiedBundle(Bundle bundle, BundleEvent event, Bundle object) {
						// TODO Auto-generated method stub
					}

					@Override
					public void removedBundle(Bundle bundle, BundleEvent event, Bundle object) {

						BundleWiring wiring = bundle.adapt(BundleWiring.class);

						Collection<String> resources = wiring.listResources("/", "*class",
								BundleWiring.LISTRESOURCES_RECURSE);

						for (String resource : resources) {
							try {
								resource = resource.replace("/", ".");
								resource = resource.substring(0, resource.lastIndexOf("."));
								Class<?> bundleClass = bundle.loadClass(resource);
								// Class.forName(resource);

								deleteModule(bundleClass, bundle);
								deleteSkill(bundleClass, bundle);

							} catch (ClassNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				});
		this.bundleTracker.open();

	}

	/**
	 * This method checks if the new bundle is a module and if it is the module
	 * description is generated and registered.
	 * 
	 * @param bundleClass class of the new bundle
	 * @param bundle      the tracked bundle
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public void addModule(Class<?> bundleClass, Bundle bundle) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {

		Module module = bundleClass.getAnnotation(Module.class);
		if (module == null)
			return;

		Object moduleObj;
		moduleObj = bundleClass.getDeclaredConstructor().newInstance();

		moduleClassObjects.put(bundle, moduleObj);

		Enumeration<String> userSnippets = bundle.getEntryPaths("Snippets");

		String moduleDescription = moduleGenerator.generateModuleDescription(moduleObj, userSnippets);

		registration.registerModule(moduleDescription, moduleObj);

		// if skills are waiting for this module, their bundles are started
		List<Bundle> startSkills = getKey(waitingSkills, module.moduleIri());
		if (startSkills.isEmpty())
			return;

		for (Bundle startSkill : startSkills) {
			try {
				startSkill.start();
			} catch (BundleException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			waitingSkills.remove(startSkill);
		}
	}

	/**
	 * This method checks if the new bundle is a skill. Then the necessary module
	 * has to be registered otherwise the skill can't be generated. If the module is
	 * registered, a stateMachine for the skill is created and an observer is added
	 * to the skill (to know when its state changes). Corresponding to the
	 * technology the suitable skill generator is used to generate the skill and the
	 * description. Finally the skill is registered.
	 * 
	 * @param bundleClass class of the new bundle
	 * @param bundle      new bundle
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public void addSkill(Class<?> bundleClass, Bundle bundle) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Skill skill = bundleClass.getAnnotation(Skill.class);
		if (skill == null)
			return;
		// checks if module is registered
		boolean moduleAvailable = registration.skillNeedsModule(skill.moduleIri());
		if (moduleAvailable) {
			Object skillObj = bundleClass.getDeclaredConstructor().newInstance();
			skillClassObjects.put(bundle, skillObj);

			Isa88StateMachine stateMachine = actionGenerator.generateAction(skillObj);

			StateChangeObserver stateChangeObserver = new StateChangeObserver(registration, skillObj);
			stateMachine.addStateChangeObserver(stateChangeObserver);

			boolean matchFound = false;
			for (Map.Entry<SkillGeneratorInterface, Class<?>> me : skillGeneratorPropertyList.entrySet()) {
				if (skill.type().equals(me.getValue())) {

					logger.info("Add " + me.getValue() + "-Skill");

					me.getKey().generateSkill(skillObj, stateMachine);
					Enumeration<String> userSnippets = bundle.getEntryPaths("Snippets");
					String skillDescription = me.getKey().generateDescription(skillObj, stateMachine, userSnippets);

					registration.registerSkill(skillDescription, skillObj);
					matchFound = true;
					break;
				}
			}
			// if no skill generator is available for this type of skill
			if (!matchFound) {
				logger.error("No SkillGenerator for this type of skill...");
			}
		} else {
			try {
				// if module is not registered the skill is added to an waiting list and the
				// bundle is stopped
				waitingSkills.put(bundle, skill.moduleIri());
				bundle.stop();
			} catch (BundleException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Method checks if removed bundle is a module. If it is the module is deleted
	 * from OPS
	 * 
	 * @param bundleClass class of the removed bundle
	 * @param bundle      removed bundle
	 */
	public void deleteModule(Class<?> bundleClass, Bundle bundle) {
		Module module = bundleClass.getAnnotation(Module.class);
		if (module == null)
			return;
		logger.info("Delete Module " + module.moduleIri());

		registration.deleteModule(moduleClassObjects.get(bundle));
		moduleClassObjects.remove(bundle);
	}

	/**
	 * Method checks if removed bundle is a skill. If it is the skill is first
	 * deleted and then removed from OPS
	 * 
	 * @param bundleClass class of removed bundle
	 * @param bundle      removed bundle
	 */
	public void deleteSkill(Class<?> bundleClass, Bundle bundle) {
		Skill skill = bundleClass.getAnnotation(Skill.class);
		if (skill == null || waitingSkills.containsKey(bundle))
			return;
		Object skillObj = skillClassObjects.get(bundle);

		for (Map.Entry<SkillGeneratorInterface, Class<?>> me : skillGeneratorPropertyList.entrySet()) {
			if (skill.type().equals(me.getValue())) {
				logger.info("Delete " + me.getValue() + "-Skill");
				me.getKey().deleteSkill(skillObj);
				break;
			}
		}
		registration.deleteSkill(skillObj);
		skillClassObjects.remove(bundle);
	}

	/**
	 * Method to get skills from waitingList corresponding to the necessary module
	 * 
	 * @param map   waitingList which contains every skill waiting for its module
	 * @param value module that is now registered
	 * @return list of skills that can now be registered
	 */
	public List<Bundle> getKey(Map<Bundle, String> map, String value) {
		List<Bundle> skillList = new ArrayList<Bundle>();
		for (Entry<Bundle, String> entry : map.entrySet()) {
			if (entry.getValue().equals(value)) {
				skillList.add(entry.getKey());
			}
		}
		return skillList;
	}

	@Deactivate
	public void deactivate() {
		this.bundleTracker.close();
	}
}
