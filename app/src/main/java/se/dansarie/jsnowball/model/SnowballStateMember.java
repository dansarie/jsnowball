package se.dansarie.jsnowball.model;

import java.util.Objects;

abstract class SnowballStateMember implements Comparable<SnowballStateMember> {
  private SnowballState state = null;
  private String notes = "";

  SnowballStateMember(SnowballState state) {
    this.state = Objects.requireNonNull(state);
    lock();
    try {
      state.addMember(this);
    } finally {
      unlock();
    }
  }

  protected void lock() {
    state.lock();
  }

  protected void unlock() {
    state.unlock();
  }

  protected void fireUpdated() {
    lock();
    try {
      state.fireUpdated(this);
    } finally {
      unlock();
    }
  }

  public SnowballState getState() {
    lock();
    try {
      return state;
    } finally {
      unlock();
    }
  }

  public String getNotes() {
    lock();
    try {
      return notes;
    } finally {
      unlock();
    }
  }

  public void setNotes(String notes) {
    lock();
    try {
      this.notes = Objects.requireNonNullElse(notes, "");
    } finally {
      unlock();
    }
  }

  public abstract void remove();
}
