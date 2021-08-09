package se.dansarie.jsnowball;

public class Author extends SnowballStateMember {
  private String firstname;
  private String lastname;
  private String orgname;
  private String orcid;

  public Author(SnowballState state) {
    super(state);
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

  @Override
  public int compareTo(SnowballStateMember other) {
    Author o = (Author)other;
    if (getLastName() == null) {
      if (o.getLastName() == null) {
        return 0;
      }
      return -1;
    }
    int ret = getLastName().compareTo(o.getLastName());
    if (ret != 0) {
      return ret;
    }
    if (getFirstName() == null) {
      if (o.getFirstName() == null) {
        return 0;
      }
      return -1;
    }
    return getFirstName().compareTo(o.getFirstName());
  }
}
