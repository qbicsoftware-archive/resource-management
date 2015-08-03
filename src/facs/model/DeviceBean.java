package facs.model;

import java.io.Serializable;
import java.util.List;

public class DeviceBean implements Serializable {
  private static final long serialVersionUID = -6717244163406823687L;

  private int id;
  private String name;
  //cost of a machine per hour
  private float cost;
  
  private String allowedRole; 
  
  public DeviceBean(int id, String name, float cost, String allowedRole) {
    super();
    this.id = id;
    this.name = name;
    this.cost = cost;
    this.allowedRole = allowedRole;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public float getCost() {
    return cost;
  }

  public void setCost(float cost) {
    this.cost = cost;
  }

  public String getAllowedRole() {
    return allowedRole;
  }

  public void setAllowedRole(String allowedRole) {
    this.allowedRole = allowedRole;
  }


}
