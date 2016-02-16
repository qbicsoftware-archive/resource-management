/*******************************************************************************
 * QBiC Calendar provides an infrastructure for defining calendars for specific purposes like
 * booking devices or planning resources for services and integration of relevant data into the
 * common portal infrastructure. Copyright (C) 2016 Aydın Can Polatkan & David Wojnar
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

public class UserBean implements Serializable {
  private static final long serialVersionUID = 7355810127461052570L;

  private int id = -1;
  private String user_ldap = "";
  private String name = "";
  private String group = "";
  private String workgroup = "";
  private String institute = "";
  private String role = "";
  private String email = "";
  private String phone = "";
  private String status = "";
  private String kostenstelle = "";
  private String project = "";

  // private List<String> Kostenstelle = new ArrayList<String>();
  // private List<String> ProjectName = new ArrayList<String>();

  public UserBean(int id, String user_ldap, String name, String group, String role, String status,
      String kostenstelle, String project) {
    super();
    this.id = id;
    this.user_ldap = user_ldap;
    this.group = group;
    this.name = name;
    this.role = role;
    this.status = status;
    this.kostenstelle = kostenstelle;
    this.project = project;
    // Kostenstelle = kostenstelle;
    // ProjectName = projectName;
  }

  public UserBean(String user_ldap, int id, String name, String group, String workgroup,
      String institute, String kostenstelle, String project, String email, String phone) {
    super();
    this.user_ldap = user_ldap;
    this.id = id;
    this.name = name;
    this.group = group;
    this.workgroup = workgroup;
    this.institute = institute;
    this.kostenstelle = kostenstelle;
    this.project = project;
    this.email = email;
    this.phone = phone;
    this.kostenstelle = kostenstelle;
    this.project = project;
  }

  public UserBean() {}

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setLDAP(String user_ldap) {
    this.user_ldap = user_ldap;
  }

  public String getLDAP() {
    return user_ldap;
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

  public String getKostenstelle() {
    return kostenstelle;
  }

  public void setGroupID(String group) {
    this.group = group;
  }

  public String getGroupID() {
    return group;
  }

  public void setKostenstelle(String kostenstelle) {
    this.kostenstelle = kostenstelle;
  }

  public String getProject() {
    return project;
  }

  public void setProject(String project) {
    this.project = project;
  }

  /*
   * public List<String> getKostenstelle() { return Kostenstelle; }
   * 
   * public void setKostenstelle(List<String> kostenstelle) { Kostenstelle = kostenstelle; }
   * 
   * 
   * public List<String> getProject() { return ProjectName; }
   * 
   * public void setProject(List<String> projectName) { ProjectName = projectName; }
   */

  public String getWorkgroup() {
    return workgroup;
  }

  public void setWorkgroup(String workgroup) {
    this.workgroup = workgroup;
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
    result = prime * result + ((kostenstelle == null) ? 0 : kostenstelle.hashCode());
    result = prime * result + ((email == null) ? 0 : email.hashCode());
    result = prime * result + id;
    result = prime * result + ((institute == null) ? 0 : institute.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((role == null) ? 0 : role.hashCode());
    result = prime * result + ((status == null) ? 0 : status.hashCode());
    result = prime * result + ((phone == null) ? 0 : phone.hashCode());
    result = prime * result + ((group == null) ? 0 : group.hashCode());
    result = prime * result + ((workgroup == null) ? 0 : workgroup.hashCode());
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
    if (kostenstelle == null) {
      if (other.kostenstelle != null)
        return false;
    } else if (!kostenstelle.equals(other.kostenstelle))
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
    if (workgroup == null) {
      if (other.workgroup != null)
        return false;
    } else if (!workgroup.equals(other.workgroup))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "UserBean [id=" + id + ", name=" + name + ", workgroup=" + workgroup + ", institute="
        + institute + ", role=" + role + ", email=" + email + ", telephon=" + phone + ", status="
        + status + ", kostenstelle=" + kostenstelle + "]";
  }
}
