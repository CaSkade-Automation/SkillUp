package restResource;

import javax.ws.rs.Path;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, service = RestResource.class)
@JaxrsResource
@Path("skills")
public class RestResource {

	private static Logger logger = LoggerFactory.getLogger(RestResource.class);

	@Activate
	public void activate() {
		logger.info("Activating " + this.getClass().getSimpleName());
	}

	@Deactivate
	public void deactivate() {
		logger.info("Deactivating " + this.getClass().getSimpleName());
	}

}