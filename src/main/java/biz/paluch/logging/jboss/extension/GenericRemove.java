package biz.paluch.logging.jboss.extension;

import org.jboss.as.controller.AbstractRemoveStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceName;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 29.07.14 20:51
 */
public class GenericRemove extends AbstractRemoveStepHandler {
    public static final GenericRemove INSTANCE = new GenericRemove();

    private GenericRemove() {
    }

    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model)
            throws OperationFailedException {

        String jndiName = model.get("jndi-name").asString();
        ContextNames.BindInfo bindInfo = ContextNames.bindInfoFor(jndiName);
        ServiceName name = bindInfo.getBinderServiceName();
        context.removeService(name);
    }
}
