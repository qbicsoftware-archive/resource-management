package facs.model;

import java.io.Serializable;

public class InstituteBean implements Serializable{
  private static final long serialVersionUID = 4413126249488373976L;
  
  private String name = "";
  private String street = "";
  private String postalCode = "";
  private String city = "";
  private String shortName = "";
  
  public InstituteBean(String name, String street, String postalCode, String city, String shortName) {
    super();
    this.name = name;
    this.street = street;
    this.postalCode = postalCode;
    this.city = city;
    this.shortName = shortName;
  }
  
  public InstituteBean(){
    
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getStreet() {
    return street;
  }

  public void setStreet(String street) {
    this.street = street;
  }

  public String getPostalCode() {
    return postalCode;
  }

  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getShortName() {
    return shortName;
  }

  public void setShortName(String shortName) {
    this.shortName = shortName;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((city == null) ? 0 : city.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((postalCode == null) ? 0 : postalCode.hashCode());
    result = prime * result + ((shortName == null) ? 0 : shortName.hashCode());
    result = prime * result + ((street == null) ? 0 : street.hashCode());
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
    InstituteBean other = (InstituteBean) obj;
    if (city == null) {
      if (other.city != null)
        return false;
    } else if (!city.equals(other.city))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (postalCode == null) {
      if (other.postalCode != null)
        return false;
    } else if (!postalCode.equals(other.postalCode))
      return false;
    if (shortName == null) {
      if (other.shortName != null)
        return false;
    } else if (!shortName.equals(other.shortName))
      return false;
    if (street == null) {
      if (other.street != null)
        return false;
    } else if (!street.equals(other.street))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "InstituteBean [name=" + name + ", street=" + street + ", postalCode=" + postalCode
        + ", city=" + city + ", shortName=" + shortName + "]";
  }
  
}
