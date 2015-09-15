package facs.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserBean implements Serializable{
  private static final long serialVersionUID = 7355810127461052570L;
  
  private int id = -1;
  private String name = "";
  private String workinggroup = "";
  private String institute = "";
  private String role = "";
  private String email = "";
  private String phone = "";
  private String status = "";
  private List<String> Kostenstelle = new ArrayList<String>();
  
  public UserBean(int id, String name, String role, String status, List<String> kostenstelle) {
    super();
    this.id = id;
    this.name = name;
    this.role = role;
    this.status = status;
    Kostenstelle = kostenstelle;
  }

  public UserBean() {
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

  public String getWorkinggroup() {
    return workinggroup;
  }

  public void setWorkinggroup(String workinggroup) {
    this.workinggroup = workinggroup;
  }

  public String getInstitute() {
    return institute;
  }

  public void setInstitute(String institute) {
    this.institute = institute;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((Kostenstelle == null) ? 0 : Kostenstelle.hashCode());
    result = prime * result + ((email == null) ? 0 : email.hashCode());
    result = prime * result + id;
    result = prime * result + ((institute == null) ? 0 : institute.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((role == null) ? 0 : role.hashCode());
    result = prime * result + ((status == null) ? 0 : status.hashCode());
    result = prime * result + ((phone == null) ? 0 : phone.hashCode());
    result = prime * result + ((workinggroup == null) ? 0 : workinggroup.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    UserBean other = (UserBean) obj;
    if (Kostenstelle == null) {
      if (other.Kostenstelle != null)
        return false;
    } else if (!Kostenstelle.equals(other.Kostenstelle))
      return false;
    if (email == null) {
      if (other.email != null)
        return false;
    } else if (!email.equals(other.email))
      return false;
    if (id != other.id)
      return false;
    if (institute == null) {
      if (other.institute != null)
        return false;
    } else if (!institute.equals(other.institute))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (role == null) {
      if (other.role != null)
        return false;
    } else if (!role.equals(other.role))
      return false;
    if (status == null) {
      if (other.status != null)
        return false;
    } else if (!status.equals(other.status))
      return false;
    if (phone == null) {
      if (other.phone != null)
        return false;
    } else if (!phone.equals(other.phone))
      return false;
    if (workinggroup == null) {
      if (other.workinggroup != null)
        return false;
    } else if (!workinggroup.equals(other.workinggroup))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "UserBean [id=" + id + ", name=" + name + ", workinggroup=" + workinggroup
        + ", institute=" + institute + ", role=" + role + ", email=" + email + ", telephon="
        + phone + ", status=" + status + ", Kostenstelle=" + Kostenstelle + "]";
  }
}
