package se.dansarie.jsnowball.model;

import java.util.List;
import java.util.Objects;

import org.json.JSONObject;

public class Tag extends SnowballStateMember {
  private String name = "";

  public Tag(SnowballState state) {
    super(state);
  }

  public String getName() {
    lock();
    try {
      return name;
    } finally {
      unlock();
    }
  }

  public void merge(Tag merged) {
    lock();
    try {
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
    } finally {
      unlock();
    }
  }

  public void setName(String name) {
    lock();
    try {
      this.name = Objects.requireNonNullElse(name, "");
      fireUpdated();
    } finally {
      unlock();
    }
  }

  @Override
  public int compareTo(SnowballStateMember other) {
    lock();
    try {
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
    } finally {
      unlock();
    }
  }

  @Override
  public void remove() {
    lock();
    try {
      for (Article art : getState().getArticles()) {
        if (art.getTags().contains(this)) {
          art.removeTag(this);
        }
      }
      getState().removeMember(this);
    } finally {
      unlock();
    }
  }

  @Override
  public String toString() {
    lock();
    try {
      return name;
    } finally {
      unlock();
    }
  }

  SerializationProxy getSerializationProxy() {
    lock();
    try {
      return new SerializationProxy(this);
    } finally {
      unlock();
    }
  }

  void restoreFromProxy(SerializationProxy proxy) {
    lock();
    try {
      setName(proxy.name);
      setNotes(proxy.notes);
    } finally {
      unlock();
    }
  }

  static class SerializationProxy {
    private final String name;
    private final String notes;

    SerializationProxy(JSONObject json) {
      name = json.getString("name");
      notes = json.getString("notes");
    }

    private SerializationProxy(Tag ta) {
      name = ta.name;
      notes = ta.getNotes();
    }

    JSONObject toJson() {
      JSONObject json = new JSONObject();
      json.put("name", name);
      json.put("notes", notes);
      return json;
    }
  }
}
