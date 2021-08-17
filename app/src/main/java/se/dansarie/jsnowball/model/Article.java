package se.dansarie.jsnowball.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.swing.ListModel;

public class Article extends SnowballStateMember {

  private String doi = null;
  private String issue = null;
  private Journal journal = null;
  private String month = null;
  private String pages = null;
  private String title = null;
  private String volume = null;
  private String year = null;

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

  public void importReferences() {
    if (doi == null || doi.length() == 0) {
      return;
    }
    String doidata = getDoiJson(doi);
    for (String ref : parseReferencesJson(doidata)) {
      Article refart = fromDoi(getState(), ref);
      addReference(refart);
    }
  }

  public static Article fromDoi(SnowballState state, String doi) {
    return parseDoiJson(state, getDoiJson(doi));
  }

  private static String getDoiJson(String doi) {
    InputStream stream = null;
    try {
      System.out.println("DOI: " + doi);
      URL url = new URL("https://doi.org/" + doi);
      URLConnection connection = url.openConnection();
      connection.setRequestProperty("Accept", "application/vnd.citationstyles.csl+json");
      connection.setRequestProperty("User-Agent", "JSnowball (mailto:marcus@dansarie.se)");
      stream = (InputStream)connection.getContent();
      String doidata = new String(stream.readAllBytes(), "UTF-8");
      System.out.println(doidata);
      return doidata;
    } catch (MalformedURLException ex) {
      System.out.println(ex);
    } catch (IOException ex) {
      System.out.println(ex);
    } finally {
      if (stream != null) {
        try {
          stream.close();
        } catch (IOException ex) {
        }
      }
    }
    return null;
  }

  private static List<String> parseReferencesJson(String doidata) {
    JsonObject jsonroot = JsonParser.parseString(doidata).getAsJsonObject();
    ArrayList<String> refs = new ArrayList<>();
    if (!jsonroot.has("reference")) {
      return refs;
    }
    for (JsonElement ref : jsonroot.getAsJsonArray("reference")) {
      if (!ref.getAsJsonObject().has("DOI")) {
        continue;
      }
      refs.add(ref.getAsJsonObject().getAsJsonPrimitive("DOI").getAsString());
    }
    return refs;
  }

  private static Article parseDoiJson(SnowballState state, String doidata) {
    JsonObject jsonroot = JsonParser.parseString(doidata).getAsJsonObject();
    String title = jsonroot.getAsJsonPrimitive("title").getAsString();
    String year = null;
    String month = null;
    String volume = null;
    String issue = null;
    String pages = null;

    /* Year and month. */
    if (jsonroot.has("issued")) {
      JsonArray date = jsonroot.getAsJsonObject("issued").getAsJsonArray("date-parts").get(0)
          .getAsJsonArray();
      if (date.size() > 0) {
        year = Integer.toString(date.get(0).getAsInt());
      }
      if (date.size() > 1) {
        month = Integer.toString(date.get(1).getAsInt());
      }
    }

    /* Volume. */
    if (jsonroot.has("volume")) {
      volume = jsonroot.getAsJsonPrimitive("volume").getAsString();
    }

    /* Issue. */
    if (jsonroot.has("journal-issue")) {
      issue = jsonroot.getAsJsonObject("journal-issue").getAsJsonPrimitive("issue").getAsString();
    }

    /* Pages. */
    if (jsonroot.has("page")) {
      pages = jsonroot.getAsJsonPrimitive("page").getAsString();
    }

    Article art = new Article(state);
    art.setTitle(title);
    art.setYear(year);
    art.setMonth(month);
    art.setVolume(volume);
    art.setIssue(issue);
    art.setPages(pages);

    /* Authors. */
    if (jsonroot.has("author")) {
      for (JsonElement p : jsonroot.getAsJsonArray("author")) {
        JsonObject po = p.getAsJsonObject();
        String given = po.getAsJsonPrimitive("given").getAsString();
        String family = po.getAsJsonPrimitive("family").getAsString();
        String orcid = null;
        if (po.has("ORCID")) {
          String[] orcidSplit = po.getAsJsonPrimitive("ORCID").getAsString().split("/");
          orcid = orcidSplit[orcidSplit.length - 1];
        }
        Author author = state.getAuthorFromStrings(given, family);
        author.setFirstName(given);
        author.setLastName(family);
        author.setOrcId(orcid);
        art.addAuthor(author);
      }
    }

    /* Journal. */
    if (jsonroot.has("container-title")) {
      String journalName = jsonroot.getAsJsonPrimitive("container-title").getAsString();
      String issn = null;
      JsonElement issnElement = jsonroot.get("ISSN");
      if (issnElement instanceof JsonArray) {
        JsonArray issnArray = jsonroot.getAsJsonArray("ISSN");
        if (issnArray.size() > 0) {
          issn = issnArray.get(0).getAsString();
        }
      } else if (issnElement instanceof JsonPrimitive) {
        issn = issnElement.getAsJsonPrimitive().getAsString();
      }

      Journal journal = state.getJournalFromIssn(issn);
      if (journal.getName() == null) {
        journal.setName(journalName);
      }
      art.setJournal(journal);
    }

    /* DOI */
    if (jsonroot.has("DOI")) {
      String doi = jsonroot.getAsJsonPrimitive("DOI").getAsString();
      art.setDoi(doi);
    }

    return art;
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
