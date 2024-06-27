package taxisvc.infra;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;
import taxisvc.domain.*;

@Component
public class DriveHateoasProcessor
    implements RepresentationModelProcessor<EntityModel<Drive>> {

    @Override
    public EntityModel<Drive> process(EntityModel<Drive> model) {
        return model;
    }
}
