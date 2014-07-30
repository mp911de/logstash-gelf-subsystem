package biz.paluch.logging.jboss.extension;

import org.jboss.as.controller.SimpleResourceDefinition;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 29.07.14 20:42
 */
public class DatenpumpeDefinition extends SimpleResourceDefinition
{


    private DatenpumpeDefinition() {
        super(SubsystemExtension.SUBSYSTEM_PATH,
                SubsystemExtension.getResourceDescriptionResolver(ModelConstants.DATENPUMPE),
                //We always need to add an 'add' operation
                DatenpumpeAdd.INSTANCE,
                //Every resource that is added, normally needs a remove operation
                DatenpumpeRemove.INSTANCE);
    }
}
