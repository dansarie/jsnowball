package se.dansarie.jsnowball;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.util.ArrayList;

public class Article extends SnowballStateMember {

  private String title = null;
  private String doi = null;
  private ArrayList<Author> authors = new ArrayList<>();
  private Journal journal = null;
  private String year = null;
  private String month = null;
  private String volume = null;
  private String issue = null;
  private String pages = null;

  public Article(SnowballState state) {
    super(state);
  }

  public String getDoi() {
    return doi;
  }

  public Journal getJournal() {
    return journal;
  }

  public String getTitle() {
    return title;
  }

  public String getYear() {
    return year;
  }

  public String getMonth() {
    return month;
  }

  public String getVolume() {
    return volume;
  }

  public String getIssue() {
    return issue;
  }

  public String getPages() {
    return pages;
  }

  public void setDoi(String doi) {
    this.doi = doi;
  }

  public void setJournal(Journal journal) {
    this.journal = journal;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setYear(String year) {
    this.year = year;
  }

  public void setMonth(String month) {
    this.month = month;
  }

  public void setVolume(String volume) {
    this.volume = volume;
  }

  public void setIssue(String issue) {
    this.issue = issue;
  }

  public void setPages(String pages) {
    this.pages = pages;
  }

  public void addAuthor(Author author) {
    if (author == null) {
      throw new IllegalArgumentException();
    }
    if (!authors.contains(author)) {
      authors.add(author);
    }
  }

  public void removeAuthor(Author author) {
    authors.remove(author);
  }

  public static Article fromDoi(SnowballState state, String doi) {
    InputStream stream = null;
    Article art = null;
    try {
      System.out.println("DOI: " + doi);
      URL url = new URL("https://doi.org/" + doi);
      URLConnection connection = url.openConnection();
      connection.setRequestProperty("Accept", "application/vnd.citationstyles.csl+json");
      stream = (InputStream)connection.getContent();
      String doidata = new String(stream.readAllBytes(), "UTF-8");
      System.out.println(doidata);
      art = parseDoiJson(state, doidata);

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
    return art;
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
      JsonArray issnArray = jsonroot.getAsJsonArray("ISSN");
      if (issnArray.size() > 0) {
        issn = issnArray.get(0).getAsString();
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
    return getTitle().compareTo(o.getTitle());
  }
}
