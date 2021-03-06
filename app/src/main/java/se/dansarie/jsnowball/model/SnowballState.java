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
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.List;

import javax.swing.ListModel;
import javax.swing.SwingUtilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SnowballState {
  private List<Article> articles = new ArrayList<>();
  private List<Author> authors = new ArrayList<>();
  private List<Journal> journals = new ArrayList<>();
  private List<Tag> tags = new ArrayList<>();
  private Set<SnowballStateMember> updatedMembers = new HashSet<>();
  private int inhibitUpdates = 0;
  private boolean saved = true;
  private ReentrantLock lock = new ReentrantLock();

  private SnowballListModel<Article> articleListModel = new SnowballListModel<>(articles);
  private SnowballListModel<Author> authorListModel = new SnowballListModel<>(authors);
  private SnowballListModel<Journal> journalListModel = new SnowballListModel<>(journals);
  private SnowballListModel<Tag> tagListModel = new SnowballListModel<>(tags);

  public SnowballState() {
  }

  private SnowballState(SerializationProxy sp) {
    lock();
    try {
      pushInhibitUpdates();
      ArrayList<Author> loadedAuthors = new ArrayList<>();
      for (Author.SerializationProxy proxy : sp.authors) {
        Author au = new Author(this);
        au.restoreFromProxy(proxy);
        loadedAuthors.add(au);
      }

      ArrayList<Journal> loadedJournals = new ArrayList<>();
      for (Journal.SerializationProxy proxy : sp.journals) {
        Journal jo = new Journal(this);
        jo.restoreFromProxy(proxy);
        loadedJournals.add(jo);
      }

      ArrayList<Tag> loadedTags = new ArrayList<>();
      for (Tag.SerializationProxy proxy : sp.tags) {
        Tag tag = new Tag(this);
        tag.restoreFromProxy(proxy);
        loadedTags.add(tag);
      }

      ArrayList<Article> loadedArticles = new ArrayList<>();
      for (int i = 0; i < sp.articles.length; i++) {
        Article ar = new Article(this);
        loadedArticles.add(ar);
      }

      for (int i = 0; i < sp.articles.length; i++) {
        loadedArticles.get(i).restoreFromProxy(sp.articles[i], loadedArticles, loadedAuthors,
            loadedJournals, loadedTags);
      }
      popInhibitUpdates();
      saved = true;
    } finally {
      unlock();
    }
  }

  public static SnowballState fromJson(String json) throws JSONException {
    return new SnowballState(new SnowballState.SerializationProxy(new JSONObject(json)));
  }

  void lock() {
    lock.lock();
  }

  void unlock() {
    lock.unlock();
  }

  void pushInhibitUpdates() {
    lock();
    try {
      inhibitUpdates += 1;
    } finally {
      unlock();
    }
  }

  void popInhibitUpdates() {
    lock();
    try {
      inhibitUpdates -= 1;
      if (inhibitUpdates == 0) {
        for (SnowballStateMember member : updatedMembers) {
          fireUpdated(member);
        }
        updatedMembers.clear();
      } else if (inhibitUpdates < 0) {
        throw new IllegalStateException("Popped more inhibits than pushed!");
      }
    } finally {
      unlock();
    }
  }

  void fireUpdated(SnowballStateMember updated) {
    lock();
    try {
      Objects.requireNonNull(updated);
      saved = false;
      if (inhibitUpdates > 0) {
        updatedMembers.add(updated);
        return;
      }
      List<? extends SnowballStateMember> list = null;
      SnowballListModel<? extends SnowballStateMember> listModel = null;
      if (updated instanceof Article) {
        list = articles;
        listModel = articleListModel;
      } else if (updated instanceof Author) {
        list = authors;
        listModel = authorListModel;
      } else if (updated instanceof Journal) {
        list = journals;
        listModel = journalListModel;
      } else if (updated instanceof Tag) {
        for (Article art : getArticles()) {
          if (art.getTags().contains(updated)) {
            art.sortTags();
          }
        }
        list = tags;
        listModel = tagListModel;
      } else {
        throw new IllegalStateException();
      }
      Collections.sort(list);
      @SuppressWarnings("unchecked")
      final SnowballListModel<SnowballStateMember> lm =
          (SnowballListModel<SnowballStateMember>)listModel;
      SwingUtilities.invokeLater(() -> lm.fireChanged(updated));
    } finally {
      unlock();
    }
  }

  public boolean isSaved() {
    lock();
    try {
      return saved;
    } finally {
      unlock();
    }
  }

  void moveDown(Tag tag) {
    lock();
    try {
      if (Objects.requireNonNull(tag).getState() != this) {
        throw new IllegalArgumentException("Attempted to move tag from wrong state.");
      }
      int idx = tags.indexOf(tag);
      if (idx == tags.size() - 1) {
        return;
      }
      Collections.swap(tags, idx, idx + 1);
      fireUpdated(tags.get(idx));
      fireUpdated(tags.get(idx + 1));
    } finally {
      unlock();
    }
  }

  void moveUp(Tag tag) {
    lock();
    try {
      if (Objects.requireNonNull(tag).getState() != this) {
        throw new IllegalArgumentException("Attempted to move tag from wrong state.");
      }
      int idx = tags.indexOf(tag);
      if (idx == 0) {
        return;
      }
      Collections.swap(tags, idx, idx - 1);
      fireUpdated(tags.get(idx));
      fireUpdated(tags.get(idx - 1));
    } finally {
      unlock();
    }
  }

  void removeMember(SnowballStateMember member) {
    lock();
    try {
      if (Objects.requireNonNull(member).getState() != this) {
        throw new IllegalArgumentException("Attempted to remove member from wrong state.");
      }
      saved = false;
      List<? extends SnowballStateMember> list = null;
      SnowballListModel<? extends SnowballStateMember> listModel = null;
      if (member instanceof Article) {
        list = articles;
        listModel = articleListModel;
      } else if (member instanceof Author) {
        list = authors;
        listModel = authorListModel;
      } else if (member instanceof Journal) {
        list = journals;
        listModel = journalListModel;
      } else if (member instanceof Tag) {
        list = tags;
        listModel = tagListModel;
      } else {
        throw new IllegalStateException();
      }
      final int idx = list.indexOf(member);
      list.remove(idx);
      final SnowballListModel<? extends SnowballStateMember> lm = listModel;
      SwingUtilities.invokeLater(() -> lm.fireChanged());
    } finally {
      unlock();
    }
  }

  void addMember(SnowballStateMember member) {
    lock();
    try {
      if (member instanceof Article) {
        addMember((Article)member, articles);
      } else if (member instanceof Author) {
        addMember((Author)member, authors);
      } else  if (member instanceof Journal) {
        addMember((Journal)member, journals);
      } else  if (member instanceof Tag) {
        addMember((Tag)member, tags);
      } else {
        throw new IllegalStateException();
      }
    } finally {
      unlock();
    }
  }

  <E extends SnowballStateMember> void addMember(E member, List<E> li) {
    lock();
    try {
      if (li.contains(Objects.requireNonNull(member))) {
        throw new IllegalStateException();
      }
      if (member.getState() != this) {
        throw new IllegalArgumentException("Attempted to add member belonging to other state.");
      }
      li.add(member);
      fireUpdated(member);
    } finally {
      unlock();
    }
  }

  public Author getAuthorFromStrings(String firstName, String lastName) {
    lock();
    try {
      for (Author a : authors) {
        if (a.getFirstName().equalsIgnoreCase(firstName)
            && a.getLastName().equalsIgnoreCase(lastName)) {
          return a;
        }
      }
      Author a = new Author(this);
      a.setFirstName(firstName);
      a.setLastName(lastName);
      return a;
    } finally {
      unlock();
    }
  }

  public Journal getJournalFromString(String name) {
    lock();
    try {
      for (Journal j : journals) {
        if (j.getName().equals(name)) {
          return j;
        }
      }
      Journal j = new Journal(this);
      j.setName(name);
      return j;
    } finally {
      unlock();
    }
  }

  public Journal getJournalFromIssn(String issn) {
    lock();
    try {
      for (Journal j : journals) {
        if (j.getIssn().equals(issn)) {
          return j;
        }
      }
      Journal j = new Journal(this);
      j.setIssn(issn);
      return j;
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

  public List<Author> getAuthors() {
    lock();
    try {
      return Collections.unmodifiableList(new ArrayList<>(authors));
    } finally {
      unlock();
    }
  }

  public List<Journal> getJournals() {
    lock();
    try {
      return Collections.unmodifiableList(new ArrayList<>(journals));
    } finally {
      unlock();
    }
  }

  public List<Tag> getTags() {
    lock();
    try {
      return Collections.unmodifiableList(new ArrayList<>(tags));
    } finally {
      unlock();
    }
  }

  public List<Article> getStartSet() {
    lock();
    try {
      ArrayList<Article> startSet = new ArrayList<>(articles);
      startSet.removeIf(a -> !a.inStartSet());
      return Collections.unmodifiableList(startSet);
    } finally {
      unlock();
    }
  }

  public ListModel<Article> getArticleListModel() {
    lock();
    try {
      return articleListModel;
    } finally {
      unlock();
    }
  }

  public ListModel<Author> getAuthorListModel() {
    lock();
    try {
      return authorListModel;
    } finally {
      unlock();
    }
  }

  public ListModel<Journal> getJournalListModel() {
    lock();
    try {
      return journalListModel;
    } finally {
      unlock();
    }
  }

  public ListModel<Tag> getTagListModel() {
    lock();
    try {
      return tagListModel;
    } finally {
      unlock();
    }
  }

  public SerializationProxy getSerializationProxy() {
    lock();
    try {
      saved = true;
      return new SerializationProxy(this);
    } finally {
      unlock();
    }
  }

  public static class SerializationProxy {
    private Article.SerializationProxy[] articles;
    private Author.SerializationProxy[] authors;
    private Journal.SerializationProxy[] journals;
    private Tag.SerializationProxy[] tags;

    private SerializationProxy(SnowballState st) {
      Collections.sort(st.articles);
      Collections.sort(st.authors);
      Collections.sort(st.journals);
      Collections.sort(st.tags);
      articles = new Article.SerializationProxy[st.articles.size()];
      for (int i = 0; i < st.articles.size(); i++) {
        articles[i] = st.articles.get(i).getSerializationProxy();
      }
      authors = new Author.SerializationProxy[st.authors.size()];
      for (int i = 0; i < st.authors.size(); i++) {
        authors[i] = st.authors.get(i).getSerializationProxy();
      }
      journals = new Journal.SerializationProxy[st.journals.size()];
      for (int i = 0; i < st.journals.size(); i++) {
        journals[i] = st.journals.get(i).getSerializationProxy();
      }
      tags = new Tag.SerializationProxy[st.tags.size()];
      for (int i = 0; i < st.tags.size(); i++) {
        tags[i] = st.tags.get(i).getSerializationProxy();
      }
    }

    SerializationProxy(JSONObject json) throws JSONException {
      if (!json.getString("version").equals("1.0")) {
        throw new RuntimeException();
      }
      JSONArray ar = json.getJSONArray("articles");
      articles = new Article.SerializationProxy[ar.length()];
      for (int i = 0; i < ar.length(); i++) {
        articles[i] = new Article.SerializationProxy(ar.getJSONObject(i));
      }

      JSONArray au = json.getJSONArray("authors");
      authors = new Author.SerializationProxy[au.length()];
      for (int i = 0; i < au.length(); i++) {
        authors[i] = new Author.SerializationProxy(au.getJSONObject(i));
      }

      JSONArray jo = json.getJSONArray("journals");
      journals = new Journal.SerializationProxy[jo.length()];
      for (int i = 0; i < jo.length(); i++) {
        journals[i] = new Journal.SerializationProxy(jo.getJSONObject(i));
      }

      JSONArray ta = json.getJSONArray("tags");
      tags = new Tag.SerializationProxy[ta.length()];
      for (int i = 0; i < ta.length(); i++) {
        tags[i] = new Tag.SerializationProxy(ta.getJSONObject(i));
      }
    }

    public String toJson() {
      JSONArray articleArray = new JSONArray();
      for (Article.SerializationProxy a : articles) {
        articleArray.put(a.toJson());
      }

      JSONArray authorArray = new JSONArray();
      for (Author.SerializationProxy a : authors) {
        authorArray.put(a.toJson());
      }

      JSONArray journalArray = new JSONArray();
      for (Journal.SerializationProxy j : journals) {
        journalArray.put(j.toJson());
      }

      JSONArray tagArray = new JSONArray();
      for (Tag.SerializationProxy t : tags) {
        tagArray.put(t.toJson());
      }

      JSONObject json = new JSONObject();
      json.put("version", "1.0");
      json.put("articles", articleArray);
      json.put("authors", authorArray);
      json.put("journals", journalArray);
      json.put("tags", tagArray);
      return json.toString(2);
    }
  }
}
