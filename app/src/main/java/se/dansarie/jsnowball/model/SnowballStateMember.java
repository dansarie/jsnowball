package se.dansarie.jsnowball.model;

import java.util.Objects;

abstract class SnowballStateMember implements Comparable<SnowballStateMember> {
  private SnowballState state = null;
  private String notes = "";

  SnowballStateMember(SnowballState state) {
    this.state = Objects.requireNonNull(state);
    state.addMember(this);
  }

  protected synchronized void fireUpdated() {
    state.fireUpdated(this);
  }

  public synchronized SnowballState getState() {
   return state;
  }

  public synchronized String getNotes() {
    return notes;
  }

  public synchronized void setNotes(String notes) {
    this.notes = Objects.requireNonNullElse(notes, "");
  }

  public abstract void remove();
}
