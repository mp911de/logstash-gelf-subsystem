package biz.paluch.logging.jboss.extension;

import org.jboss.logging.*;

@MessageLogger(projectCode = "LSGELF")
interface LogstashGelfExtensionLogger extends BasicLogger {

    /**
     * A logger with a category of the package name.
     */
    LogstashGelfExtensionLogger ROOT_LOGGER = Logger.getMessageLogger(LogstashGelfExtensionLogger.class,
            "biz.paluch.logging.jboss.extension");

    /**
     * Logs an info message indicating an item was bound into JNDI.
     *
     * @param item the item name.
     * @param jndiName the JNDI name under which the session was bound.
     */
    @LogMessage()
    @Message(id = 1, value = "Bound %s session [%s]")
    void boundItem(String item, String jndiName);

    /**
     * Logs an info message indicating an item was unbound from JNDI.
     *
     * @param item the item name.
     * @param jndiName the JNDI name under which the session was bound.
     */
    @LogMessage()
    @Message(id = 2, value = "Unbound %s session [%s]")
    void unboundItem(String item, String jndiName);

    /**
     * Logs a debug message indicating an item was removed.
     *
     * @param item the item name.
     * @param jndiName the JNDI name under which the session had been bound.
     */
    @LogMessage()
    @Message(id = 3, value = "Removed %s [%s]")
    void removedItem(String item, String jndiName);

}
