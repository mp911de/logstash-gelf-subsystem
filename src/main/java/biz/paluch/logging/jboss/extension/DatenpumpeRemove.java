package biz.paluch.logging.jboss.extension;

import org.jboss.as.controller.AbstractRemoveStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.dmr.ModelNode;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 29.07.14 20:51
 */
public class DatenpumpeRemove extends AbstractRemoveStepHandler {
    public static final DatenpumpeRemove INSTANCE = new DatenpumpeRemove();

    private DatenpumpeRemove() {
    }

    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model)
            throws OperationFailedException {
        String suffix = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS)).getLastElement().getValue();
        /*
         * ServiceName name = TrackerService.createServiceName(suffix); context.removeService(name);
         */
    }
}
