/*******************************************************************************
 * QBiC Calendar provides an infrastructure for defining calendars for specific purposes like
 * booking devices or planning resources for services and integration of relevant data into the
 * common portal infrastructure. Copyright (C) 2017 Aydın Can Polatkan & David Wojnar
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see http://www.gnu.org/licenses/.
 *******************************************************************************/
package facs.events;

import java.util.Date;

import com.vaadin.ui.components.calendar.event.BasicEvent;

public class UserEvent extends BasicEvent {
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
