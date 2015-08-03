package facs.components;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class Settings extends CustomComponent{
  private static final long serialVersionUID = 2183973381935176872L;
  
  public Settings(){
    this.setCaption("Settings");
    TabSheet settings = new TabSheet();
    settings.addStyleName(ValoTheme.TABSHEET_FRAMED);
    
    VerticalLayout devices = new VerticalLayout();
    
    devices.addComponent(deviceGrid());
    
  }
  
}
