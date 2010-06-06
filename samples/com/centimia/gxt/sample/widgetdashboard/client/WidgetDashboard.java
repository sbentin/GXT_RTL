/*
 * Ext GWT - Ext for GWT
 * Copyright(c) 2007-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */
package com.centimia.gxt.sample.widgetdashboard.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.samples.client.ExampleService;
import com.extjs.gxt.samples.client.ExampleServiceAsync;
import com.extjs.gxt.samples.client.ExamplesModel;
import com.extjs.gxt.samples.client.FileService;
import com.extjs.gxt.samples.client.FileServiceAsync;
import com.extjs.gxt.samples.client.examples.model.Category;
import com.extjs.gxt.samples.client.examples.model.Entry;
import com.extjs.gxt.samples.resources.client.Resources;
import com.extjs.gxt.themes.client.Slate;
import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.core.FastMap;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.TreePanelEvent;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.ThemeManager;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.HtmlContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.custom.ThemeSelector;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.HeadElement;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.sample.showcase.client.StyleSheetLoader;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.RootPanel;

public class WidgetDashboard implements EntryPoint, Command, Listener<TreePanelEvent<ModelData>> {

	public static boolean isExplorer() {
		String test = Window.Location.getPath();
		if (test.indexOf("pages") != -1) {
			return false;
		}
		return true;
	}

	public static final String SERVICE = "service";
	public static final String FILE_SERVICE = "fileservice";
	public static final String MODEL = "model";

	private Viewport viewport;
	private TreePanel<ModelData> tree;
	private TabPanel panel = new TabPanel();
	private Map<String, Entry> examples = new FastMap<Entry>();
	private ContentPanel demoPanel;
	
	public void onModuleLoad() {
		String styleSheetName = "resources/css/gxt-all.css";
		if (LocaleInfo.getCurrentLocale().isRTL()) {
			styleSheetName = "resources/css/gxt-all-rtl.css";
		}
		
		 // Find existing style sheets that need to be removed
		boolean styleSheetsFound = false;
		final HeadElement headElem = StyleSheetLoader.getHeadElement();
		final List<Element> toRemove = new ArrayList<Element>();
		NodeList<Node> children = headElem.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.getItem(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element elem = Element.as(node);
				if (elem.getTagName().equalsIgnoreCase("link") && elem.getPropertyString("rel").equalsIgnoreCase("stylesheet")) {
					styleSheetsFound = true;
					String href = elem.getPropertyString("href");
					// If the correct style sheets are already loaded, then we should have
					// nothing to remove.
					if (!href.equals(styleSheetName) && href.indexOf("gxt") != -1) {
						toRemove.add(elem);
					}
				}
			}
		}

		// Return if we already have the correct style sheets
		if (styleSheetsFound && toRemove.size() != 0) {
			// Remove the old style sheets
			for (Element elem : toRemove) {
				headElem.removeChild(elem);
			}
		}
		
		ExampleServiceAsync service = (ExampleServiceAsync) GWT.create(ExampleService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) service;
		String moduleRelativeURL = SERVICE;
		endpoint.setServiceEntryPoint(moduleRelativeURL);
		Registry.register(SERVICE, service);

		FileServiceAsync fileservice = (FileServiceAsync) GWT.create(FileService.class);
		endpoint = (ServiceDefTarget) fileservice;
		moduleRelativeURL = FILE_SERVICE;
		endpoint.setServiceEntryPoint(moduleRelativeURL);
		Registry.register(FILE_SERVICE, fileservice);

		ExamplesModel model = new ExamplesModel();
		for (int i = 0; i < model.getChildren().size(); i++) {
			Category cat = (Category) model.getChildren().get(i);
			for (int j = 0; j < cat.getChildren().size(); j++) {
				Entry entry = (Entry) cat.getChildren().get(j);
				examples.put(entry.getId(), entry);
			}
		}

		Registry.register(MODEL, model);
		viewport = new Viewport();
		
		BorderLayout layout = new BorderLayout();
		
		demoPanel = new ContentPanel();
		demoPanel.setLayout(new BorderLayout());
		
		viewport.setLayout(layout);
		
		createNorth();

		createTree();
		panel.setResizeTabs(true);
		panel.setMinTabWidth(115);
		panel.setAnimScroll(true);
		panel.setTabScroll(true);
		panel.setCloseContextMenu(true);
		
		replaceView("basicgrid");
		demoPanel.add(panel, new BorderLayoutData(LayoutRegion.CENTER));
		
		
		viewport.add(tree, new BorderLayoutData(LayoutRegion.WEST));
		viewport.add(demoPanel, new BorderLayoutData(LayoutRegion.CENTER));
		
		StyleSheetLoader.loadStyleSheet(styleSheetName, "ext-el-mask", this);
	}

	private void replaceView(String viewItem) {
		TabItem example = new TabItem("Example");
		example.setScrollMode(Scroll.AUTO);
		
		Entry entry = null;
		
		if (viewItem == null)
			entry = examples.get("combobox");
		else
			entry = examples.get(viewItem);
		
		if (entry == null) {
			return;
		}
		
		example.setTitle(entry.getName());
		example.setText(entry.getName());
		
		example.setClosable(true);
		
		if (entry.isFill()) {
			example.setLayout(new FitLayout());
			example.setScrollMode(Scroll.NONE);
		}

		panel.add(example);		
		example.add(entry.getExample());
		
		panel.setSelection(example);
	}
	
	private void createTree() {
		ExamplesModel model = Registry.get(MODEL);
  
		TreeStore<ModelData> store = new TreeStore<ModelData>();  
		store.add(model.getChildren(), true);
		
		tree = new TreePanel<ModelData>(store);  
		tree.setWidth(200);  
		tree.setDisplayProperty("name");  
		tree.getStyle().setLeafIcon(Resources.ICONS.table());
		
		tree.addListener(Events.OnClick, this);
	}

	public void execute() {
		RootPanel.getBodyElement().getStyle().setProperty("display", "none");
		RootPanel.getBodyElement().getStyle().setProperty("display", "");
		// Associate the Main panel with the HTML host page.
		RootPanel.get("viewport").add(viewport);
	}
	
	private void createNorth() {
		StringBuffer sb = new StringBuffer();
		sb.append("<div id='demo-header' class='x-small-editor'><div id=demo-title>Ext GWT Examples</div></div>");

		HtmlContainer northPanel = new HtmlContainer(sb.toString());
		northPanel.setStateful(false);

		BorderLayoutData data = new BorderLayoutData(LayoutRegion.NORTH, 33);
		data.setMargins(new Margins());
		viewport.add(northPanel, data);
	}

	public void handleEvent(TreePanelEvent<ModelData> be) {
		if (be.getItem() instanceof Entry)
			replaceView(((Entry) be.getItem()).getId());			
	}
}