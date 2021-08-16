package se.dansarie.jsnowball.model;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class Tag extends SnowballStateMember implements Serializable {

  private String name = null;

  Tag() {
  }

  private Tag(SerializationProxy sp) {
    name = sp.name;
    setNotes(sp.notes);
  }

  public void setName(String name) {
    this.name = name;
    state.updated(this);
  }

  public String getName() {
    return name;
  }

  @Override
  public int compareTo(SnowballStateMember other) {
    Tag o = (Tag)other;
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
    return name;
  }

  private Object writeReplace() {
    return new SerializationProxy(this);
  }

  private void readObject(ObjectInputStream ois) throws InvalidObjectException {
    throw new InvalidObjectException("Use of serialization proxy required.");
  }

  private static class SerializationProxy implements Serializable {
    static final long serialVersionUID = 4350613725999012654L;
    private String name;
    private String notes;

    private SerializationProxy(Tag ta) {
      name = ta.name;
      notes = ta.getNotes();
    }

    private Object readResolve() {
      return new Tag(this);
    }
  }
}
