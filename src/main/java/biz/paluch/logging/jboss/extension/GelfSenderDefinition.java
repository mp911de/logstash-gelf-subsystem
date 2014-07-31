package biz.paluch.logging.jboss.extension;

import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.registry.ManagementResourceRegistration;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 29.07.14 20:42
 */
public class GelfSenderDefinition extends SimpleResourceDefinition {

    static final GelfSenderDefinition INSTANCE = new GelfSenderDefinition();

    private static final AttributeDefinition[] ATTRIBUTES = { Attributes.JNDI_NAME, Attributes.HOST, Attributes.PORT };

    private GelfSenderDefinition() {
        super(PathElement.pathElement(ModelConstants.SENDER), SubsystemExtension
                .getResourceDescriptionResolver(ModelConstants.SENDER),
        // We always need to add an 'add' operation
                GelfSenderAdd.INSTANCE,
                // Every resource that is added, normally needs a remove operation
                GenericRemove.INSTANCE);
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        GelfSenderWriteAttributeHandler handler = new GelfSenderWriteAttributeHandler(ATTRIBUTES);
        for (AttributeDefinition attribute : ATTRIBUTES) {
            resourceRegistration.registerReadWriteAttribute(attribute, null, handler);
        }
    }
}
