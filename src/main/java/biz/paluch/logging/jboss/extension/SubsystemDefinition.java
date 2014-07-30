package biz.paluch.logging.jboss.extension;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIBE;

import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.operations.common.GenericSubsystemDescribeHandler;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.OperationEntry;

/**
 * @author <a href="mailto:tcerar@redhat.com">Tomaz Cerar</a>
 */
public class SubsystemDefinition extends SimpleResourceDefinition {
    public static final SubsystemDefinition INSTANCE = new SubsystemDefinition();

    private SubsystemDefinition() {
        super(SubsystemExtension.SUBSYSTEM_PATH, SubsystemExtension.getResourceDescriptionResolver(null),
        // We always need to add an 'add' operation
                SubsystemAdd.INSTANCE,
                // Every resource that is added, normally needs a remove operation
                SubsystemRemove.INSTANCE);
    }

    @Override
    public void registerOperations(ManagementResourceRegistration resourceRegistration) {
        super.registerOperations(resourceRegistration);
        // you can register aditional operations here
        resourceRegistration.registerOperationHandler(DESCRIBE, GenericSubsystemDescribeHandler.INSTANCE,
                GenericSubsystemDescribeHandler.INSTANCE, false, OperationEntry.EntryType.PRIVATE);

    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        // you can register attributes here
    }
}
