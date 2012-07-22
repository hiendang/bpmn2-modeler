package org.eclipse.bpmn2.modeler.ui.property.artifact;

import org.eclipse.bpmn2.modeler.ui.property.AbstractBpmn2PropertySection;
import org.eclipse.bpmn2.modeler.ui.property.DefaultPropertiesComposite;
import org.eclipse.bpmn2.modeler.ui.property.editors.ObjectEditor;
import org.eclipse.bpmn2.modeler.ui.property.editors.TextObjectEditor;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
/**
 * 
 * @author hien quoc dang
 *
 */
public class TextAnnotationPropertiesComposite extends
		DefaultPropertiesComposite {
	public TextAnnotationPropertiesComposite(Composite parent, int style) {
		super(parent, style);
	}

	public TextAnnotationPropertiesComposite(AbstractBpmn2PropertySection section) {
		super(section);
	}
	
	@Override
	public void createBindings(EObject be) {
		//bindAttribute(be,"text");
		ObjectEditor editor = new TextObjectEditor(this,be,be.eClass().getEStructuralFeature("text"));
		editor.createControl(getAttributesParent(),"Text",SWT.MULTI);
		//ObjectEditor editor = new TextObjectEditor(this,be,be.eClass().getEStructuralFeature("text"));
		//editor.createControl(getAttributesParent(),"Text",SWT.);
	}
}
