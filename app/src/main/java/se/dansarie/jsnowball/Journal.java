package se.dansarie.jsnowball;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class Journal extends SnowballStateMember implements Serializable {
  private String name = null;
  private String issn = null;

  Journal() {
  }

  private Journal(SerializationProxy sp) {
    name = sp.name;
    issn = sp.issn;
  }

  public String getIssn() {
    return issn;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
    state.updated(this);
  }

  public void setIssn(String issn) {
    this.issn = issn;
    state.updated(this);
  }

  @Override
  public int compareTo(SnowballStateMember other) {
    Journal o = (Journal)other;
    if (getName() == null) {
      if (o.getName() == null) {
        return 0;
      }
      return -1;
    }
    if (o.getName() == null) {
      return 1;
    }
    return getName().compareToIgnoreCase(o.getName());
  }

  @Override
  public String toString() {
    return getName();
  }

  private Object writeReplace() {
    return new SerializationProxy(this);
  }

  private void readObject(ObjectInputStream ois) throws InvalidObjectException {
    throw new InvalidObjectException("Use of serialization proxy required.");
  }

  private static class SerializationProxy implements Serializable {
    static final long serialVersionUID = 3612664749150805684L;
    private String name;
    private String issn;

    private SerializationProxy(Journal jo) {
      name = jo.name;
      issn = jo.issn;
    }

    private Object readResolve() {
      return new Journal(this);
    }
  }
}
