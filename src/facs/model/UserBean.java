package facs.model;

import java.io.Serializable;
import java.util.List;

public class UserBean implements Serializable{
  private static final long serialVersionUID = 7355810127461052570L;
  
  private int id;
  private String name;
  private String role;
  private String status;
  private List<String> Kostenstelle;
  
  public UserBean(int id, String name, String role, String status, List<String> kostenstelle) {
    super();
    this.id = id;
    this.name = name;
    this.role = role;
    this.status = status;
    Kostenstelle = kostenstelle;
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

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public List<String> getKostenstelle() {
    return Kostenstelle;
  }

  public void setKostenstelle(List<String> kostenstelle) {
    Kostenstelle = kostenstelle;
  }
}
