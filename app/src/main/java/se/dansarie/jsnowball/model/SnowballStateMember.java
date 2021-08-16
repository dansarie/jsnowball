package se.dansarie.jsnowball.model;

import java.util.Objects;

abstract class SnowballStateMember implements Comparable<SnowballStateMember> {
  private SnowballState state = null;
  private String notes = "";

  SnowballStateMember(SnowballState state) {
    this.state = Objects.requireNonNull(state);
    state.addMember(this);
  }

  protected void fireUpdated() {
    state.fireUpdated(this);
  }

  public SnowballState getState() {
   return state;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = Objects.requireNonNullElse(notes, "");
  }

  public void remove() {
    state.removeMember(this);
  }
}
