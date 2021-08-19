package se.dansarie.jsnowball.model;

import java.util.Objects;

import org.json.JSONObject;

public class Journal extends SnowballStateMember {
  private String name = "";
  private String issn = "";

  public Journal(SnowballState state) {
    super(state);
  }

  public String getIssn() {
    lock();
    try {
      return issn;
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

  public void merge(Journal merged) {
    lock();
    try {
      if (Objects.requireNonNull(merged) == this) {
        throw new IllegalArgumentException("Attempted to merge author with itself.");
      }
      if (merged.getState() != getState()) {
        throw new IllegalArgumentException("Attempted to merge authors belonging to different "
            + "states.");
      }
      for (Article art : getState().getArticles()) {
        if (art.getJournal() == merged) {
          art.setJournal(this);
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

  public void setIssn(String issn) {
    lock();
    try {
      this.issn = Objects.requireNonNullElse(issn, "");
      fireUpdated();
    } finally {
      unlock();
    }
  }

  @Override
  public int compareTo(SnowballStateMember other) {
    lock();
    try {
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
    } finally {
      unlock();
    }
  }

  @Override
  public void remove() {
    lock();
    try {
      for (Article art : getState().getArticles()) {
        if (art.getJournal() == this) {
          art.setJournal(null);
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
      return getName();
    } finally {
      unlock();
    }
  }

  public static Journal getByIssn(SnowballState state, String issn) {
    for (Journal jo : state.getJournals()) {
      if (jo.getIssn().equals(issn)) {
        return jo;
      }
    }
    return null;
  }

  public static Journal getByName(SnowballState state, String name) {
    for (Journal jo : state.getJournals()) {
      if (jo.getName().equals(name)) {
        return jo;
      }
    }
    return null;
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
      setIssn(proxy.issn);
      setName(proxy.name);
      setNotes(proxy.notes);
    } finally {
      unlock();
    }
  }

  static class SerializationProxy {
    private final String issn;
    private final String name;
    private final String notes;

    SerializationProxy(JSONObject json) {
      issn = json.getString("issn");
      name = json.getString("name");
      notes = json.getString("notes");
    }

    private SerializationProxy(Journal jo) {
      issn = jo.issn;
      name = jo.name;
      notes = jo.getNotes();
    }

    JSONObject toJson() {
      JSONObject json = new JSONObject();
      json.put("issn", issn);
      json.put("name", name);
      json.put("notes", notes);
      return json;
    }
  }
}
