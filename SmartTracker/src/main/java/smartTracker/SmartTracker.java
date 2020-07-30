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
import annotations.Module;
import annotations.Skill;
import moduleGenerator.ModuleGenerator;
import registration.Registration;
import skillGeneratorInterface.SkillGeneratorInterface;
import statemachine.StateMachine;

@Component(immediate = true)
public class SmartTracker {

	private final Logger logger = LoggerFactory.getLogger(SmartTracker.class);
	private org.osgi.util.tracker.BundleTracker<Bundle> bundleTracker;

	// class objects from a skill bundle to use method to delete skill before
	// removing bundle
	private Map<Bundle, Object> skillClassObjects = new HashMap<Bundle, Object>();
	private Map<Bundle, Object> moduleClassObjects = new HashMap<Bundle, Object>();

	// Bundle of skill xy is waiting for module with moduleIri z
	private Map<Bundle, String> waitingSkills = new HashMap<Bundle, String>();

	@Reference
	ActionGenerator actionGenerator;

	private Map<SkillGeneratorInterface, String> skillGeneratorPropertyList = new HashMap<>();

	@Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE)
	void addSkillGeneratorInterface(SkillGeneratorInterface skillGenerator, Map<String, Object> properties) {
		this.skillGeneratorPropertyList.put(skillGenerator, properties.get("name").toString());
	}

	void removeSkillGeneratorInterface(SkillGeneratorInterface skillGenerator) {
		this.skillGeneratorPropertyList.remove(skillGenerator);
	}

	@Reference
	Registration registration;

	@Reference
	ModuleGenerator moduleGenerator;

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
									Class<?> newBundle = bundle.loadClass(resource);
									Skill skill = newBundle.getAnnotation(Skill.class);
									Module module = newBundle.getAnnotation(Module.class);

									if (module != null) {
										Object moduleObj = newBundle.getDeclaredConstructor().newInstance();
										moduleClassObjects.put(bundle, moduleObj);

										Enumeration<String> userSnippets = bundle.getEntryPaths("Snippets");

										String moduleDescription = moduleGenerator.generateModuleDescription(moduleObj,
												userSnippets);

										registration.registerModule(moduleDescription, moduleObj);

										List<Bundle> startSkills = getKey(waitingSkills, module.moduleIri());
										if (!startSkills.isEmpty()) {
											try {
												for (Bundle startSkill : startSkills) {
													startSkill.start();
													waitingSkills.remove(startSkill);
												}
											} catch (BundleException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
										}
									}

									if (skill != null) {
										boolean moduleAvailable = registration.skillNeedsModule(skill.moduleIri());
										if (moduleAvailable) {
											Object skillObj = newBundle.getDeclaredConstructor().newInstance();
											skillClassObjects.put(bundle, skillObj);

											StateMachine stateMachine = actionGenerator.generateAction(skillObj);
											StateChangeObserver stateChangeObserver = new StateChangeObserver(registration, skillObj); 
											stateMachine.addStateChangeObserver(stateChangeObserver);

											for (Map.Entry<SkillGeneratorInterface, String> me : skillGeneratorPropertyList
													.entrySet()) {
												if (skill.type().contains(me.getValue())) {

													logger.info("Add " + me.getValue() + "-Skill");

													me.getKey().generateSkill(skillObj, stateMachine);
													Enumeration<String> userSnippets = bundle.getEntryPaths("Snippets");
													String skillDescription = me.getKey().generateDescription(skillObj,
															stateMachine, userSnippets);

													registration.registerSkill(skillDescription, skillObj);
												} else {
													logger.error("No SkillGenerator for this type of skill...");
												}
											}
										} else {
											try {
												waitingSkills.put(bundle, skill.moduleIri());
												bundle.stop();
											} catch (BundleException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
										}
									}
								} catch (ClassNotFoundException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (SecurityException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (InstantiationException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (IllegalAccessException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (IllegalArgumentException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (InvocationTargetException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (NoSuchMethodException e) {
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
						if (wiring != null) {

							Collection<String> resources = wiring.listResources("/", "*class",
									BundleWiring.LISTRESOURCES_RECURSE);

							for (String resource : resources) {
								try {
									resource = resource.replace("/", ".");
									resource = resource.substring(0, resource.lastIndexOf("."));
									Class<?> removeBundle = bundle.loadClass(resource);
									// Class.forName(resource);
									Module module = removeBundle.getAnnotation(Module.class);
									Skill skill = removeBundle.getAnnotation(Skill.class);

									if (module != null) {
										logger.info("Delete Module " + module.moduleIri());

										registration.deleteModule(moduleClassObjects.get(bundle));
										moduleClassObjects.remove(bundle);
									}

									if (skill != null && !waitingSkills.containsKey(bundle)) {

										Object skillObj = skillClassObjects.get(bundle);
										// hier fehlt noch delete action!

										for (Map.Entry<SkillGeneratorInterface, String> me : skillGeneratorPropertyList
												.entrySet()) {
											if (skill.type().contains(me.getValue())) {
												logger.info("Delete " + me.getValue() + "-Skill");
												me.getKey().deleteSkill(skillObj);
												registration.deleteSkill(skillObj);
											}
										}

										skillClassObjects.remove(bundle);
									}
								} catch (ClassNotFoundException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (SecurityException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (IllegalArgumentException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
					}
				});
		this.bundleTracker.open();

	}

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
