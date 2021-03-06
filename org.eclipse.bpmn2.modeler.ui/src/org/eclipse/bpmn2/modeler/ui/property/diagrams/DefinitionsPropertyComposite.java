package org.eclipse.bpmn2.modeler.ui.property.diagrams;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.bpmn2.Bpmn2Package;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.DocumentRoot;
import org.eclipse.bpmn2.Import;
import org.eclipse.bpmn2.impl.DefinitionsImpl;
import org.eclipse.bpmn2.modeler.core.utils.ImportUtil;
import org.eclipse.bpmn2.modeler.core.utils.NamespaceUtil;
import org.eclipse.bpmn2.modeler.ui.property.AbstractBpmn2PropertiesComposite;
import org.eclipse.bpmn2.modeler.ui.property.AbstractBpmn2PropertySection;
import org.eclipse.bpmn2.modeler.ui.property.AbstractBpmn2TableComposite;
import org.eclipse.bpmn2.modeler.ui.property.DefaultPropertiesComposite;
import org.eclipse.bpmn2.modeler.ui.property.dialogs.NamespacesEditingDialog;
import org.eclipse.bpmn2.modeler.ui.property.dialogs.SchemaImportDialog;
import org.eclipse.bpmn2.modeler.ui.property.editors.ObjectEditor;
import org.eclipse.bpmn2.modeler.ui.property.editors.TextAndButtonObjectEditor;
import org.eclipse.bpmn2.modeler.ui.property.editors.TextObjectEditor;
import org.eclipse.bpmn2.modeler.ui.util.PropertyUtil;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreEMap;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class DefinitionsPropertyComposite extends DefaultPropertiesComposite  {

	public DefinitionsPropertyComposite(Composite parent, int style) {
		super(parent, style);
	}

	private AbstractPropertiesProvider propertiesProvider;
	private NamespacesTable namespacesTable;
	
	/**
	 * @param section
	 */
	public DefinitionsPropertyComposite(AbstractBpmn2PropertySection section) {
		super(section);
	}

	@Override
	public AbstractPropertiesProvider getPropertiesProvider(EObject object) {
		if (propertiesProvider==null) {
			propertiesProvider = new AbstractPropertiesProvider(object) {
				String[] properties = new String[] {
						"name",
						"targetNamespace",
						"typeLanguage",
						"expressionLanguage",
						"exporter",
						"exporterVersion",
						"imports",
						"relationships"
				};
				
				@Override
				public String[] getProperties() {
					return properties; 
				}
			};
		}
		return propertiesProvider;
	}

	@Override
	public void createBindings(EObject be) {
		super.createBindings(be);
		if (namespacesTable!=null)
			namespacesTable.dispose();
		
		namespacesTable = new NamespacesTable(propertySection);
		DefinitionsImpl definitions = (DefinitionsImpl)getEObject();
		DocumentRoot root = (DocumentRoot) definitions.eContainer();
		namespacesTable.bindList(root, Bpmn2Package.eINSTANCE.getDocumentRoot_XMLNSPrefixMap());
		namespacesTable.setTitle("Namespaces");
	}

	@Override
	protected AbstractBpmn2TableComposite bindList(EObject object, EStructuralFeature feature) {
		if (modelEnablement.isEnabled(object.eClass(), feature)) {
			if ("imports".equals(feature.getName())) {
				ImportsTable importsTable = new ImportsTable(propertySection);
				importsTable.bind();
				return importsTable;
			}
			else if ("relationships".equals(feature.getName())) {
				AbstractBpmn2TableComposite table = new AbstractBpmn2TableComposite(propertySection, AbstractBpmn2TableComposite.DEFAULT_STYLE);
				table.bindList(getEObject(), feature);
				return table;
			}
			else {
				return super.bindList(object, feature);
			}
		}
		return null;
	}

	public class NamespacesTable extends AbstractBpmn2TableComposite {
		
		public NamespacesTable(AbstractBpmn2PropertySection section) {
			super(section, ADD_BUTTON | REMOVE_BUTTON | SHOW_DETAILS);
		}

		
		@Override
		protected EObject addListItem(EObject object, EStructuralFeature feature) {
			DocumentRoot root = (DocumentRoot)object;
			Map<String,String> map = root.getXMLNSPrefixMap();
			NamespacesEditingDialog dialog = new NamespacesEditingDialog(getShell(), "Create New Namespace", map, "","");
			if (dialog.open() == Window.OK) {
				System.out.println(dialog.getPrefix()+" : "+dialog.getNamespace());
				map.put(dialog.getPrefix(), dialog.getNamespace());
			}
			return null;
		}

		@Override
		protected Object removeListItem(EObject object, EStructuralFeature feature, int index) {
			DocumentRoot root = (DocumentRoot)object;
			Map<String,String> map = root.getXMLNSPrefixMap();
			for ( Map.Entry<String, String> entry : map.entrySet() ) {
				if (index-- == 0) {
					map.remove( entry.getKey() );
					break;
				}
			}
			return null;
		}

		@Override
		public AbstractBpmn2PropertiesComposite createDetailComposite(final Composite parent, Class eClass) {
			detailSection.setText("Namespace Details");
			AbstractBpmn2PropertiesComposite composite = new DefaultPropertiesComposite(parent, SWT.NONE) {
				
				@Override
				protected void bindAttribute(Composite parent, EObject object, EAttribute attribute, String label) {
					if (attribute.getName().equals("key")) {
						ObjectEditor editor = new TextAndButtonObjectEditor(this,be,attribute) {

							@Override
							protected void buttonClicked() {
								Map.Entry<String, String> entry = (Map.Entry<String, String>)object;
								DocumentRoot root = (DocumentRoot)object.eContainer();
								Map<String, String> map = (Map<String, String>)root.getXMLNSPrefixMap();
								NamespacesEditingDialog dialog = new NamespacesEditingDialog(getShell(), "Change Namespace Prefix", map, entry.getKey(),null);
								if (dialog.open() == Window.OK) {
									updateObject(dialog.getPrefix());
								}
							}
							
							@Override
							protected boolean updateObject(final Object result) {
								// we can't just change the key because the map that contains it
								// needs to be updated, so remove old key, then add new.
								if (result instanceof String && !((String)result).isEmpty() ) {
									final Map.Entry<String, String> entry = (Map.Entry<String, String>)object;
									final String oldKey = entry.getKey();
									TransactionalEditingDomain domain = getDiagramEditor().getEditingDomain();
									domain.getCommandStack().execute(new RecordingCommand(domain) {
										@Override
										protected void doExecute() {
											DocumentRoot root = (DocumentRoot)object.eContainer();
											Map<String, String> map = (Map<String, String>)root.getXMLNSPrefixMap();
											String value = map.remove(oldKey);
											map.put((String)result, value);
										}
									});
									return true;
								}
								text.setText(PropertyUtil.getText(object, feature));
								return false;
							}
						};
						editor.createControl(parent,"Prefix",SWT.NONE);
					}
					else {
						ObjectEditor editor = new TextObjectEditor(this,be,attribute);
						editor.createControl(parent,"Namespace",SWT.NONE);
					}
				}
			};
			return composite;
		}

		@Override
		public TableContentProvider getContentProvider(EObject object, EStructuralFeature feature, EList<EObject>list) {
			if (contentProvider==null) {
				contentProvider = new TableContentProvider(object, feature, list) {

					@Override
					public Object[] getElements(Object inputElement) {
						List<Object> elements = new ArrayList<Object>();
						EcoreEMap<String,String> map = (EcoreEMap<String,String>)inputElement;
						for ( Map.Entry<String, String> entry : map.entrySet() ) {
							elements.add(entry);
						}
						return elements.toArray(new EObject[elements.size()]);
					}

				};
			}
			return contentProvider;
		}
		
		@Override
		protected int createColumnProvider(EObject theobject, EStructuralFeature thefeature) {
			if (columnProvider==null) {
				columnProvider = getColumnProvider(theobject, thefeature);
			}
			return columnProvider.getColumns().size();
		}
		
		@Override
		public AbstractTableColumnProvider getColumnProvider(EObject object, EStructuralFeature feature) {
			if (columnProvider==null) {
				columnProvider = new AbstractTableColumnProvider() {
					@Override
					public boolean canModify(EObject object, EStructuralFeature feature, EObject item) {
						return false;
					}
				};
				columnProvider.add(new NamespacesTableColumn(object, 0));
				columnProvider.add(new NamespacesTableColumn(object, 1));
			}
			return columnProvider;
		}
		
		public class NamespacesTableColumn extends TableColumn {
			
			int columnIndex;
			
			public NamespacesTableColumn(EObject object, int columnIndex) {
				super(object,null);
				this.columnIndex = columnIndex;
			}

			@Override
			public String getProperty() {
				return getHeaderText();
			}

			@Override
			public String getHeaderText() {
				if (columnIndex==0)
					return "Prefix";
				return "Namespace";
			}

			@Override
			public String getText(Object element) {
				Map.Entry<String, String> entry = (Map.Entry<String, String>)element;
				if (columnIndex==0)
					return entry.getKey();
				return entry.getValue();
			}
		}
	}
	
	public class ImportsTable extends AbstractBpmn2TableComposite {

		/**
		 * @param parent
		 * @param style
		 */
		public ImportsTable(AbstractBpmn2PropertySection section) {
			super(section, DEFAULT_STYLE);
		}


		public void bind() {
			DefinitionsImpl definitions = (DefinitionsImpl)getEObject();
			EStructuralFeature imports = definitions.eClass().getEStructuralFeature("imports");
			
			super.bindList(definitions, imports);
		}

		
		@Override
		public AbstractTableColumnProvider getColumnProvider(EObject object, EStructuralFeature feature) {
			if (columnProvider==null) {
				columnProvider = new AbstractTableColumnProvider() {
					@Override
					public boolean canModify(EObject object, EStructuralFeature feature, EObject item) {
						return false;
					}
				};
				
				// add a namespace prefix column that does NOT come from the Import object
				TableColumn tableColumn = new TableColumn(object,null) {
					@Override
					public String getHeaderText() {
						return "Namespace Prefix";
					}
	
					@Override
					public String getText(Object element) {
						Import imp = (Import)element;
						String prefix = NamespaceUtil.getPrefixForNamespace(imp.eResource(), imp.getNamespace());
						if (prefix!=null)
							return prefix;
						return "";
					}
				};
				columnProvider.add(tableColumn);
				// add remaining columns
				EClass eClass = PACKAGE.getImport();
				columnProvider.add(new TableColumn(object,
						(EAttribute)eClass.getEStructuralFeature("namespace")));
				columnProvider.add(new TableColumn(object,
						(EAttribute)eClass.getEStructuralFeature("location")));
				columnProvider.add(new TableColumn(object,
						(EAttribute)eClass.getEStructuralFeature("importType")));
			}
			return columnProvider;
		}


		@Override
		protected EObject editListItem(EObject object, EStructuralFeature feature) {
			return super.editListItem(object, feature);
		}


		@Override
		protected Object removeListItem(EObject object, EStructuralFeature feature, int index) {
			EList<Import> list = (EList<Import>)object.eGet(feature);
			Import imp = list.get(index);
			ImportUtil.removeImport(imp);
//			return super.removeListItem(object, feature, index);
			return null;
		}

		@Override
		protected EObject addListItem(EObject object, EStructuralFeature feature) {
			SchemaImportDialog dialog = new SchemaImportDialog(getShell());
			if (dialog.open() == Window.OK) {
				Object result[] = dialog.getResult();
				if (result.length == 1) {
					return ImportUtil.addImport(object, result[0]);
				}
			}
			return null;
		}
	}
	
	public class ImportPropertiesComposite extends DefaultPropertiesComposite {

		private Text text;
		private Button button;
		
		public ImportPropertiesComposite(Composite parent, int style) {
			super(parent, style);
		}

		/**
		 * @param section
		 */
		public ImportPropertiesComposite(AbstractBpmn2PropertySection section) {
			super(section);
		}
		
		@Override
		public void createBindings(EObject be) {
			final Import imp = (Import)be;
			
			Composite composite = getAttributesParent();
			TextAndButtonObjectEditor editor = new TextAndButtonObjectEditor(this,be,null) {

				@Override
				protected void buttonClicked() {
					IInputValidator validator = new IInputValidator() {

						@Override
						public String isValid(String newText) {
							String ns = NamespaceUtil.getNamespaceForPrefix(imp.eResource(), newText);
							if (ns==null)
								return null;
							return "Prefix "+newText+" is already used for namespace\n"+ns;
						}
						
					};
					String initialValue = getText();
					InputDialog dialog = new InputDialog(
							getShell(),
							"Namespace Prefix",
							"Enter a namespace prefix",
							initialValue,
							validator);
					if (dialog.open()==Window.OK){
						updateObject(dialog.getValue());
					}
				}
				
				protected boolean updateObject(final Object value) {
					// remove old prefix
					String prefix = text.getText();
					NamespaceUtil.removeNamespaceForPrefix(imp.eResource(), prefix);
					// and add new
					NamespaceUtil.addNamespace(imp.eResource(), (String)value, imp.getNamespace());
					setText((String) value);
					return true;
				}
				
				protected String getText() {
					return getNamespacePrefix();
				}
			};
			editor.createControl(composite,"Namespace Prefix",SWT.NONE);
			
			super.createBindings(be);
		}
		
		private String getNamespacePrefix() {
			Import imp = (Import)be;
			String prefix = NamespaceUtil.getPrefixForNamespace(imp.eResource(), imp.getNamespace());
			if (prefix==null)
				prefix = "";
			return prefix;
		}
	}
}
