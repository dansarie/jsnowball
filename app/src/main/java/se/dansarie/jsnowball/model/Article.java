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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Objects;

import javax.swing.ListModel;
import javax.swing.SwingUtilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Article extends SnowballStateMember {

  private String doi = "";
  private String issue = "";
  private Journal journal = null;
  private String month = "";
  private String pages = "";
  private boolean startSet = false;
  private ArticleStatus status = ArticleStatus.UNDECIDED;
  private String title = "";
  private String volume = "";
  private String year = "";

  private ArrayList<Author> authors = new ArrayList<>();
  private ArrayList<Article> references = new ArrayList<>();
  private ArrayList<Article> referencesTo = new ArrayList<>();
  private ArrayList<Tag> tags = new ArrayList<>();

  private SnowballListModel<Author> authorsListModel = new SnowballListModel<>(authors);
  private SnowballListModel<Article> referenceListModel = new SnowballListModel<>(references);
  private SnowballListModel<Article> referencesToListModel = new SnowballListModel<>(referencesTo);
  private SnowballListModel<Tag> tagListModel = new SnowballListModel<>(tags);

  public Article(SnowballState state) {
    super(state);
  }

  public Article(SnowballState state, CrossRef r) {
    super(state);
    lock();
    try {
      setDoi(r.doi);
      setIssue(r.issue);
      setPages(r.page);
      setTitle(r.title);
      setVolume(r.volume);
      if (r.issued != null) {
        setMonth("" + r.issued.getMonthValue());
        setYear("" + r.issued.getYear());
      }
      for (CrossRef.Author a : r.authors) {
        if (a.orcid != null) {
          Author au = Author.getByOrcid(state, a.orcid);
          if (au != null) {
            addAuthor(au);
            continue;
          }
        }
        Author au = Author.getByName(state, a.firstName, a.lastName);
        if (au == null) {
          addAuthor(new Author(state, a));
        } else {
          addAuthor(au);
        }
      }
      setJournal(getJournal(r.journal, r.issn));
    } finally {
      unlock();
    }
  }

  public Article(SnowballState state, ScopusCsv r) {
    super(state);
    lock();
    try {
      setDoi(r.doi);
      setIssue(r.issue);
      setJournal(getJournal(r.journal, r.issn));
      setPages(r.page);
      setTitle(r.title);
      setVolume(r.volume);
      setYear(r.year);
      for (ScopusCsv.Author author : r.authors) {
        Author au = Author.getByName(state, author.firstName, author.lastName);
        if (au == null) {
          au = new Author(state);
          au.setFirstName(author.firstName);
          au.setLastName(author.lastName);
        }
        addAuthor(au);
      }
    } finally {
      unlock();
    }
  }

  public Article(SnowballState state, Arxiv a) {
    super(state);
    lock();
    try {
      setMonth(Integer.toString(a.published.getMonthValue()));
      setNotes(a.id);
      setTitle(a.title);
      setYear(Integer.toString(a.published.getYear()));
      for (Arxiv.Author author : a.authors) {
        Author au = Author.getByName(state, author.firstName, author.lastName);
        if (au == null) {
          au = new Author(state);
          au.setFirstName(author.firstName);
          au.setLastName(author.lastName);
        }
        addAuthor(au);
      }
      Journal jo = Journal.getByName(state, "ArXiv");
      if (jo == null) {
        jo = new Journal(state);
        jo.setName("ArXiv");
      }
      setJournal(jo);
    } finally {
      unlock();
    }
  }

  private Journal getJournal(String name, String issn) {
    Journal jo = null;
    SnowballState state = getState();
    if (issn != null) {
      jo = Journal.getByIssn(state, issn);
      if (jo != null) {
        return jo;
      }
    }
    if (name != null) {
      jo = Journal.getByName(state, name);
      if (jo != null) {
        return jo;
      }
      jo = new Journal(state);
      jo.setName(name);
      jo.setIssn(issn);
      return jo;
    }
    return null;
  }

  public void addAuthor(Author author) {
    lock();
    try {
      if (Objects.requireNonNull(author).getState() != getState()) {
        throw new IllegalArgumentException("Article and author belong to different states.");
      }
      if (!authors.contains(author)) {
        authors.add(author);
        author.addArticle(this);
        Collections.sort(authors);
        SwingUtilities.invokeLater(() -> authorsListModel.fireAdded(author));
        fireUpdated();
      }
    } finally {
      unlock();
    }
  }

  public void addReference(Article ref) {
    lock();
    try {
      if (Objects.requireNonNull(ref) == this) {
        throw new IllegalArgumentException("Circular references not allowed.");
      }
      if (ref.getState() != getState()) {
        throw new IllegalArgumentException("Article and reference belong to different states.");
      }
      if (!references.contains(ref)) {
        if (ref.referencesTo.contains(this)) {
          throw new IllegalStateException();
        }
        references.add(ref);
        ref.referencesTo.add(this);
        Collections.sort(references);
        Collections.sort(ref.referencesTo);
        SwingUtilities.invokeLater(() -> {
          referenceListModel.fireAdded(ref);
          ref.referencesToListModel.fireAdded(this);
        });
        fireUpdated();
        ref.fireUpdated();
      }
    } finally {
      unlock();
    }
  }

  public void addTag(Tag tag) {
    lock();
    try {
      if (tag.getState() != getState()) {
        throw new IllegalArgumentException("Article and tag belong to different states.");
      }
      if (!tags.contains(tag)) {
        tags.add(tag);
        Collections.sort(tags);
        SwingUtilities.invokeLater(() -> tagListModel.fireAdded(tag));
        fireUpdated();
      }
    } finally {
      unlock();
    }
  }

  public int distanceTo(Article... articles) {
    lock();
    try {
      List<Article> destinations = Arrays.asList(articles);
      if (destinations.contains(this)) {
        return 0;
      }

      List<Article> all = getState().getArticles();
      Map<Article, Integer> dist = new HashMap<>();
      for (Article a : all) {
        dist.put(a, -1);
      }
      for (Article a : destinations) {
        dist.put(a, 0);
      }

      boolean run = true;
      for (int distance = 0; run; distance++) {
        run = false;
        for (Article a : all) {
          if (dist.get(a) != distance) {
            continue;
          }
          HashSet<Article> neighbors = new HashSet<>(a.getReferences());
          neighbors.addAll(a.getReferencesTo());
          for (Article neighbor : neighbors) {
            if (neighbor == this) {
              return distance + 1;
            }
            if (dist.get(neighbor) < 0) {
              dist.put(neighbor, distance + 1);
              run = true;
            }
          }
        }
      }
      return -1;
    } finally {
      unlock();
    }
  }

  @Override
  protected void fireUpdated() {
    lock();
    try {
      for (Author au : getAuthors()) {
        au.fireUpdated();
      }
      if (getJournal() != null) {
        getJournal().fireUpdated();
      }
      super.fireUpdated();
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

  public ListModel<Author> getAuthorsListModel() {
    lock();
    try {
      return authorsListModel;
    } finally {
      unlock();
    }
  }

  public String getDoi() {
    lock();
    try {
      return doi;
    } finally {
      unlock();
    }
  }

  public String getIssue() {
    lock();
    try {
      return issue;
    } finally {
      unlock();
    }
  }

  public Journal getJournal() {
    lock();
    try {
      return journal;
    } finally {
      unlock();
    }
  }

  public String getMonth() {
    lock();
    try {
      return month;
    } finally {
      unlock();
    }
  }

  public ArticleStatus getStatus() {
    lock();
    try {
      return status;
    } finally {
      unlock();
    }
  }

  public String getPages() {
    lock();
    try {
      return pages;
    } finally {
      unlock();
    }
  }

  public List<Article> getReferences() {
    lock();
    try {
      return Collections.unmodifiableList(new ArrayList<>(references));
    } finally {
      unlock();
    }
  }

  public ListModel<Article> getReferenceListModel() {
    lock();
    try {
      return referenceListModel;
    } finally {
      unlock();
    }
  }

  public List<Article> getReferencesTo() {
    lock();
    try {
      return Collections.unmodifiableList(new ArrayList<>(referencesTo));
    } finally {
      unlock();
    }
  }

  public ListModel<Article> getReferencesToListModel() {
    lock();
    try {
      return referencesToListModel;
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

  public ListModel<Tag> getTagListModel() {
    lock();
    try {
      return tagListModel;
    } finally {
      unlock();
    }
  }

  public String getTitle() {
    lock();
    try {
      return title;
    } finally {
      unlock();
    }
  }

  public String getVolume() {
    lock();
    try {
      return volume;
    } finally {
      unlock();
    }
  }

  public String getYear() {
    lock();
    try {
      return year;
    } finally {
      unlock();
    }
  }

  public boolean inStartSet() {
    lock();
    try {
      return startSet;
    } finally {
      unlock();
    }
  }

  public void merge(Article merged) {
    lock();
    try {
      if (Objects.requireNonNull(merged) == this) {
        throw new IllegalArgumentException("Attempted to merge article with itself.");
      }
      SnowballState state = getState();
      if (merged.getState() != state) {
        throw new IllegalArgumentException("Attempted to merge articles belonging to different "
            + "states.");
      }

      for (Author au : merged.getAuthors()) {
        if (!getAuthors().contains(au)) {
          addAuthor(au);
        }
      }

      for (Article art : state.getArticles()) {
        if (art == this || art == merged) {
          continue;
        }
        if (art.getReferences().contains(merged)) {
          art.removeReference(merged);
          if (!art.getReferences().contains(this)) {
            art.addReference(this);
          }
        }
      }

      for (Article ref : merged.getReferences()) {
        if (!getReferences().contains(ref) && ref != this) {
          addReference(ref);
        }
      }

      for (Tag tag : merged.getTags()) {
        if (!getTags().contains(tag)) {
          addTag(tag);
        }
      }
      merged.remove();
    } finally {
      unlock();
    }
  }

  public void removeAuthor(Author author) {
    lock();
    try {
      int idx = authors.indexOf(author);
      if (idx < 0) {
        throw new IllegalArgumentException("Attempted to remove non-existing author.");
      }
      authors.remove(author);
      author.removeArticle(this);
      SwingUtilities.invokeLater(() -> authorsListModel.fireRemoved(idx));
      fireUpdated();
    } finally {
      unlock();
    }
  }

  public void removeReference(Article ref) {
    lock();
    try {
      int idx = references.indexOf(Objects.requireNonNull(ref));
      if (idx < 0) {
        throw new IllegalArgumentException("Attempted to remove non-existing reference.");
      }
      int idx2 = ref.referencesTo.indexOf(this);
      if (idx < 0) {
        throw new IllegalStateException();
      }
      references.remove(idx);
      ref.referencesTo.remove(idx2);
      SwingUtilities.invokeLater(() -> {
        referenceListModel.fireRemoved(idx);
        ref.referencesToListModel.fireRemoved(idx2);
      });
      fireUpdated();
      ref.fireUpdated();
    } finally {
      unlock();
    }
  }

  @Override
  public void remove() {
    lock();
    try {
      for (Article art : getState().getArticles()) {
        if (art.getReferences().contains(this)) {
          art.removeReference(this);
        }
      }
      for (Article art : getReferences()) {
        removeReference(art);
      }
      getState().removeMember(this);
    } finally {
      unlock();
    }
  }

  public void removeTag(Tag tag) {
    lock();
    try {
      int idx = tags.indexOf(Objects.requireNonNull(tag));
      if (idx < 0) {
        throw new IllegalArgumentException("Attempted to remove non-existing tag.");
      }
      tags.remove(idx);
      SwingUtilities.invokeLater(() -> tagListModel.fireRemoved(idx));
      fireUpdated();
    } finally {
      unlock();
    }
  }

  public void setDoi(String doi) {
    lock();
    try {
      this.doi = Objects.requireNonNullElse(doi, "");
      fireUpdated();
    } finally {
      unlock();
    }
  }

  public void setIssue(String issue) {
    lock();
    try {
      this.issue = Objects.requireNonNullElse(issue, "");
      fireUpdated();
    } finally {
      unlock();
    }
  }

  public void setJournal(Journal journal) {
    lock();
    try {
      this.journal = journal;
      fireUpdated();
    } finally {
      unlock();
    }
  }

  public void setMonth(String month) {
    lock();
    try {
      this.month = Objects.requireNonNullElse(month, "");
      fireUpdated();
    } finally {
      unlock();
    }
  }

  public void setPages(String pages) {
    lock();
    try {
      this.pages = Objects.requireNonNullElse(pages, "");
      fireUpdated();
    } finally {
      unlock();
    }
  }

  public void setStatus(ArticleStatus status) {
    lock();
    try {
      this.status = Objects.requireNonNull(status);
      fireUpdated();
    } finally {
      unlock();
    }
  }

  public void setStartSet(boolean inStartSet) {
    lock();
    try {
      startSet = inStartSet;
      fireUpdated();
    } finally {
      unlock();
    }
  }

  public void setTitle(String title) {
    lock();
    try {
      this.title = Objects.requireNonNullElse(title, "");
      fireUpdated();
    } finally {
      unlock();
    }
  }

  public void setVolume(String volume) {
    lock();
    try {
      this.volume = Objects.requireNonNullElse(volume, "");
      fireUpdated();
    } finally {
      unlock();
    }
  }

  public void setYear(String year) {
    lock();
    try {
      this.year = Objects.requireNonNullElse(year, "");
      fireUpdated();
    } finally {
      unlock();
    }
  }

  void sortTags() {
    lock();
    try {
      Collections.sort(tags);
      fireUpdated();
    } finally {
      unlock();
    }
  }

  @Override
  public int compareTo(SnowballStateMember other) {
    lock();
    try {
      Article o = (Article)other;
      if (getTitle() == null) {
        if (o.getTitle() == null) {
          return 0;
        }
        return -1;
      }
      if (o.getTitle() == null) {
        return 1;
      }
      return getTitle().compareToIgnoreCase(o.getTitle());
    } finally {
      unlock();
    }
  }

  @Override
  public String toString() {
    lock();
    try {
      return getTitle();
    } finally {
      unlock();
    }
  }

  public static Article getByDoi(SnowballState state, String doi) {
    for (Article art : state.getArticles()) {
      if (art.getDoi().equalsIgnoreCase(doi.trim())) {
        return art;
      }
    }
    return null;
  }

  public static Article getByTitle(SnowballState state, String title) {
    for (Article art : state.getArticles()) {
      if(art.getTitle().equals(title)) {
        return art;
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

  void restoreFromProxy(SerializationProxy proxy, List<Article> articles,
      List<Author> authors, List<Journal> journals, List<Tag> tags) {
    lock();
    try {
      setDoi(proxy.doi);
      setIssue(proxy.issue);
      setMonth(proxy.month);
      setPages(proxy.pages);
      setStartSet(proxy.startSet);
      setStatus(ArticleStatus.valueOf(proxy.status));
      setTitle(proxy.title);
      setVolume(proxy.volume);
      setYear(proxy.year);
      setNotes(proxy.notes);
      if (proxy.journal >= 0) {
        setJournal(journals.get(proxy.journal));
      }
      for (int i : proxy.authors) {
        addAuthor(authors.get(i));
      }
      for (int i : proxy.references) {
        addReference(articles.get(i));
      }
      for (int i : proxy.tags) {
        addTag(tags.get(i));
      }
    } finally {
      unlock();
    }
  }

  public enum ArticleStatus {
    INCLUDED, EXCLUDED, UNDECIDED
  }

  static class SerializationProxy {
    private final String doi;
    private final String issue;
    private final String month;
    private final String notes;
    private final String pages;
    private final boolean startSet;
    private final String status;
    private final String title;
    private final String volume;
    private final String year;
    private final int[] authors;
    private final int journal;
    private final int[] references;
    private final int[] tags;

    SerializationProxy(JSONObject json) throws JSONException {
      doi = json.getString("doi");
      issue = json.getString("issue");
      journal = json.getInt("journal");
      month = json.getString("month");
      notes = json.getString("notes");
      pages = json.getString("pages");
      startSet = json.optBoolean("startset", false);
      status = json.getString("status");
      title = json.getString("title");
      volume = json.getString("volume");
      year = json.getString("year");

      JSONArray au = json.getJSONArray("authors");
      authors = new int[au.length()];
      for (int i = 0; i < au.length(); i++) {
        authors[i] = au.getInt(i);
      }

      JSONArray ref = json.getJSONArray("references");
      references = new int[ref.length()];
      for (int i = 0; i < ref.length(); i++) {
        references[i] = ref.getInt(i);
      }

      JSONArray ta = json.getJSONArray("tags");
      tags = new int[ta.length()];
      for (int i = 0; i < ta.length(); i++) {
        tags[i] = ta.getInt(i);
      }
    }

    private SerializationProxy(Article art) {
      doi = art.doi;
      issue = art.issue;
      month = art.month;
      notes = art.getNotes();
      pages = art.pages;
      startSet = art.startSet;
      status = art.status.toString();
      title = art.title;
      volume = art.volume;
      year = art.year;
      SnowballState state = art.getState();

      authors = new int[art.authors.size()];
      List<Author> aulist = state.getAuthors();
      for (int i = 0; i < art.authors.size(); i++) {
        authors[i] = aulist.indexOf(art.authors.get(i));
      }

      if (art.journal == null) {
        journal = -1;
      } else {
        journal = state.getJournals().indexOf(art.journal);
      }

      references = new int[art.references.size()];
      List<Article> articles = state.getArticles();
      for (int i = 0; i < art.references.size(); i++) {
        references[i] = articles.indexOf(art.references.get(i));
      }

      tags = new int[art.tags.size()];
      List<Tag> taglist = state.getTags();
      for (int i = 0; i < art.tags.size(); i++) {
        tags[i] = taglist.indexOf(art.tags.get(i));
      }
    }

    JSONObject toJson() {
      JSONObject json = new JSONObject();
      json.put("doi", doi);
      json.put("issue", issue);
      json.put("journal", journal);
      json.put("month", month);
      json.put("notes", notes);
      json.put("pages", pages);
      json.put("startset", startSet);
      json.put("status", status.toString());
      json.put("title", title);
      json.put("volume", volume);
      json.put("year", year);
      json.put("authors", authors);
      json.put("references", references);
      json.put("tags", tags);
      return json;
    }
  }
 }
