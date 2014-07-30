package biz.paluch.logging.jboss.extension;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;
import org.jboss.as.controller.*;
import org.jboss.as.controller.descriptions.StandardResourceDescriptionResolver;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceName;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

import javax.xml.stream.XMLStreamConstants;
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
        context.setSubsystemXmlMapping(ModelConstants.SUBSYSTEM_NAME, ModelConstants.NAMESPACE, parser);
    }

    @Override
    public void initialize(ExtensionContext context) {
        final SubsystemRegistration subsystem = context.registerSubsystem(ModelConstants.SUBSYSTEM_NAME, 1, 0);
        final ManagementResourceRegistration registration = subsystem.registerSubsystemModel(SubsystemDefinition.INSTANCE);
        /*
         * registration.registerOperationHandler(DESCRIBE, GenericSubsystemDescribeHandler.INSTANCE,
         * GenericSubsystemDescribeHandler.INSTANCE, false, OperationEntry.EntryType.PRIVATE);
         */

        registration.registerSubModel(DatenpumpeDefinition.INSTANCE);

        subsystem.registerXMLElementWriter(parser);
    }

    private static ModelNode createAddSubsystemOperation() {
        final ModelNode subsystem = new ModelNode();
        subsystem.get(OP).set(ADD);
        subsystem.get(OP_ADDR).add(SUBSYSTEM, ModelConstants.SUBSYSTEM_NAME);
        return subsystem;
    }

    /**
     * The subsystem parser, which uses stax to read and write to and from xml
     */
    private static class SubsystemParser implements XMLStreamConstants, XMLElementReader<List<ModelNode>>,
            XMLElementWriter<SubsystemMarshallingContext> {

        /**
         * {@inheritDoc}
         */
        @Override
        public void writeContent(XMLExtendedStreamWriter writer, SubsystemMarshallingContext context) throws XMLStreamException {
            context.startSubsystemElement(ModelConstants.NAMESPACE, false);
            writer.writeEndElement();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void readElement(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {
            // Require no content

            final PathAddress address = PathAddress.pathAddress(SUBSYSTEM_PATH);

            list.add(createAddSubsystemOperation());

            while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
                switch (Namespace.forUri(reader.getNamespaceURI())) {
                    case LOGSTASH_GELF_1_0: {
                        final Element element = Element.forName(reader.getLocalName());
                        switch (element) {
                            case DATENPUMPE: {
                                prepareDatenpumpe(reader, list, address);
                                ParseUtils.requireNoContent(reader);
                                break;
                            }

                            case SENDER: {
                                ParseUtils.requireNoContent(reader);
                                break;
                            }
                            default: {
                                reader.handleAny(list);
                                break;
                            }
                        }
                        break;
                    }
                    default:
                        throw ParseUtils.unexpectedElement(reader);
                }
            }
        }

    }

    private static void prepareDatenpumpe(final XMLExtendedStreamReader reader, List<ModelNode> list, final PathAddress parent)
            throws XMLStreamException {
        String name = null;
        String host = null;
        int port;
        String jndiName = null;
        final ModelNode operation = new ModelNode();

        for (int i = 0; i < reader.getAttributeCount(); i++) {

            String value = reader.getAttributeValue(i);
            String attributeName = reader.getAttributeLocalName(i);
            if (attributeName.equals(ModelConstants.NAME)) {
                name = value;
                Attributes.NAME.parseAndSetParameter(value, operation, reader);
            } else if (attributeName.equals(ModelConstants.HOST)) {
                Attributes.HOST.parseAndSetParameter(value, operation, reader);
                host = value;
            } else if (attributeName.equals(ModelConstants.PORT)) {
                Attributes.PORT.parseAndSetParameter(value, operation, reader);
                port = Integer.parseInt(value);
            } else if (attributeName.equals(ModelConstants.JNDI_NAME)) {
                Attributes.JNDI_NAME.parseAndSetParameter(value, operation, reader);
                jndiName = value;
            } else {
                throw ParseUtils.unexpectedElement(reader);
            }
        }

        if (name == null) {
            throw ParseUtils.missingRequired(reader, Collections.singleton(ModelConstants.NAME));
        }

        if (jndiName == null) {
            throw ParseUtils.missingRequired(reader, Collections.singleton(ModelConstants.JNDI_NAME));
        }

        final PathAddress address = parent.append(PathElement.pathElement(ModelConstants.DATENPUMPE, name));

        operation.get(OP_ADDR).set(address.toModelNode());
        operation.get(OP).set(ADD);
        list.add(operation);

    }

}
