/*******************************************************************************
 * Copyright (c) 2011 Red Hat, Inc.
 *  All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 *
 * @author Bob Brodt
 ******************************************************************************/

package org.eclipse.bpmn2.modeler.ui.adapters.properties;

import org.eclipse.bpmn2.Bpmn2Package;
import org.eclipse.bpmn2.RootElement;
import org.eclipse.bpmn2.modeler.core.adapters.ExtendedPropertiesAdapter;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

/**
 * @author Bob Brodt
 *
 */
public class RootElementPropertiesAdapter extends ExtendedPropertiesAdapter {

	/**
	 * @param adapterFactory
	 * @param object
	 */
	public RootElementPropertiesAdapter(AdapterFactory adapterFactory, EObject object) {
		super(adapterFactory, object);
		
		// create a Root Element feature descriptor for every feature that is a reference to a RootElement
		// the RootElementFeatureDescriptor is used to construct RootElement objects via its createObject()
		// method, and inserts those objects into the document root.
		EList<EStructuralFeature>list = object.eClass().getEAllStructuralFeatures();
		for (EStructuralFeature ref : list) {
			EClassifier type = ref.getEType();
			if (type instanceof EClass) {
				EList<EClass> supertypes = ((EClass)type).getEAllSuperTypes();
				for (EClass st : supertypes) {
					if (st == Bpmn2Package.eINSTANCE.getRootElement()) {
						setFeatureDescriptor(ref, new RootElementRefFeatureDescriptor(adapterFactory,object,ref));
					}
				}
			}
		}
	}

}
