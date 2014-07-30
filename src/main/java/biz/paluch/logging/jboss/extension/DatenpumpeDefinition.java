package biz.paluch.logging.jboss.extension;

import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.registry.ManagementResourceRegistration;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 29.07.14 20:42
 */
public class DatenpumpeDefinition extends SimpleResourceDefinition {

    static final DatenpumpeDefinition INSTANCE = new DatenpumpeDefinition();

    private static final AttributeDefinition[] ATTRIBUTES = { Attributes.JNDI_NAME, Attributes.HOST, Attributes.PORT };

    private DatenpumpeDefinition() {
        super(PathElement.pathElement(ModelConstants.DATENPUMPE), SubsystemExtension
                .getResourceDescriptionResolver(ModelConstants.DATENPUMPE),
        // We always need to add an 'add' operation
                DatenpumpeAdd.INSTANCE,
                // Every resource that is added, normally needs a remove operation
                DatenpumpeRemove.INSTANCE);
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        DatenpumpeWriteAttributeHandler handler = new DatenpumpeWriteAttributeHandler(ATTRIBUTES);
        for (AttributeDefinition attribute : ATTRIBUTES) {
            resourceRegistration.registerReadWriteAttribute(attribute, null, handler);
        }
    }
}
