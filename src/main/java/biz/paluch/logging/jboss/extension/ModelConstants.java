package biz.paluch.logging.jboss.extension;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 29.07.14 21:05
 */
public interface ModelConstants
{
     /**
     * The name space used for the {@code substystem} element
     */
    public static final String NAMESPACE = "urn:biz.paluch.logging:logstash-gelf-jboss-subsystem:1.0";

    /**
     * The name of our subsystem within the model.
     */
    public static final String SUBSYSTEM_NAME = "logstash-gelf-jboss-subsystem";
    public static final String DATENPUMPE = "datenpumpe";

    public static final String NAME = "name";
    public static final String HOST = "host";
    public static final String PORT = "port";
    public static final String JNDI_NAME = "jndi-name";
}