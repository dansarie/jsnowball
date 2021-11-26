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

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;


class SnowballListModel<E> implements ListModel<E> {
  private List<E> list;
  private Set<ListDataListener> listeners = new HashSet<>();

  SnowballListModel(List<E> list) {
    this.list = Objects.requireNonNull(list);
  }

  @Override
  public void addListDataListener(ListDataListener li) {
    listeners.add(Objects.requireNonNull(li));
  }

  void fireAdded(E item) {
    int idx = list.indexOf(item);
    if (idx < 0) {
      throw new IllegalArgumentException();
    }
    ListDataEvent ev = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, idx, idx);
    for (ListDataListener li : listeners) {
      li.intervalAdded(ev);
    }
  }

  void fireChanged() {
    ListDataEvent ev = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, list.size() - 1);
    for (ListDataListener li : listeners) {
      li.contentsChanged(ev);
    }
  }

  void fireChanged(E item) {
    int idx = list.indexOf(item);
    if (idx < 0) {
      return;
    }
    ListDataEvent ev = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, idx, idx);
    for (ListDataListener li : listeners) {
      li.contentsChanged(ev);
    }
  }

  void fireRemoved(int idx) {
    ListDataEvent ev = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, idx, idx);
    for (ListDataListener li : listeners) {
      li.intervalRemoved(ev);
    }
  }

  @Override
  public E getElementAt(int idx) {
    return list.get(idx);
  }

  @Override
  public int getSize() {
    return list.size();
  }

  @Override
  public void removeListDataListener(ListDataListener li) {
    listeners.remove(li);
  }
}
