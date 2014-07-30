package biz.paluch.logging.jboss.extension;

import java.util.List;
import java.util.Locale;

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.msc.service.ServiceController;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OPERATION_NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REQUEST_PROPERTIES;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REQUIRED;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.TYPE;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 29.07.14 20:45
 */
public class DatenpumpeAdd extends AbstractAddStepHandler  implements DescriptionProvider
{
    public static final DatenpumpeAdd INSTANCE = new DatenpumpeAdd();

    @Override
    public ModelNode getModelDescription(Locale locale)
    {
    ModelNode node = new ModelNode();
    node.get(DESCRIPTION).set("Adds a job acquisition");
    node.get(OPERATION_NAME).set(ADD);

    node.get(REQUEST_PROPERTIES, NAME, DESCRIPTION).set("Name of job acquisition thread");
    node.get(REQUEST_PROPERTIES, NAME, TYPE).set(ModelType.STRING);
    node.get(REQUEST_PROPERTIES, NAME, REQUIRED).set(true);

    node.get(REQUEST_PROPERTIES, ACQUISITION_STRATEGY, DESCRIPTION).set("Job acquisition strategy");
    node.get(REQUEST_PROPERTIES, ACQUISITION_STRATEGY, TYPE).set(ModelType.STRING);
    node.get(REQUEST_PROPERTIES, ACQUISITION_STRATEGY, REQUIRED).set(false);

    node.get(REQUEST_PROPERTIES, PROPERTIES, DESCRIPTION).set("Additional properties");
    node.get(REQUEST_PROPERTIES, PROPERTIES, TYPE).set(ModelType.OBJECT);
    node.get(REQUEST_PROPERTIES, PROPERTIES, VALUE_TYPE).set(ModelType.LIST);
    node.get(REQUEST_PROPERTIES, PROPERTIES, REQUIRED).set(false);

    }


    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException
    {
        DatenpumpeDefinition.HOST.validateAndSet(operation,model);
        DatenpumpeDefinition.NAME.validateAndSet(operation,model);
        DatenpumpeDefinition.JNDI_NAME.validateAndSet(operation,model);
        
        if(operation.hasDefined(DatenpumpeDefinition.PORT.getName()))
        {
            DatenpumpeDefinition.PORT.validateAndSet(operation, model);
        }
        
    }

    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model,
            ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
            throws OperationFailedException {
        String suffix = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS)).getLastElement().getValue();
        long tick = TICK.resolveModelAttribute(context,model).asLong();
        TrackerService service = new TrackerService(suffix, tick);
        ServiceName name = TrackerService.createServiceName(suffix);
        ServiceController<TrackerService> controller = context.getServiceTarget()
                .addService(name, service)
                .addListener(verificationHandler)
                .setInitialMode(Mode.ACTIVE)
                .install();
        newControllers.add(controller);
    }
}
