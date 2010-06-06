/*
 * Ext GWT - Ext for GWT
 * Copyright(c) 2007-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */
package com.extjs.gxt.samples.client.examples.forms;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.extjs.gxt.samples.resources.client.TestData;
import com.extjs.gxt.samples.resources.client.model.Stock;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Slider;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.CheckBoxGroup;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.SliderField;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.TimeField;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.user.client.Element;

public class FormsExample extends LayoutContainer {

  private VerticalPanel vp;

  private FormData formData;

  @Override
  protected void onRender(Element parent, int index) {
    super.onRender(parent, index);
    formData = new FormData("-20");
    vp = new VerticalPanel();
    vp.setSpacing(10);
    createForm1();
    createForm2();
    add(vp);
  }

  private void createForm1() {
    FormPanel simple = new FormPanel();
    simple.setHeading("Simple Form");
    simple.setFrame(true);
    simple.setWidth(350);

    LayoutContainer layout = new LayoutContainer();
	FormLayout formLayout = new FormLayout(LabelAlign.LEFT);
	layout.setLayout(formLayout);
	
    TextField<String> firstName = new TextField<String>();
    firstName.setFieldLabel("Name");
    firstName.setAllowBlank(false);
    layout.add(firstName, formData);

    TextField<String> email = new TextField<String>();
    email.setFieldLabel("Email");
    email.setAllowBlank(false);
    layout.add(email, formData);

    List<Stock> stocks = TestData.getStocks();
    Collections.sort(stocks, new Comparator<Stock>() {
      public int compare(Stock arg0, Stock arg1) {
        return arg0.getName().compareTo(arg1.getName());
      }
    });

    ListStore<Stock> store = new ListStore<Stock>();
    store.add(stocks);

    ComboBox<Stock> combo = new ComboBox<Stock>();
    combo.setFieldLabel("Company");
    combo.setDisplayField("name");
    combo.setTriggerAction(TriggerAction.ALL);
    combo.setStore(store);
    layout.add(combo, formData);

    DateField date = new DateField();
    date.setFieldLabel("Birthday");
    layout.add(date, formData);

    TimeField time = new TimeField();
    time.setFieldLabel("Time");
    layout.add(time, formData);

    Slider slider = new Slider();
    slider.setMinValue(40);
    slider.setMaxValue(90);
    slider.setValue(60);
    slider.setIncrement(1);
    slider.setMessage("{0} inches tall");

    final SliderField sf = new SliderField(slider);
    sf.setFieldLabel("Size");
    layout.add(sf, formData);
    CheckBox check1 = new CheckBox();
    check1.setBoxLabel("קלאסי");

    CheckBox check2 = new CheckBox();
    check2.setBoxLabel("רוק");
    check2.setValue(true);

    CheckBox check3 = new CheckBox();
    check3.setBoxLabel("בלוז");

    CheckBoxGroup checkGroup = new CheckBoxGroup();
    checkGroup.setFieldLabel("Music");
    checkGroup.add(check1);
    checkGroup.add(check2);
    checkGroup.add(check3);
    layout.add(checkGroup, formData);

    Radio radio = new Radio();
    radio.setBoxLabel("אדום");
    radio.setValue(true);

    Radio radio2 = new Radio();
    radio2.setBoxLabel("כחול");

    RadioGroup radioGroup = new RadioGroup();
    radioGroup.setFieldLabel("Favorite Color");
    radioGroup.add(radio);
    radioGroup.add(radio2);
    layout.add(radioGroup, formData);

    TextArea description = new TextArea();
    description.setPreventScrollbars(true);
    description.setFieldLabel("Description");
    layout.add(description, formData);

    Radio radio3 = new Radio();
    radio3.setBoxLabel("Apple");
    radio3.setValue(true);

    Radio radio4 = new Radio();
    radio4.setBoxLabel("Banana");

    RadioGroup radioGroup2 = new RadioGroup();
    radioGroup2.setFieldLabel("Favorite Fruit");
    radioGroup2.add(radio3);
    radioGroup2.add(radio4);
    layout.add(radioGroup2, formData);
    
    simple.add(layout);
    
    Button b = new Button("Submit");
    simple.addButton(b);
    simple.addButton(new Button("Cancel"));

    simple.setButtonAlign(HorizontalAlignment.CENTER);

    FormButtonBinding binding = new FormButtonBinding(simple);
    binding.addButton(b);

    vp.add(simple);
  }

  private void createForm2() {
    FormPanel form2 = new FormPanel();
    form2.setFrame(true);
    form2.setHeading("Simple Form with FieldSets");
    form2.setWidth(350);
    form2.setLayout(new FlowLayout());

    FieldSet fieldSet = new FieldSet();
    fieldSet.setHeading("מידע אישי");
    fieldSet.setCheckboxToggle(true);

    FormLayout layout = new FormLayout();
    layout.setLabelWidth(75);
    fieldSet.setLayout(layout);

    TextField<String> firstName = new TextField<String>();
    firstName.setFieldLabel("First Name");
    firstName.setAllowBlank(false);
    fieldSet.add(firstName, formData);

    TextField<String> lastName = new TextField<String>();
    lastName.setFieldLabel("Last Name");
    fieldSet.add(lastName, formData);

    TextField<String> company = new TextField<String>();
    company.setFieldLabel("Company");
    fieldSet.add(company, formData);

    TextField<String> email = new TextField<String>();
    email.setFieldLabel("Email");
    fieldSet.add(email, formData);

    form2.add(fieldSet);

    fieldSet = new FieldSet();
    fieldSet.setHeading("טלפונים");
    fieldSet.setCollapsible(true);

    layout = new FormLayout();
    layout.setLabelWidth(75);
    fieldSet.setLayout(layout);

    TextField<String> field = new TextField<String>();
    field.setFieldLabel("Home");
    fieldSet.add(field, formData);

    field = new TextField<String>();
    field.setFieldLabel("Business");
    fieldSet.add(field, formData);

    field = new TextField<String>();
    field.setFieldLabel("Mobile");
    fieldSet.add(field, formData);

    field = new TextField<String>();
    field.setFieldLabel("Fax");
    fieldSet.add(field, formData);

    form2.add(fieldSet);
    form2.setButtonAlign(HorizontalAlignment.CENTER);
    form2.addButton(new Button("Save"));
    form2.addButton(new Button("Cancel"));

    vp.add(form2);
  }

}
