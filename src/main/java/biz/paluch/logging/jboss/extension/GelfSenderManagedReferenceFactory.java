package biz.paluch.logging.jboss.extension;

import org.jboss.as.naming.ManagedReference;
import org.jboss.as.naming.ManagedReferenceFactory;
import org.jboss.as.naming.ValueManagedReference;
import org.jboss.msc.value.ImmediateValue;

import biz.paluch.logging.gelf.intern.GelfSender;
import biz.paluch.logging.gelf.intern.GelfSenderFactory;
import biz.paluch.logging.gelf.standalone.DefaultGelfSenderConfiguration;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 30.07.14 08:39
 */
public class GelfSenderManagedReferenceFactory implements ManagedReferenceFactory {
    private GelfSenderServiceConfiguration configuration;

    public GelfSenderManagedReferenceFactory(GelfSenderServiceConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public ManagedReference getReference() {
        DefaultGelfSenderConfiguration senderConfiguration = new DefaultGelfSenderConfiguration();
        senderConfiguration.setHost(configuration.getHost());
        senderConfiguration.setPort(configuration.getPort());

        final GelfSender gelfSender = GelfSenderFactory.createSender(senderConfiguration);

        return new ValueManagedReference(new ImmediateValue<Object>(gelfSender)) {
            @Override
            public void release() {
                gelfSender.close();
            }
        };
    }
}
