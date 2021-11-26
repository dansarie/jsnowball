/* Copyright (c) 2021 Marcus Dansarie

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
