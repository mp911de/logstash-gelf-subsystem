package biz.paluch.logging.jboss.extension;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import org.jboss.as.controller.*;
import org.jboss.as.controller.registry.Resource;
import org.jboss.as.naming.ServiceBasedNamingStore;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.as.naming.service.BinderService;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.AbstractServiceListener;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceTarget;

import java.util.List;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 29.07.14 20:45
 */
public class GelfSenderAdd extends AbstractGelfSenderAdd {
    public static final GelfSenderAdd INSTANCE = new GelfSenderAdd();

    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model,
            ServiceVerificationHandler verificationHandler, List<ServiceController<?>> controllers)
            throws OperationFailedException {

        final PathAddress address = PathAddress.pathAddress(operation.get(OP_ADDR));
        ModelNode fullTree = Resource.Tools.readModel(context.readResource(PathAddress.EMPTY_ADDRESS));
        installRuntimeServices(context, address, fullTree, verificationHandler, controllers);
    }

    static void installRuntimeServices(OperationContext context, PathAddress address, ModelNode model,
            ServiceVerificationHandler verificationHandler, List<ServiceController<?>> controllers)
            throws OperationFailedException {

        final ServiceTarget serviceTarget = context.getServiceTarget();
        final GelfSenderServiceConfiguration configuration = getGelfSenderServiceConfiguration(context, address
                .getLastElement().getValue(), model);

        GelfSenderManagedReferenceFactory factory = new GelfSenderManagedReferenceFactory(configuration);
        ContextNames.BindInfo bindInfo = ContextNames.bindInfoFor(configuration.getJndiName());

        BinderService binderService = new BinderService(bindInfo.getBindName());
        ServiceBuilder<?> binderBuilder = serviceTarget
                .addService(bindInfo.getBinderServiceName(), binderService)
                .addInjection(binderService.getManagedObjectInjector(), factory)
                .addDependency(bindInfo.getParentContextServiceName(), ServiceBasedNamingStore.class,
                        binderService.getNamingStoreInjector()).addListener(new AbstractServiceListener<Object>() {
                    public void transition(final ServiceController<? extends Object> controller,
                            final ServiceController.Transition transition) {
                        switch (transition) {
                            case STARTING_to_UP: {
                                LogstashGelfExtensionLogger.ROOT_LOGGER.boundItem("GelfSender", configuration.getJndiName());
                                break;
                            }
                            case START_REQUESTED_to_DOWN: {
                                LogstashGelfExtensionLogger.ROOT_LOGGER.unboundItem("GelfSender", configuration.getJndiName());
                                break;
                            }
                            case REMOVING_to_REMOVED: {
                                LogstashGelfExtensionLogger.ROOT_LOGGER.removedItem("GelfSender", configuration.getJndiName());
                                break;
                            }
                        }
                    }
                });

        binderBuilder.setInitialMode(ServiceController.Mode.ACTIVE).addListener(verificationHandler);
        controllers.add(binderBuilder.install());

    }

}
