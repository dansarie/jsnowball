package se.dansarie.jsnowball.model;

import java.util.Objects;

import org.json.JSONObject;

public class Journal extends SnowballStateMember {
  private String name = "";
  private String issn = "";

  public Journal(SnowballState state) {
    super(state);
  }

  public synchronized String getIssn() {
    return issn;
  }

  public String getName() {
    return name;
  }

  public synchronized void merge(Journal merged) {
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
  }

  public synchronized void setName(String name) {
    this.name = Objects.requireNonNullElse(name, "");
    fireUpdated();
  }

  public synchronized void setIssn(String issn) {
    this.issn = Objects.requireNonNullElse(issn, "");
    fireUpdated();
  }

  @Override
  public synchronized int compareTo(SnowballStateMember other) {
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
  public synchronized void remove() {
    for (Article art : getState().getArticles()) {
      if (art.getJournal() == this) {
        art.setJournal(null);
      }
    }
    getState().removeMember(this);
  }

  @Override
  public synchronized String toString() {
    return getName();
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

  synchronized SerializationProxy getSerializationProxy() {
    return new SerializationProxy(this);
  }

  synchronized void restoreFromProxy(SerializationProxy proxy) {
    setIssn(proxy.issn);
    setName(proxy.name);
    setNotes(proxy.notes);
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
