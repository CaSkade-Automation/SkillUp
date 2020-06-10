package smartTracker;

import java.io.IOException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import actionGenerator.ActionGenerator;
import annotations.Module;
import annotations.Skill;
import moduleDescriptionGenerator.ModuleDescriptionGenerator;
import moduleRegistration.ModuleRegistration;
import opcUaSkillDescriptionGenerator.OpcUaSkillDescriptionGenerator;
import server.Server;
import skillGeneratorInterface.SkillGeneratorInterface;
import skillRegistration.SkillRegistration;
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

	@Reference
	Server server;

	@Reference(target = "(name=OpcUa)", cardinality = ReferenceCardinality.OPTIONAL)
	volatile SkillGeneratorInterface opcUaSkillGenerator;

	@Reference(target = "(name=Rest)", cardinality = ReferenceCardinality.OPTIONAL)
	volatile SkillGeneratorInterface restSkillGenerator;

	@Reference
	ModuleRegistration moduleRegistration;

	@Reference
	SkillRegistration skillRegistration;

	@Reference
	OpcUaSkillDescriptionGenerator opcUaSkillDescriptionGenerator;

	@Reference
	ModuleDescriptionGenerator moduleDescriptionGenerator;

	@Activate
	public void activate(BundleContext context) {

		this.bundleTracker = new org.osgi.util.tracker.BundleTracker<Bundle>(context, Bundle.ACTIVE,
				new org.osgi.util.tracker.BundleTrackerCustomizer<Bundle>() {

					@Override
					public Bundle addingBundle(Bundle bundle, BundleEvent event) {
						// TODO Auto-generated method stub

						if (bundle.getLocation().contains("include")) {

							BundleWiring wiring = bundle.adapt(BundleWiring.class);
							if (wiring != null) {

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

											String moduleDescription = moduleDescriptionGenerator
													.generateModuleDescription(moduleObj, server, userSnippets);

											try {
												moduleRegistration.broadcast(moduleObj, moduleDescription);
											} catch (IOException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}

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
											Object moduleObj = skillRegistration.skillNeedsModule(skill.moduleIri());
											if (moduleObj != null) {
												Object skillObj = newBundle.getDeclaredConstructor().newInstance();
												skillClassObjects.put(bundle, skillObj);

												StateMachine stateMachine = actionGenerator.generateAction(skillObj);

												if (skill.type().equals("OpcUaSkill")) {
													logger.info("Add OPC-UA-Skill");

													opcUaSkillGenerator.generateSkill(skillObj, stateMachine);
													Enumeration<String> userSnippets = bundle.getEntryPaths("Snippets");
													String skillDescription = opcUaSkillDescriptionGenerator
															.generateOpcUaDescription(server, skillObj, stateMachine,
																	userSnippets);

													skillRegistration.register(skillDescription, skillObj);

												} else if (skill.type().equals("RestSkill")) {
													logger.info("Add REST-Skill");

													restSkillGenerator.generateSkill(skillObj, stateMachine);
													// später hier noch Beschreibung generieren
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

										moduleRegistration.delete(moduleClassObjects.get(bundle));
										moduleClassObjects.remove(bundle);
									}

									if (skill != null && !waitingSkills.containsKey(bundle)) {

										Object skillObj = skillClassObjects.get(bundle);
										// hier fehlt noch delete action!

										if (skill.type().equals("OpcUaSkill")) {
											logger.info("Delete OPC-UA-Skill");

											opcUaSkillGenerator.deleteSkill(skillObj);
											skillRegistration.delete(skillObj);

										} else if (skill.type().equals("RestSkill")) {
											logger.info("Delete REST-Skill");

											restSkillGenerator.deleteSkill(skillObj);
											// skillRegistration.delete();
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
