package biz.paluch.logging.jboss.extension;

import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

import biz.paluch.logging.gelf.intern.sender.DefaultGelfSenderProvider;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 29.07.14 21:08
 */
public class Attributes {

    protected static final SimpleAttributeDefinition HOST = new SimpleAttributeDefinitionBuilder(ModelConstants.HOST,
            ModelType.STRING).setAllowExpression(true).setXmlName(ModelConstants.HOST)
            .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES).setAllowNull(false).build();

    protected static final SimpleAttributeDefinition JNDI_NAME = new SimpleAttributeDefinitionBuilder(ModelConstants.JNDI_NAME,
            ModelType.STRING).setAllowExpression(true).setXmlName(ModelConstants.JNDI_NAME)
            .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES).setAllowNull(false).build();

    protected static final SimpleAttributeDefinition PORT = new SimpleAttributeDefinitionBuilder(ModelConstants.PORT,
            ModelType.INT).setAllowExpression(true).setXmlName(ModelConstants.PORT)
            .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
            .setDefaultValue(new ModelNode(DefaultGelfSenderProvider.DEFAULT_PORT)).setAllowNull(true).build();
}
