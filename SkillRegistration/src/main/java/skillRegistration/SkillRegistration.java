package skillRegistration;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import actionGenerator.ActionGenerator;
import annotations.Skill;
import server.Server;
import skillDescriptionGeneratorInterface.SkillDescriptionGenerator;
import skillGeneratorInterface.SkillGeneratorInterface;
import smartModule.SmartModule;
import statemachine.StateMachine;

@Component(immediate = true)
public class SkillRegistration {

	private final Logger logger = LoggerFactory.getLogger(SkillRegistration.class);

	private BundleTracker<Bundle> bundleTracker;
	private Map<Bundle, Object> classObjects = new HashMap<Bundle, Object>();

	@Reference
	ActionGenerator actionGenerator;

	@Reference(target = "(name=OpcUa)", cardinality = ReferenceCardinality.OPTIONAL)
	volatile SkillGeneratorInterface opcUaSkillGenerator;

	@Reference(target = "(name=Rest)", cardinality = ReferenceCardinality.OPTIONAL)
	volatile SkillGeneratorInterface restSkillGenerator;

	@Reference
	SkillDescriptionGenerator skillDescriptionGenerator;

	@Reference
	SmartModule module;
	
	@Reference
	Server server; 

	@Activate
	public void activate(BundleContext context) {

		this.bundleTracker = new BundleTracker<Bundle>(context, Bundle.ACTIVE, new BundleTrackerCustomizer<Bundle>() {

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
								Class<?> skill = bundle.loadClass(resource);
								// Class.forName(resource);
								Skill skillAnnotation = skill.getAnnotation(Skill.class);

								if (skillAnnotation != null) {

									Object skillObj = skill.getDeclaredConstructor().newInstance();
									classObjects.put(bundle, skillObj);
									StateMachine stateMachine = actionGenerator.generateAction(skillObj);

									if (skillAnnotation.value().equals("OpcUaSkill")) {
										logger.info("Add OPC-UA-Skill");

										opcUaSkillGenerator.generateSkill(skillObj, stateMachine);
										Enumeration<String> userSnippets = bundle.getEntryPaths("ExampleSnippet");
										String skillDescription = skillDescriptionGenerator
												.generateOpcUaDescription(server, skillObj, stateMachine, userSnippets);
										module.registerSkill(skillDescription, skillObj.getClass().getSimpleName());

									} else if (skillAnnotation.value().equals("RestSkill")) {
										logger.info("Add REST-Skill");

										restSkillGenerator.generateSkill(skillObj, stateMachine);
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
							Class<?> skill = bundle.loadClass(resource);
							// Class.forName(resource);
							Skill skillAnnotation = skill.getAnnotation(Skill.class);

							if (skillAnnotation != null) {

								Object skillObj = classObjects.get(bundle);
								// hier fehlt noch delete action!

								if (skillAnnotation.value().equals("OpcUaSkill")) {
									logger.info("Delete OPC-UA-Skill");

									opcUaSkillGenerator.deleteSkill(skillObj);

								} else if (skillAnnotation.value().equals("RestSkill")) {
									logger.info("Delete REST-Skill");

									restSkillGenerator.deleteSkill(skillObj);
								}
								classObjects.remove(bundle);
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

	@Deactivate
	public void deactivate() {
		this.bundleTracker.close();
	}
}
