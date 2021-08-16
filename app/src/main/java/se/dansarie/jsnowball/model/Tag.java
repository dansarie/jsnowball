package se.dansarie.jsnowball.model;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class Tag extends SnowballStateMember {

  private String name = null;

  public Tag(SnowballState state) {
    super(state);
  }

  private Tag(SerializationProxy sp) {
    super(null);
    name = sp.name;
    setNotes(sp.notes);
  }

  public String getName() {
    return name;
  }

  public void merge(Tag merged) {
    if (Objects.requireNonNull(merged) == this) {
      throw new IllegalArgumentException("Attempted to merge tag with itself.");
    }
    if (merged.getState() != getState()) {
      throw new IllegalArgumentException("Attempted to merge tags belonging to different "
          + "states.");
    }
    for (Article art : getState().getArticles()) {
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

  public void setName(String name) {
    this.name = name;
    fireUpdated();
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

  SerializationProxy getSerializationProxy() {
    return new SerializationProxy(this);
  }

  void restoreFromProxy(SerializationProxy proxy) {
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
