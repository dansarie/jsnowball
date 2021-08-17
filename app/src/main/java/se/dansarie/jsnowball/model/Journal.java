package se.dansarie.jsnowball.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class Journal extends SnowballStateMember {
  private String name = "";
  private String issn = "";

  public Journal(SnowballState state) {
    super(state);
  }

  public String getIssn() {
    return issn;
  }

  public String getName() {
    return name;
  }

  public void merge(Journal merged) {
    if (Objects.requireNonNull(merged) == this) {
      throw new IllegalArgumentException("Attempted to merge author with itself.");
    }
    if (merged.getState() != getState()) {
      throw new IllegalArgumentException("Attempted to merge authors belonging to different "
          + "states.");
    }
    for (Article art : new ArrayList<>(getState().getArticles())) {
      if (art.getJournal() == merged) {
        art.setJournal(this);
      }
    }
    getState().removeMember(merged);
  }

  public void setName(String name) {
    this.name = Objects.requireNonNullElse(name, "");
    fireUpdated();
  }

  public void setIssn(String issn) {
    this.issn = Objects.requireNonNullElse(issn, "");
    fireUpdated();
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
  public void remove() {
    for (Article art : new ArrayList<>(getState().getArticles())) {
      if (art.getJournal() == this) {
        art.setJournal(null);
      }
    }
    getState().removeMember(this);
  }

  @Override
  public String toString() {
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

  SerializationProxy getSerializationProxy() {
    return new SerializationProxy(this);
  }

  void restoreFromProxy(SerializationProxy proxy) {
    setIssn(proxy.issn);
    setName(proxy.name);
    setNotes(proxy.notes);
  }

  static class SerializationProxy implements Serializable {
    static final long serialVersionUID = 3612664749150805684L;
    private String issn;
    private String name;
    private String notes;

    private SerializationProxy(Journal jo) {
      issn = jo.issn;
      name = jo.name;
      notes = jo.getNotes();
    }
  }
}
