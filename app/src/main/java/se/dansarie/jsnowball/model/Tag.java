package se.dansarie.jsnowball.model;

import java.util.List;
import java.util.Objects;

import org.json.JSONObject;

public class Tag extends SnowballStateMember {
  private String name = "";
  private int color = 0;

  public Tag(SnowballState state) {
    super(state);
  }

  public int getColor() {
    lock();
    try {
      return color;
    } finally {
      unlock();
    }
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

  public void moveDown() {
    lock();
    try {
      getState().moveDown(this);
    } finally {
      unlock();
    }
  }

  public void moveUp() {
    lock();
    try {
      getState().moveUp(this);
    } finally {
      unlock();
    }
  }

  public void setColor(int color) {
    lock();
    try {
      if (color < 0 || color >= (1 << 24)) {
        throw new IllegalArgumentException();
      }
      this.color = color;
      fireUpdated();
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
    if (getState() != other.getState()) {
      throw new IllegalArgumentException("Attempted to compare tags belonging to different "
          + "states.");
    }
    lock();
    try {
      List<Tag> tags = getState().getTags();
      return tags.indexOf(this) - tags.indexOf(other);
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
      setColor(proxy.color);
      setName(proxy.name);
      setNotes(proxy.notes);
    } finally {
      unlock();
    }
  }

  static class SerializationProxy {
    private final String name;
    private final String notes;
    private final int color;

    SerializationProxy(JSONObject json) {
      name = json.getString("name");
      notes = json.getString("notes");
      color = json.getInt("color");
    }

    private SerializationProxy(Tag ta) {
      color = ta.color;
      name = ta.name;
      notes = ta.getNotes();
    }

    JSONObject toJson() {
      JSONObject json = new JSONObject();
      json.put("name", name);
      json.put("notes", notes);
      json.put("color", color);
      return json;
    }
  }
}
