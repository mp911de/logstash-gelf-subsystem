package biz.paluch.logging.jboss.extension;

import org.jboss.as.controller.*;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;

import java.util.ArrayList;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 30.07.14 08:50
 */
public class GelfSenderWriteAttributeHandler extends RestartParentWriteAttributeHandler {
    GelfSenderWriteAttributeHandler(AttributeDefinition... attributeDefinitions) {
        super(ModelConstants.SENDER, attributeDefinitions);
    }

    @Override
    protected void recreateParentService(OperationContext context, PathAddress parentAddress, ModelNode parentModel,
            ServiceVerificationHandler verificationHandler) throws OperationFailedException {
        GelfSenderAdd.installRuntimeServices(context, parentAddress, parentModel, verificationHandler,
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
