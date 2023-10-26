/* Copyright (c) 2021-2023 Marcus Dansarie

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
   GNU General Public License for more details.
   You should have received a copy of the GNU General Public License
   along with this program. If not, see <http://www.gnu.org/licenses/>. */

package se.dansarie.jsnowball.model;

import java.util.List;
import java.util.Objects;

import org.json.JSONException;
import org.json.JSONObject;

public class Tag extends SnowballStateMember {
  private String name = "";
  private int color = 0;
  private TagShape shape = TagShape.CIRCLE;

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

  public TagShape getShape() {
    lock();
    try {
      return shape;
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

  public void setShape(TagShape shape) {
    lock();
    try {
      this.shape = Objects.requireNonNull(shape);
      fireUpdated();
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
      getState().pushInhibitUpdates();
      setColor(proxy.color);
      setName(proxy.name);
      setNotes(proxy.notes);
      setShape(proxy.shape);
      getState().popInhibitUpdates();
    } finally {
      unlock();
    }
  }

  static class SerializationProxy {
    private final String name;
    private final String notes;
    private final int color;
    private final TagShape shape;

    SerializationProxy(JSONObject json) {
      name = json.getString("name");
      notes = json.getString("notes");
      color = json.getInt("color");
      TagShape sh = TagShape.CIRCLE;
      try {
        sh = TagShape.valueOf(json.getString("shape"));
      } catch (JSONException ex) {
        /* Ignore. */
      }
      shape = sh;
    }

    private SerializationProxy(Tag ta) {
      color = ta.color;
      name = ta.name;
      notes = ta.getNotes();
      shape = ta.shape;
    }

    JSONObject toJson() {
      JSONObject json = new JSONObject();
      json.put("name", name);
      json.put("notes", notes);
      json.put("color", color);
      json.put("shape", shape.toString());
      return json;
    }
  }

  public static enum TagShape {
    CIRCLE, SQUARE, RHOMBUS
  }
}
