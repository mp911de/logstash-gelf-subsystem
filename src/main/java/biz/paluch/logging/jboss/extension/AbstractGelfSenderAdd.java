package biz.paluch.logging.jboss.extension;

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.dmr.ModelNode;

/**
 * @author <a href="mailto:mark.paluch@1und1.de">Mark Paluch</a>
 * @since 31.07.14 09:26
 */
public class AbstractGelfSenderAdd extends AbstractAddStepHandler {
    public static String getJndiName(final String rawJndiName) {
        final String jndiName;
        if (!rawJndiName.startsWith("java:")) {
            jndiName = "java:jboss/logstash-gelf/" + rawJndiName;
        } else {
            jndiName = rawJndiName;
        }
        return jndiName;
    }

    protected static GelfSenderServiceConfiguration getGelfSenderServiceConfiguration(OperationContext context, String name,
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

    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
        Attributes.HOST.validateAndSet(operation, model);
        Attributes.JNDI_NAME.validateAndSet(operation, model);
        if (operation.hasDefined(Attributes.PORT.getName())) {
            Attributes.PORT.validateAndSet(operation, model);
        }
    }
}
