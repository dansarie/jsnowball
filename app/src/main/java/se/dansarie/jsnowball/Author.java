package se.dansarie.jsnowball;

public class Author extends SnowballStateMember {
  private String firstname;
  private String lastname;
  private String orgname;
  private String orcid;

  Author() {
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
    state.updated(this);
  }

  public void setLastName(String name) {
    lastname = name;
    state.updated(this);
  }

  public void setOrgName(String name) {
    orgname = name;
    state.updated(this);
  }

  public void setOrcId(String id) {
    orcid = id;
    state.updated(this);
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
    if (o.getLastName() == null) {
      return 1;
    }
    int ret = getLastName().compareToIgnoreCase(o.getLastName());
    if (ret != 0) {
      return ret;
    }
    if (getFirstName() == null) {
      if (o.getFirstName() == null) {
        return 0;
      }
      return -1;
    }
    if (o.getFirstName() == null) {
      return 1;
    }
    return getFirstName().compareToIgnoreCase(o.getFirstName());
  }

  @Override
  public String toString() {
    return getLastName() + ", " + getFirstName();
  }
}
