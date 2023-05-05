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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.json.JSONException;
import org.json.JSONObject;


public class Author extends SnowballStateMember {
  private String firstname = "";
  private String lastname = "";
  private String orgname = "";
  private String orcid = "";
  private String label = "";
  private String stringrepr = null;
  private List<Article> articles = new ArrayList<>();

  public Author(SnowballState state) {
    super(state);
  }

  public Author(SnowballState state, CrossRef.Author r) {
    super(state);
    lock();
    try {
      setFirstName(r.firstName);
      setLastName(r.lastName);
      setOrcId(r.orcid);
    } finally {
      unlock();
    }
  }

  void addArticle(Article art) {
    lock();
    try {
      if(articles.contains(Objects.requireNonNull(art))) {
        return;
      }
      articles.add(art);
      Collections.sort(articles);
    } finally {
      unlock();
    }
  }

  public List<Article> getArticles() {
    lock();
    try {
      return Collections.unmodifiableList(new ArrayList<>(articles));
    } finally {
      unlock();
    }
  }

  public String getFirstName() {
    lock();
    try {
      return firstname;
    } finally {
      unlock();
    }
  }

  public String getLabel() {
    lock();
    try {
      return label;
    } finally {
      unlock();
    }
  }

  public String getLastName() {
    lock();
    try {
      return lastname;
    } finally {
      unlock();
    }
  }

  public String getOrgName() {
    lock();
    try {
      return orgname;
    } finally {
      unlock();
    }
  }

  public String getOrcId() {
    lock();
    try {
      return orcid;
    } finally {
      unlock();
    }
  }

  public void merge(Author merged) {
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
        List<Author> authors = art.getAuthors();
        if (authors.contains(merged)) {
          art.removeAuthor(merged);
          if (!authors.contains(this)) {
            art.addAuthor(this);
          }
        }
      }
      merged.remove();
    } finally {
      unlock();
    }
  }

  @Override
  public void remove() {
    lock();
    try {
      for (Article art : articles) {
        art.removeAuthor(this);
      }
      getState().removeMember(this);
    } finally {
      unlock();
    }
  }

  void removeArticle(Article art) {
    lock();
    try {
      articles.remove(Objects.requireNonNull(art));
    } finally {
      unlock();
    }
  }

  public void setFirstName(String name) {
    lock();
    try {
      stringrepr = null;
      firstname = Objects.requireNonNullElse(name, "");
      fireUpdated();
    } finally {
      unlock();
    }
  }

  public void setLabel(String label) {
    lock();
    try {
      this.label = Objects.requireNonNullElse(label, "");
      fireUpdated();
    } finally {
      unlock();
    }
  }

  public void setLastName(String name) {
    lock();
    try {
      stringrepr = null;
      lastname = Objects.requireNonNullElse(name, "");
      fireUpdated();
    } finally {
      unlock();
    }
  }

  public void setOrgName(String name) {
    lock();
    try {
      orgname = Objects.requireNonNullElse(name, "");
      fireUpdated();
    } finally {
      unlock();
    }
  }

  public void setOrcId(String id) {
    lock();
    try {
      orcid = Objects.requireNonNullElse(id, "");
      fireUpdated();
    } finally {
      unlock();
    }
  }

  @Override
  public int compareTo(SnowballStateMember other) {
    lock();
    try {
      Author o = (Author)other;
      if (getLastName() == null) {
        if (o.getLastName() == null) {
          return 0;
        }
        return -1;
      }
      if (o.getLastName() == null) {
        return 1;
      }
      int ret = getLastName().compareToIgnoreCase(o.getLastName());
      if (ret != 0) {
        return ret;
      }
      if (getFirstName() == null) {
        if (o.getFirstName() == null) {
          return 0;
        }
        return -1;
      }
      if (o.getFirstName() == null) {
        return 1;
      }
      return getFirstName().compareToIgnoreCase(o.getFirstName());
    } finally {
      unlock();
    }
  }

  @Override
  public String toString() {
    lock();
    try {
      if (stringrepr != null) {
        return stringrepr;
      }
      stringrepr = getLastName() + ", " + getFirstName();
      return stringrepr;
    } finally {
      unlock();
    }
  }

  public static Author getByOrcid(SnowballState state, String orcid) {
    for (Author au : state.getAuthors()) {
      if (au.getOrcId().equals(orcid)) {
        return au;
      }
    }
    return null;
  }

  public static Author getByName(SnowballState state, String first, String last) {
    for (Author au : state.getAuthors()) {
      if (au.getFirstName().equalsIgnoreCase(first) && au.getLastName().equalsIgnoreCase(last)) {
        return au;
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
      getState().pushInhibitUpdates();
      setFirstName(proxy.firstname);
      setLabel(proxy.label);
      setLastName(proxy.lastname);
      setNotes(proxy.notes);
      setOrcId(proxy.orcid);
      setOrgName(proxy.orgname);
      getState().popInhibitUpdates();
    } finally {
      unlock();
    }
  }

  static class SerializationProxy {
    private final String firstname;
    private final String lastname;
    private final String notes;
    private final String orcid;
    private final String orgname;
    private final String label;

    SerializationProxy(JSONObject json) throws JSONException {
      firstname = json.getString("firstname");
      lastname = json.getString("lastname");
      notes = json.getString("notes");
      orcid = json.getString("orcid");
      orgname = json.getString("orgname");
      label = json.optString("label", "");
    }

    private SerializationProxy(Author au) {
      firstname = au.firstname;
      lastname = au.lastname;
      notes = au.getNotes();
      orcid = au.orcid;
      orgname = au.orgname;
      label = au.label;
    }

    JSONObject toJson() {
      JSONObject json = new JSONObject();
      json.put("firstname", firstname);
      json.put("lastname", lastname);
      json.put("notes", notes);
      json.put("orcid", orcid);
      json.put("orgname", orgname);
      json.put("label", label);
      return json;
    }
  }
}
