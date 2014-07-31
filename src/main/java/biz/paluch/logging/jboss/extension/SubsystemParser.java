package biz.paluch.logging.jboss.extension;

import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.util.List;

/**
 * The subsystem parser, which uses stax to read and write to and from xml
 */
class SubsystemParser implements XMLStreamConstants, XMLElementReader<List<ModelNode>>,
        XMLElementWriter<SubsystemMarshallingContext> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeContent(XMLExtendedStreamWriter writer, SubsystemMarshallingContext context) throws XMLStreamException {
        context.startSubsystemElement(Namespace.CURRENT.getUriString(), false);

        ModelNode model = context.getModelNode();

        if (model.get(ModelConstants.DATENPUMPE).isDefined()) {
            List<Property> datenpumpen = model.get(ModelConstants.DATENPUMPE).asPropertyList();

            for (Property datenpumpe : datenpumpen) {
                ModelNode node = datenpumpe.getValue();
                writeNode(writer, node, Element.DATENPUMPE);
            }
        }

        if (model.get(ModelConstants.SENDER).isDefined()) {
            List<Property> gelfSenders = model.get(ModelConstants.SENDER).asPropertyList();

            for (Property gelfSender : gelfSenders) {
                ModelNode node = gelfSender.getValue();
                writeNode(writer, node, Element.SENDER);
            }
        }
        writer.writeEndElement();
    }

    private void writeNode(XMLExtendedStreamWriter writer, ModelNode node, Element element) throws XMLStreamException {
        writer.writeStartElement(element.getLocalName());

        Attributes.JNDI_NAME.marshallAsAttribute(node, writer);
        Attributes.HOST.marshallAsAttribute(node, writer);
        Attributes.PORT.marshallAsAttribute(node, writer);
        writer.writeEndElement();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readElement(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {
        // Require no content

        ModelNode subsystemAddress = SubsystemExtension.createSubsystemAddress();

        list.add(SubsystemExtension.createAddSubsystemOperation(subsystemAddress));

        while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
            switch (Namespace.forUri(reader.getNamespaceURI())) {
                case LOGSTASH_GELF_1_0: {
                    final Element element = Element.forName(reader.getLocalName());
                    switch (element) {
                        case DATENPUMPE: {
                            SubsystemExtension.prepareOperation(ModelConstants.DATENPUMPE, reader, list, subsystemAddress);
                            ParseUtils.requireNoContent(reader);
                            break;
                        }

                        case SENDER: {
                            SubsystemExtension.prepareOperation(ModelConstants.SENDER, reader, list, subsystemAddress);
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
