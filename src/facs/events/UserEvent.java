package facs.events;
import java.util.Date;

import com.vaadin.ui.components.calendar.event.BasicEvent;

public class UserEvent extends BasicEvent{
  private static final long serialVersionUID = -1597480745980224970L;
  
  private String userName;
  private String kostenstelle;
  private String device;
  private float costs;
  private boolean locked;
  private boolean isInDatabase;
  
  public void init(String userName, String kostenstelle, String device, float costs,
      boolean locked, boolean isInDatabase) {
    this.userName = userName;
    this.kostenstelle = kostenstelle;
    this.device = device;
    this.costs = costs;
    this.locked = locked;
    this.isInDatabase = isInDatabase;
  }
  
  public UserEvent(String caption, String description, Date startDate, Date endDate) {
    super(caption, description, startDate, endDate);
    // TODO Auto-generated constructor stub
  }  
  
  public String getUserName() {
    return userName;
  }
  public void setUserName(String userName) {
    this.userName = userName;
  }
  public String getKostenstelle() {
    return kostenstelle;
  }
  public void setKostenstelle(String kostenstelle) {
    this.kostenstelle = kostenstelle;
  }
  public String getDevice() {
    return device;
  }
  public void setDevice(String device) {
    this.device = device;
  }
  public float getCosts() {
    return costs;
  }
  public void setCosts(float costs) {
    this.costs = costs;
  }
  public boolean isLocked() {
    return locked;
  }
  public void setLocked(boolean locked) {
    this.locked = locked;
  }
  public boolean isInDatabase() {
    return isInDatabase;
  }
  public void setInDatabase(boolean isInDatabase) {
    this.isInDatabase = isInDatabase;
  }

}
