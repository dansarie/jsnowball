package se.dansarie.jsnowball.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.swing.ListModel;

public class Article extends SnowballStateMember {

  private String doi = "";
  private String issue = "";
  private Journal journal = null;
  private String month = "";
  private String pages = "";
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
    setDoi(r.doi);
    setIssue(r.issue);
    setMonth("" + r.issued.getMonthValue());
    setPages(r.page);
    setTitle(r.title);
    setVolume(r.volume);
    setYear("" + r.issued.getYear());
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
    if (r.journal != null) {
      Journal jo = null;
      if (r.issn != null) {
        jo = Journal.getByIssn(state, r.issn);
      }
      if (jo == null) {
        jo = Journal.getByName(state, r.journal);
      }
      if (jo == null) {
        jo = new Journal(state);
        jo.setName(r.journal);
        jo.setIssn(r.issn);
      }
      setJournal(jo);
    }
  }

  public void addAuthor(Author author) {
    if (Objects.requireNonNull(author).getState() != getState()) {
      throw new IllegalArgumentException("Article and author belong to different states.");
    }
    if (!authors.contains(author)) {
      authors.add(author);
      Collections.sort(authors);
      authorsListModel.fireAdded(author);
      fireUpdated();
    }
  }

  public void addReference(Article ref) {
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
      referenceListModel.fireAdded(ref);
      ref.referencesToListModel.fireAdded(this);
      fireUpdated();
      ref.fireUpdated();
    }
  }

  public void addTag(Tag tag) {
    if (tag.getState() != getState()) {
      throw new IllegalArgumentException("Article and tag belong to different states.");
    }
    if (!tags.contains(tag)) {
      tags.add(tag);
      Collections.sort(tags);
      tagListModel.fireAdded(tag);
      fireUpdated();
    }
  }

  public List<Author> getAuthors() {
    return Collections.unmodifiableList(authors);
  }

  public ListModel<Author> getAuthorsListModel() {
    return authorsListModel;
  }


  public String getDoi() {
    return doi;
  }

  public String getIssue() {
    return issue;
  }

  public Journal getJournal() {
    return journal;
  }

  public String getMonth() {
    return month;
  }

  public String getPages() {
    return pages;
  }

  public List<Article> getReferences() {
    return Collections.unmodifiableList(references);
  }

  public ListModel<Article> getReferenceListModel() {
    return referenceListModel;
  }

  public List<Article> getReferencesTo() {
    return Collections.unmodifiableList(referencesTo);
  }

  public ListModel<Article> getReferencesToListModel() {
    return referencesToListModel;
  }

  public List<Tag> getTags() {
    return Collections.unmodifiableList(tags);
  }

  public ListModel<Tag> getTagListModel() {
    return tagListModel;
  }

  public String getTitle() {
    return title;
  }

  public String getVolume() {
    return volume;
  }

  public String getYear() {
    return year;
  }

  public void removeAuthor(Author author) {
    int idx = authors.indexOf(author);
    if (idx < 0) {
      throw new IllegalArgumentException("Attempted to remove non-existing author.");
    }
    authors.remove(author);
    authorsListModel.fireRemoved(idx);
    fireUpdated();
  }

  public void removeReference(Article ref) {
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
    referenceListModel.fireRemoved(idx);
    ref.referencesToListModel.fireRemoved(idx2);
    fireUpdated();
    ref.fireUpdated();
  }

  @Override
  public void remove() {
    for (Article art : new ArrayList<>(getState().getArticles())) {
      if (art.getReferences().contains(this)) {
        art.removeReference(this);
      }
    }
    for (Article art : new ArrayList<>(references)) {
      removeReference(art);
    }
    getState().removeMember(this);
  }

  public void removeTag(Tag tag) {
    int idx = tags.indexOf(Objects.requireNonNull(tag));
    if (idx < 0) {
      throw new IllegalArgumentException("Attempted to remove non-existing tag.");
    }
    tags.remove(idx);
    tagListModel.fireRemoved(idx);
    fireUpdated();
  }

  public void setDoi(String doi) {
    this.doi = Objects.requireNonNullElse(doi, "");
    fireUpdated();
  }

  public void setIssue(String issue) {
    this.issue = Objects.requireNonNullElse(issue, "");
    fireUpdated();
  }

  public void setJournal(Journal journal) {
    this.journal = journal;
    fireUpdated();
  }

  public void setMonth(String month) {
    this.month = Objects.requireNonNullElse(month, "");
    fireUpdated();
  }

  public void setPages(String pages) {
    this.pages = Objects.requireNonNullElse(pages, "");
    fireUpdated();
  }

  public void setTitle(String title) {
    this.title = Objects.requireNonNullElse(title, "");
    fireUpdated();
  }

  public void setVolume(String volume) {
    this.volume = Objects.requireNonNullElse(volume, "");
    fireUpdated();
  }

  public void setYear(String year) {
    this.year = Objects.requireNonNullElse(year, "");
    fireUpdated();
  }

  @Override
  public int compareTo(SnowballStateMember other) {
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
  }

  @Override
  public String toString() {
    return getTitle();
  }

  SerializationProxy getSerializationProxy() {
    return new SerializationProxy(this);
  }

  void restoreFromProxy(SerializationProxy proxy, List<Article> articles, List<Author> authors,
      List<Journal> journals, List<Tag> tags) {
    setDoi(proxy.doi);
    setIssue(proxy.issue);
    setMonth(proxy.month);
    setPages(proxy.pages);
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
  }

  static class SerializationProxy implements Serializable {
    static final long serialVersionUID = 7198211239321687296L;
    private String doi;
    private String issue;
    private String month;
    private String notes;
    private String pages;
    private String title;
    private String volume;
    private String year;
    private int[] authors;
    private int journal;
    private int[] references;
    private int[] tags;

    private SerializationProxy(Article art) {
      doi = art.doi;
      issue = art.issue;
      month = art.month;
      notes = art.getNotes();
      pages = art.pages;
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
  }
 }
