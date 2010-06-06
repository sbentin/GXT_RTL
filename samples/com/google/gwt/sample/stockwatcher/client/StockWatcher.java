/*
 * Ext GWT - Ext for GWT
 * Copyright(c) 2007-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */
package com.google.gwt.sample.stockwatcher.client;

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

public class StockWatcher implements EntryPoint, Command, Listener<TreePanelEvent<ModelData>> {

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
		sb.append("<div id='demo-header' class='x-small-editor'><div id='demo-theme'></div><div id=demo-title>Ext GWT Examples</div></div>");

		HtmlContainer northPanel = new HtmlContainer(sb.toString());
		northPanel.setStateful(false);

		ThemeManager.register(Slate.SLATE);
		ThemeSelector selector = new ThemeSelector();
		selector.setWidth(125);
		northPanel.add(selector, "#demo-theme");

		BorderLayoutData data = new BorderLayoutData(LayoutRegion.NORTH, 33);
		data.setMargins(new Margins());
		viewport.add(northPanel, data);
	}

	public void handleEvent(TreePanelEvent<ModelData> be) {
		if (be.getItem() instanceof Entry)
			replaceView(((Entry) be.getItem()).getId());			
	}
}

// package com.google.gwt.sample.stockwatcher.client;
//
// import java.util.ArrayList;
// import java.util.Date;
// import java.util.List;
//
// import com.extjs.gxt.samples.client.ExampleService;
// import com.extjs.gxt.samples.client.ExampleServiceAsync;
// import com.extjs.gxt.samples.client.ExamplesModel;
// import com.extjs.gxt.samples.client.FileService;
// import com.extjs.gxt.samples.client.FileServiceAsync;
// import com.extjs.gxt.samples.client.examples.binding.BasicBindingExample;
// import com.extjs.gxt.samples.client.examples.binding.GridBindingExample;
// import com.extjs.gxt.samples.client.examples.binding.GridStoreBindingExample;
// import com.extjs.gxt.samples.client.examples.button.ButtonAlignExample;
// import com.extjs.gxt.samples.client.examples.button.ButtonsExample;
// import com.extjs.gxt.samples.client.examples.core.TemplateExample;
// import com.extjs.gxt.samples.client.examples.dnd.BasicDNDExample;
// import com.extjs.gxt.samples.client.examples.dnd.DualListFieldExample;
// import com.extjs.gxt.samples.client.examples.dnd.GridToGridExample;
// import com.extjs.gxt.samples.client.examples.dnd.ListViewDNDExample;
// import com.extjs.gxt.samples.client.examples.dnd.MultiComponentExample;
// import com.extjs.gxt.samples.client.examples.dnd.ReorderingGridExample;
// import com.extjs.gxt.samples.client.examples.dnd.ReorderingTreeGridExample;
// import com.extjs.gxt.samples.client.examples.dnd.ReorderingTreePanelExample;
// import com.extjs.gxt.samples.client.examples.dnd.TreeGridToTreeGridExample;
// import com.extjs.gxt.samples.client.examples.dnd.TreePanelToTreePanelExample;
// import com.extjs.gxt.samples.client.examples.forms.AdvancedComboBoxExample;
// import com.extjs.gxt.samples.client.examples.forms.AdvancedFormsExample;
// import com.extjs.gxt.samples.client.examples.forms.ComboBoxExample;
// import com.extjs.gxt.samples.client.examples.forms.CustomFormExample;
// import com.extjs.gxt.samples.client.examples.forms.FileUploadExample;
// import com.extjs.gxt.samples.client.examples.forms.FormsExample;
// import com.extjs.gxt.samples.client.examples.grid.AggregationGridExample;
// import com.extjs.gxt.samples.client.examples.grid.AutoHeightGridExample;
// import com.extjs.gxt.samples.client.examples.grid.BufferedGridExample;
// import com.extjs.gxt.samples.client.examples.grid.CheckGroupingGridExample;
// import com.extjs.gxt.samples.client.examples.grid.ColumnGroupingExample;
// import com.extjs.gxt.samples.client.examples.grid.EditableGridExample;
// import com.extjs.gxt.samples.client.examples.grid.GridPluginsExample;
// import com.extjs.gxt.samples.client.examples.grid.GroupingGridExample;
// import com.extjs.gxt.samples.client.examples.grid.PagingGridExample;
// import com.extjs.gxt.samples.client.examples.grid.RowEditorExample;
// import com.extjs.gxt.samples.client.examples.grid.TotalsGridExample;
// import com.extjs.gxt.samples.client.examples.model.Category;
// import com.extjs.gxt.samples.client.examples.model.Entry;
// import com.extjs.gxt.samples.client.examples.tabs.BasicTabExample;
// import com.extjs.gxt.ui.client.Registry;
// import com.extjs.gxt.ui.client.event.ButtonEvent;
// import com.extjs.gxt.ui.client.event.SelectionListener;
// import com.extjs.gxt.ui.client.store.ListStore;
// import com.extjs.gxt.ui.client.widget.ContentPanel;
// import com.extjs.gxt.ui.client.widget.Label;
// import com.extjs.gxt.ui.client.widget.button.Button;
// import com.extjs.gxt.ui.client.widget.form.TextField;
// import com.extjs.gxt.ui.client.widget.grid.Grid;
// import com.google.gwt.core.client.EntryPoint;
// import com.google.gwt.core.client.GWT;
// import com.google.gwt.dom.client.Element;
// import com.google.gwt.dom.client.HeadElement;
// import com.google.gwt.dom.client.Node;
// import com.google.gwt.dom.client.NodeList;
// import com.google.gwt.i18n.client.LocaleInfo;
// import com.google.gwt.sample.showcase.client.StyleSheetLoader;
// import com.google.gwt.user.client.Command;
// import com.google.gwt.user.client.Random;
// import com.google.gwt.user.client.Window;
// import com.google.gwt.user.client.rpc.ServiceDefTarget;
// import com.google.gwt.user.client.ui.RootPanel;
// import com.google.gwt.user.client.ui.VerticalPanel;
//
// /**
// * Entry point classes define <code>onModuleLoad()</code>.
// */
// public class StockWatcher implements EntryPoint, Command {
//
// private static final int REFRESH_INTERVAL = 5000; // ms
//
// private VerticalPanel mainPanel = new VerticalPanel();
// // private FlexTable stocksFlexTable = new FlexTable();
// private Grid<StockPrice> grid = null;
// private ContentPanel addPanel = new ContentPanel();
// private TextField<String> newSymbolTextBox = new TextField<String>();
// private Button addStockButton = new Button("Add");
// private Label lastUpdatedLabel = new Label();
// // private ArrayList<String> stocks = new ArrayList<String>();
// private ListStore<StockPrice> stocks = new ListStore<StockPrice>();
// private StockWatcherConstants constants = GWT.create(StockWatcherConstants.class);
// private StockWatcherMessages messages = GWT.create(StockWatcherMessages.class);
//
// public static final String SERVICE = "service";
// public static final String FILE_SERVICE = "fileservice";
//	  
// /**
// * This is the entry point method.
// */
// public void onModuleLoad() {
// String styleSheetName = "resources/css/gxt-all.css";
// if (LocaleInfo.getCurrentLocale().isRTL()) {
// styleSheetName = "resources/css/gxt-all-rtl.css";
// }
//		
// ExampleServiceAsync service = (ExampleServiceAsync) GWT.create(ExampleService.class);
// ServiceDefTarget endpoint = (ServiceDefTarget) service;
// String moduleRelativeURL = SERVICE;
// endpoint.setServiceEntryPoint(moduleRelativeURL);
// Registry.register(SERVICE, service);
//	    
// FileServiceAsync fileservice = (FileServiceAsync) GWT.create(FileService.class);
// endpoint = (ServiceDefTarget) fileservice;
// moduleRelativeURL = FILE_SERVICE;
// endpoint.setServiceEntryPoint(moduleRelativeURL);
// Registry.register(FILE_SERVICE, fileservice);
//	    
// // Find existing style sheets that need to be removed
// boolean styleSheetsFound = false;
// final HeadElement headElem = StyleSheetLoader.getHeadElement();
// final List<Element> toRemove = new ArrayList<Element>();
// NodeList<Node> children = headElem.getChildNodes();
// for (int i = 0; i < children.getLength(); i++) {
// Node node = children.getItem(i);
// if (node.getNodeType() == Node.ELEMENT_NODE) {
// Element elem = Element.as(node);
// if (elem.getTagName().equalsIgnoreCase("link") && elem.getPropertyString("rel").equalsIgnoreCase("stylesheet")) {
// styleSheetsFound = true;
// String href = elem.getPropertyString("href");
// // If the correct style sheets are already loaded, then we should have
// // nothing to remove.
// if (!href.equals(styleSheetName) && href.indexOf("gxt") != -1) {
// toRemove.add(elem);
// }
// }
// }
// }
//
// // Return if we already have the correct style sheets
// if (styleSheetsFound && toRemove.size() != 0) {
// // Remove the old style sheets
// for (Element elem : toRemove) {
// headElem.removeChild(elem);
// }
// }
//	    
//	    
//		
// drawApp();
// StyleSheetLoader.loadStyleSheet(styleSheetName, "ext-el-mask", this);
// }
//
// private void drawApp() {
// Window.setTitle(constants.stockWatcher());
// RootPanel.get("appTitle").add(new Label(constants.stockWatcher()));
//		
// AutoHeightGridExample example = new AutoHeightGridExample();
// // ColumnGroupingExample example = new ColumnGroupingExample();
// // AggregationGridExample example = new AggregationGridExample();
// // GridPluginsExample example = new GridPluginsExample();
// // EditableGridExample example = new EditableGridExample();
// // RowEditorExample example = new RowEditorExample();
// // PagingGridExample example = new PagingGridExample();
// // GroupingGridExample example = new GroupingGridExample();
// // CheckGroupingGridExample example = new CheckGroupingGridExample();
// // TotalsGridExample example = new TotalsGridExample();
// // BufferedGridExample example = new BufferedGridExample();
// // ButtonsExample example = new ButtonsExample();
// // ButtonAlignExample example = new ButtonAlignExample();
// // GridStoreBindingExample example = new GridStoreBindingExample();
// // TemplateExample example = new TemplateExample();
// // BasicDNDExample example = new BasicDNDExample();
// // GridToGridExample example = new GridToGridExample();
// // ListViewDNDExample example = new ListViewDNDExample();
// // MultiComponentExample example = new MultiComponentExample();
// // ReorderingGridExample example = new ReorderingGridExample();
// // ReorderingTreeGridExample example = new ReorderingTreeGridExample();
//		
// ExamplesModel model = new ExamplesModel();
// for (int i = 0; i < model.getChildren().size(); i++) {
// Category cat = (Category) model.getChildren().get(i);
// for (int j = 0; j < cat.getChildren().size(); j++) {
// Entry entry = (Entry) cat.getChildren().get(j);
// //examples.put(entry.getId(), entry);
// }
// }
//
// Registry.register("model", model);
//	    
// // ReorderingTreePanelExample example = new ReorderingTreePanelExample();
// // TreeGridToTreeGridExample example = new TreeGridToTreeGridExample();
// // TreePanelToTreePanelExample example = new TreePanelToTreePanelExample();
// // AdvancedComboBoxExample example = new AdvancedComboBoxExample();
// // AdvancedFormsExample example = new AdvancedFormsExample();
// // BasicTabExample example = new BasicTabExample();
// // ComboBoxExample example = new ComboBoxExample();
// // CustomFormExample example = new CustomFormExample();
// // FileUploadExample example = new FileUploadExample();
// FormsExample example = new FormsExample();
//	    
// mainPanel.add(example);
//		
// // addStockButton = new Button(constants.add());
// //
// // final NumberFormat currency = NumberFormat.getCurrencyFormat();
// // final NumberFormat changeFormat = NumberFormat.getFormat("+#,##0.00;-#,##0.00");
// // final NumberCellRenderer<Grid<StockPrice>> numberRenderer = new NumberCellRenderer<Grid<StockPrice>>(currency);
// //
// // GridCellRenderer<StockPrice> change = new GridCellRenderer<StockPrice>() {
// // public String render(StockPrice model, String property, ColumnData config, int rowIndex, int colIndex,
// // ListStore<StockPrice> store, Grid<StockPrice> grid) {
// // double val = (Double) model.get(property);
// // String style = val < 0 ? "red" : "green";
// // return "<span style='color:" + style + "'>" + changeFormat.format(val) + "</span>";
// // }
// // };
// //
// // GridCellRenderer<StockPrice> priceNumber = new GridCellRenderer<StockPrice>() {
// // public String render(StockPrice model, String property, ColumnData config, int rowIndex, int colIndex, ListStore<StockPrice> store,
// // Grid<StockPrice> grid) {
// // return numberRenderer.render(null, property, model.get(property));
// // }
// // };
// //
// // List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
// //
// // ColumnConfig column = new ColumnConfig();
// // // symbol column
// // column.setId("symbol");
// // column.setHeader(constants.symbol());
// // column.setWidth(100);
// // configs.add(column);
// //
// // // price
// // column = new ColumnConfig();
// // column.setId("price");
// // column.setHeader(constants.price());
// // column.setWidth(100);
// // column.setRenderer(priceNumber);
// // configs.add(column);
// //
// // column = new ColumnConfig();
// // column.setId("change");
// // column.setHeader(constants.change());
// // column.setWidth(300);
// // column.setRenderer(change);
// // configs.add(column);
// //
// // ColumnModel cm = new ColumnModel(configs);
// //
// // grid = new Grid<StockPrice>(stocks, cm);
// // grid.setStyleAttribute("borderTop", "none");
// // grid.setAutoExpandColumn("symbol");
// // grid.setBorders(true);
// // grid.setStripeRows(true);
// //
// // ContentPanel gridCP = new ContentPanel();
// // gridCP.setBodyBorder(false);
// // gridCP.setCollapsible(true);
// // gridCP.setHeading(constants.stockWatcher());
// // gridCP.setButtonAlign(HorizontalAlignment.CENTER);
// // gridCP.setLayout(new FitLayout());
// // gridCP.setSize(600, 300);
// // gridCP.add(grid);
// //
// // // Assemble Add Stock panel.
// // addPanel.setLayout(new RowLayout(Orientation.HORIZONTAL));
// // addPanel.setHeaderVisible(false);
// // addPanel.setBorders(false);
// // addPanel.setSize(180, 26);
// // addPanel.setBodyBorder(false);
// // addPanel.add(newSymbolTextBox, new RowData(120, 22));
// // addPanel.add(addStockButton, new RowData(60, 22, new Margins(0, 4, 0, 4)));
// //
// // // Assemble Main panel.
// // mainPanel.add(gridCP);
// // mainPanel.add(addPanel);
// // mainPanel.add(lastUpdatedLabel);
// //
// //
// //
// // // Move cursor focus to the input box.
// // newSymbolTextBox.focus();
// //
// // // Setup timer to refresh list automatically.
// // Timer refreshTimer = new Timer() {
// // @Override
// // public void run() {
// // refreshWatchList();
// // }
// // };
// // refreshTimer.scheduleRepeating(REFRESH_INTERVAL);
// //
// // // Listen for mouse events on the Add button.
// // addStockButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
// //
// // @Override
// // public void componentSelected(ButtonEvent ce) {
// // addStock();
// // }
// // });
// //
// // // Listen for keyboard events in the input box.
// // newSymbolTextBox.addKeyListener(new KeyListener() {
// // public void componentKeyPress(ComponentEvent event) {
// // if (event.getKeyCode() == KeyCodes.KEY_ENTER) {
// // addStock();
// // }
// // }
// // });
//		
// }
//
// /**
// * Refreshes the stock data
// */
// protected void refreshWatchList() {
// final double MAX_PRICE = 100.0; // $100.00
// final double MAX_PRICE_CHANGE = 0.02; // +/- 2%
//
// StockPrice[] prices = new StockPrice[stocks.getCount()];
// for (int i = 0; i < stocks.getCount(); i++) {
// double price = Random.nextDouble() * MAX_PRICE;
// double change = price * MAX_PRICE_CHANGE * (Random.nextDouble() * 2.0 - 1.0);
//
// prices[i] = new StockPrice(stocks.getAt(i).getSymbol(), price, change);
// }
//
// updateTable(prices);
// }
//
// /**
// * Update the Price and Change fields all the rows in the stock table.
// *
// * @param prices
// * Stock data for all rows.
// */
// private void updateTable(StockPrice[] prices) {
// for (int i = 0; i < prices.length; i++) {
// updateTable(prices[i]);
// }
//
// // Display timestamp showing last refresh.
// lastUpdatedLabel.setText(messages.lastUpdate(new Date()));
// }
//
// /**
// * Update a single row in the stock table.
// *
// * @param price
// * Stock data for a single row.
// */
// private void updateTable(StockPrice price) {
// // Make sure the stock is still in the stock table.
// if (!stocks.contains(price)) {
// return;
// }
//		
// //int row = stocks.indexOf(price);
// stocks.remove(price);
// stocks.add(price);
// }
//
// /**
// * Add a stock to the stock list
// */
// protected void addStock() {
// final String symbol = newSymbolTextBox.getValue().toUpperCase().trim();
// newSymbolTextBox.focus();
//
// // Stock code must be between 1 and 10 chars that are numbers, letters, or dots.
// if (!symbol.matches("^[0-9A-Z\\.]{1,10}$")) {
// Window.alert(messages.invalidSymbol(symbol));
// newSymbolTextBox.selectAll();
// return;
// }
//
// final StockPrice newStock = new StockPrice(symbol, 0.00, 0.00);
// newSymbolTextBox.setValue("");
//
// // Don't add the stock if it's already in the table.
// if (stocks.contains(newStock))
// return;
// else
// stocks.add(newStock);
//		
// // Add a button to remove this stock from the table.
// Button removeStockButton = new Button("x");
// removeStockButton.addStyleDependentName("remove");
//
// removeStockButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
// @Override
// public void componentSelected(ButtonEvent ce) {
// stocks.remove(newStock);
// }
// });
//
// // Get the stock price.
// refreshWatchList();
// }
//
// @Override
// public void execute() {
// RootPanel.getBodyElement().getStyle().setProperty("display", "none");
// RootPanel.getBodyElement().getStyle().setProperty("display", "");
// // Associate the Main panel with the HTML host page.
// RootPanel.get("stockList").add(mainPanel);
// }
// }
