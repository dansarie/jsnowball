package se.dansarie.jsnowball.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Tag extends SnowballStateMember {
  private String name = "";

  public Tag(SnowballState state) {
    super(state);
  }

  public synchronized String getName() {
    return name;
  }

  public synchronized void merge(Tag merged) {
    if (Objects.requireNonNull(merged) == this) {
      throw new IllegalArgumentException("Attempted to merge tag with itself.");
    }
    if (merged.getState() != getState()) {
      throw new IllegalArgumentException("Attempted to merge tags belonging to different "
          + "states.");
    }
    for (Article art : new ArrayList<>(getState().getArticles())) {
      List<Tag> tags = art.getTags();
      if (tags.contains(merged)) {
        art.removeTag(merged);
        if (tags.contains(this)) {
          art.addTag(this);
        }
      }
    }
    getState().removeMember(merged);
  }

  public synchronized void setName(String name) {
    this.name = Objects.requireNonNullElse(name, "");
    fireUpdated();
  }

  @Override
  public synchronized int compareTo(SnowballStateMember other) {
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
  public synchronized void remove() {
    for (Article art : new ArrayList<>(getState().getArticles())) {
      if (art.getTags().contains(this)) {
        art.removeTag(this);
      }
    }
    getState().removeMember(this);
  }

  @Override
  public synchronized String toString() {
    return name;
  }

  synchronized SerializationProxy getSerializationProxy() {
    return new SerializationProxy(this);
  }

  synchronized void restoreFromProxy(SerializationProxy proxy) {
    setName(proxy.name);
    setNotes(proxy.notes);
  }

  static class SerializationProxy implements Serializable {
    static final long serialVersionUID = 4350613725999012654L;
    private String name;
    private String notes;

    private SerializationProxy(Tag ta) {
      name = ta.name;
      notes = ta.getNotes();
    }
  }
}
