/*******************************************************************************
 * QBiC Calendar provides an infrastructure for defining calendars for specific purposes like booking devices or
 * planning resources for services and integration of relevant data into the common portal infrastructure.
 * Copyright (C) 2016 Aydın Can Polatkan
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
package facs.model;

import java.io.Serializable;

public class DeviceBean implements Serializable {
  private static final long serialVersionUID = -6717244163406823687L;

  private int id;
  private String name;
  private String description;
  private boolean restriction; 
  
  public DeviceBean(int id, String name, String description, boolean restriction) {
    super();
    this.id = id;
    this.name = name;
    this.setDescription(description);
    this.setRestriction(restriction);
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean getRestriction() {
    return restriction;
  }

  public void setRestriction(boolean restriction) {
    this.restriction = restriction;
  }
  
}
