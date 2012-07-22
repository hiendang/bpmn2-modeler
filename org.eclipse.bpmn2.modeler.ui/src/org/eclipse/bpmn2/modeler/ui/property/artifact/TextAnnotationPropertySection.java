package org.eclipse.bpmn2.modeler.ui.property.artifact;

import org.eclipse.bpmn2.TextAnnotation;
import org.eclipse.bpmn2.modeler.ui.property.AbstractBpmn2PropertiesComposite;
import org.eclipse.bpmn2.modeler.ui.property.AbstractBpmn2PropertySection;
import org.eclipse.bpmn2.modeler.ui.property.PropertiesCompositeFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
/**
 * 
 * @author hien quoc dang
 *
 */
public class TextAnnotationPropertySection extends AbstractBpmn2PropertySection implements ITabbedPropertyConstants{
	static {
		PropertiesCompositeFactory.register(TextAnnotation.class, TextAnnotationPropertiesComposite.class);
	}

	@Override
	protected AbstractBpmn2PropertiesComposite createSectionRoot() {
		return new TextAnnotationPropertiesComposite(this);
	}
	
	@Override
	protected EObject getBusinessObjectForPictogramElement(PictogramElement pe) {
		EObject be = (EObject) Graphiti.getLinkService().getBusinessObjectForLinkedPictogramElement(pe);
		if (be instanceof TextAnnotation)
			return be;
		return null;
	}
}
