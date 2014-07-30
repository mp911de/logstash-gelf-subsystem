package biz.paluch.logging.jboss.extension;

import java.util.ArrayList;

import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.RestartParentWriteAttributeHandler;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;

/**
 * @author <a href="mailto:mark.paluch@1und1.de">Mark Paluch</a>
 * @since 30.07.14 08:50
 */
public class DatenpumpeWriteAttributeHandler extends RestartParentWriteAttributeHandler {
    DatenpumpeWriteAttributeHandler(AttributeDefinition... attributeDefinitions) {
        super(ModelConstants.DATENPUMPE, attributeDefinitions);
    }

    @Override
    protected void recreateParentService(OperationContext context, PathAddress parentAddress, ModelNode parentModel,
            ServiceVerificationHandler verificationHandler) throws OperationFailedException {
        DatenpumpeAdd.installRuntimeServices(context, parentAddress, parentModel, verificationHandler,
                new ArrayList<ServiceController<?>>());
    }

    @Override
    protected ServiceName getParentServiceName(PathAddress parentAddress) {
        return SubsystemExtension.SERVICE_NAME.append(parentAddress.getLastElement().getValue());
    }

    @Override
    protected void removeServices(OperationContext context, ServiceName parentService, ModelNode parentModel)
            throws OperationFailedException {
        super.removeServices(context, parentService, parentModel);
        String jndiName = DatenpumpeAdd.getJndiName(parentModel, context);
        final ContextNames.BindInfo bindInfo = ContextNames.bindInfoFor(jndiName);
        context.removeService(bindInfo.getBinderServiceName());
    }

    @Override
    public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
        super.execute(context, operation);
    }
}
