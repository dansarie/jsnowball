package se.dansarie.jsnowball;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class Author extends SnowballStateMember implements Serializable {
  private String firstname;
  private String lastname;
  private String orgname;
  private String orcid;

  Author() {
  }

  private Author(SerializationProxy sp) {
    firstname = sp.firstname;
    lastname = sp.lastname;
    orgname = sp.orgname;
    orcid = sp.orcid;
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

  private Object writeReplace() {
    return new SerializationProxy(this);
  }

  private void readObject(ObjectInputStream ois) throws InvalidObjectException {
    throw new InvalidObjectException("Use of serialization proxy required.");
  }

  private static class SerializationProxy implements Serializable {
    static final long serialVersionUID = 5154001552990566490L;
    private String firstname;
    private String lastname;
    private String orgname;
    private String orcid;

    private SerializationProxy(Author au) {
      firstname = au.firstname;
      lastname = au.lastname;
      orgname = au.orgname;
      orcid = au.orgname;
    }

    private Object readResolve() {
      return new Author(this);
    }
  }
}
