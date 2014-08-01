package biz.paluch.logging.jboss.extension;

import org.jboss.as.naming.ManagedReference;
import org.jboss.as.naming.ManagedReferenceFactory;
import org.jboss.as.naming.ValueManagedReference;
import org.jboss.msc.value.ImmediateValue;

import biz.paluch.logging.gelf.standalone.DatenpumpeImpl;
import biz.paluch.logging.gelf.standalone.DefaultGelfSenderConfiguration;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 30.07.14 08:39
 */
public class DatenpumpeManagedReferenceFactory implements ManagedReferenceFactory {
    private GelfSenderServiceConfiguration configuration;

    public DatenpumpeManagedReferenceFactory(GelfSenderServiceConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public ManagedReference getReference() {
        DefaultGelfSenderConfiguration senderConfiguration = new DefaultGelfSenderConfiguration();
        senderConfiguration.setHost(configuration.getHost());
        senderConfiguration.setPort(configuration.getPort());

        final DatenpumpeImpl datenpumpe = new DatenpumpeImpl(senderConfiguration);

        return new ValueManagedReference(new ImmediateValue<Object>(datenpumpe)) {
            @Override
            public void release() {
                datenpumpe.close();
            }
        };
    }
}
