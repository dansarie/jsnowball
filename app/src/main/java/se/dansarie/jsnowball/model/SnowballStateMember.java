package se.dansarie.jsnowball.model;

import java.util.Objects;

public abstract class SnowballStateMember implements Comparable<SnowballStateMember> {
  protected SnowballState state = null;

  private String notes = "";

  public SnowballState getState() {
   return state;
  }

  public void setState(SnowballState state) {
    this.state = state;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = Objects.requireNonNullElse(notes, "");
  }
}
