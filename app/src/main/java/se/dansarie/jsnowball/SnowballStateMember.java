package se.dansarie.jsnowball;

import java.util.Objects;

public abstract class SnowballStateMember implements Comparable<SnowballStateMember> {
  protected SnowballState state;

  private String notes = "";

  SnowballStateMember(SnowballState state) {
    this.state = Objects.requireNonNull(state);
    state.addmember(this);
  }

  public SnowballState getState() {
   return state;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = Objects.requireNonNull(notes);
  }
}
