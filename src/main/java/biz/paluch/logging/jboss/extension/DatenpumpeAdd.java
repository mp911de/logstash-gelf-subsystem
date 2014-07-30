package biz.paluch.logging.jboss.extension;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

import java.util.List;

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.registry.Resource;
import org.jboss.as.naming.ServiceBasedNamingStore;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.as.naming.service.BinderService;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.AbstractServiceListener;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceTarget;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 29.07.14 20:45
 */
public class DatenpumpeAdd extends AbstractAddStepHandler {
    public static final DatenpumpeAdd INSTANCE = new DatenpumpeAdd();

    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
        Attributes.HOST.validateAndSet(operation, model);
        Attributes.JNDI_NAME.validateAndSet(operation, model);
        if (operation.hasDefined(Attributes.PORT.getName())) {
            Attributes.PORT.validateAndSet(operation, model);
        }
    }

    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model,
            ServiceVerificationHandler verificationHandler, List<ServiceController<?>> controllers)
            throws OperationFailedException {

        final PathAddress address = PathAddress.pathAddress(operation.get(OP_ADDR));
        ModelNode fullTree = Resource.Tools.readModel(context.readResource(PathAddress.EMPTY_ADDRESS));
        installRuntimeServices(context, address, fullTree, verificationHandler, controllers);
    }

    public static String getJndiName(final String rawJndiName) {
        final String jndiName;
        if (!rawJndiName.startsWith("java:")) {
            jndiName = "java:jboss/logstash-gelf/" + rawJndiName;
        } else {
            jndiName = rawJndiName;
        }
        return jndiName;
    }

    static void installRuntimeServices(OperationContext context, PathAddress address, ModelNode model,
            ServiceVerificationHandler verificationHandler, List<ServiceController<?>> controllers)
            throws OperationFailedException {

        final ServiceTarget serviceTarget = context.getServiceTarget();
        final GelfSenderServiceConfiguration configuration = getGelfSenderServiceConfiguration(context, address
                .getLastElement().getValue(), model);

        DatenpumpeManagedReferenceFactory factory = new DatenpumpeManagedReferenceFactory(configuration);
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
                                LogstashGelfExtensionLogger.ROOT_LOGGER.boundItem("Datenpumpe", configuration.getJndiName());
                                break;
                            }
                            case START_REQUESTED_to_DOWN: {
                                LogstashGelfExtensionLogger.ROOT_LOGGER.unboundItem("Datenpumpe", configuration.getJndiName());
                                break;
                            }
                            case REMOVING_to_REMOVED: {
                                LogstashGelfExtensionLogger.ROOT_LOGGER.removedItem("Datenpumpe", configuration.getJndiName());
                                break;
                            }
                        }
                    }
                });

        binderBuilder.setInitialMode(ServiceController.Mode.ACTIVE).addListener(verificationHandler);
        controllers.add(binderBuilder.install());

    }

    private static GelfSenderServiceConfiguration getGelfSenderServiceConfiguration(OperationContext context, String name,
            ModelNode model) throws OperationFailedException {
        String host = Attributes.HOST.resolveModelAttribute(context, model).asString();
        int port = Attributes.PORT.resolveModelAttribute(context, model).asInt();
        String jndiName = getJndiName(model, context);

        GelfSenderServiceConfiguration configuration = new GelfSenderServiceConfiguration();
        configuration.setHost(host);
        configuration.setPort(port);
        configuration.setJndiName(jndiName);

        return configuration;
    }

    public static String getJndiName(ModelNode model, OperationContext context) throws OperationFailedException {
        return getJndiName(Attributes.JNDI_NAME.resolveModelAttribute(context, model).asString());
    }
}
