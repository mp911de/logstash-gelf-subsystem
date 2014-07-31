package biz.paluch.logging.jboss.extension;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;
import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.descriptions.StandardResourceDescriptionResolver;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceName;
import org.jboss.staxmapper.XMLExtendedStreamReader;

import javax.xml.stream.XMLStreamException;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 */
public class SubsystemExtension implements Extension {

    /**
     * The parser used for parsing our subsystem
     */
    private final SubsystemParser parser = new SubsystemParser();
    public final static ServiceName SERVICE_NAME = ServiceName.JBOSS.append(ModelConstants.SUBSYSTEM_NAME);

    protected static final PathElement SUBSYSTEM_PATH = PathElement.pathElement(SUBSYSTEM, ModelConstants.SUBSYSTEM_NAME);
    private static final String RESOURCE_NAME = SubsystemExtension.class.getPackage().getName() + ".LocalDescriptions";

    static StandardResourceDescriptionResolver getResourceDescriptionResolver(final String keyPrefix) {
        String prefix = ModelConstants.SUBSYSTEM_NAME + (keyPrefix == null ? "" : "." + keyPrefix);
        return new StandardResourceDescriptionResolver(prefix, RESOURCE_NAME, SubsystemExtension.class.getClassLoader(), true,
                false);
    }

    @Override
    public void initializeParsers(ExtensionParsingContext context) {
        context.setSubsystemXmlMapping(ModelConstants.SUBSYSTEM_NAME, Namespace.CURRENT.getUriString(), parser);
    }

    @Override
    public void initialize(ExtensionContext context) {
        final SubsystemRegistration subsystem = context.registerSubsystem(ModelConstants.SUBSYSTEM_NAME, 1, 0);
        final ManagementResourceRegistration registration = subsystem.registerSubsystemModel(SubsystemDefinition.INSTANCE);

        registration.registerSubModel(DatenpumpeDefinition.INSTANCE);
        registration.registerSubModel(GelfSenderDefinition.INSTANCE);

        subsystem.registerXMLElementWriter(parser);
    }

    public static ModelNode createSubsystemAddress() {
        final ModelNode address = new ModelNode();
        address.add(ModelDescriptionConstants.SUBSYSTEM, ModelConstants.SUBSYSTEM_NAME);
        address.protect();
        return address;
    }

    public static ModelNode createAddSubsystemOperation(ModelNode address) {
        final ModelNode subsystem = new ModelNode();
        subsystem.get(OP).set(ADD);
        subsystem.get(OP_ADDR).set(address);
        return subsystem;
    }

    public static void prepareOperation(String address, XMLExtendedStreamReader reader, List<ModelNode> list, ModelNode parent)
            throws XMLStreamException {
        String jndiName = null;
        final ModelNode operation = new ModelNode();

        for (int i = 0; i < reader.getAttributeCount(); i++) {

            String value = reader.getAttributeValue(i);
            String attributeName = reader.getAttributeLocalName(i);
            if (attributeName.equals(ModelConstants.HOST)) {
                Attributes.HOST.parseAndSetParameter(value, operation, reader);
            } else if (attributeName.equals(ModelConstants.PORT)) {
                Attributes.PORT.parseAndSetParameter(value, operation, reader);
            } else if (attributeName.equals(ModelConstants.JNDI_NAME)) {
                Attributes.JNDI_NAME.parseAndSetParameter(value, operation, reader);
                jndiName = value;
            } else {
                throw ParseUtils.unexpectedElement(reader);
            }
        }

        if (jndiName == null) {
            throw ParseUtils.missingRequired(reader, Collections.singleton(ModelConstants.JNDI_NAME));
        }

        final ModelNode dsAddress = parent.clone();
        dsAddress.add(address, jndiName);
        dsAddress.protect();

        operation.get(OP_ADDR).set(dsAddress);
        operation.get(OP).set(ADD);

        list.add(operation);

    }

}
