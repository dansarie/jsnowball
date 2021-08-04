package se.dansarie.jsnowball;

public class Author {
  private String firstname;
  private String lastname;
  private String orgname;
  private String orcid;

  public Author(String first, String last) {
    firstname = first;
    lastname = last;
  }

  public String getFirstName() {
    return firstname;
  }

  public String getLastName() {
    return lastname;
  }

  public String getOrgName() {
    return orgname;
  }

  public String getOrcId() {
    return orcid;
  }

  public void setFirstName(String name) {
    firstname = name;
  }

  public void setLastName(String name) {
    lastname = name;
  }

  public void setOrgName(String name) {
    orgname = name;
  }

  public void setOrcId(String id) {
    orcid = id;
  }
}
