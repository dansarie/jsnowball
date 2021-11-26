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

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CrossRef {
  public final String publisher;
  public final String title;
  public final int referenceCount;
  public final int referencedByCount;
  public final String source;
  public final String doiPrefix;
  public final String doi;
  public final String url;
  public final String member;
  public final String type;
  public final LocalDateTime created;
  public final LocalDateTime deposited;
  public final LocalDateTime indexed;
  public final LocalDate issued;
  public final String issue;
  public final String volume;
  public final String page;
  public final String journal;
  public final String issn;
  public final List<CrossRef.Author> authors;
  public final List<CrossRef.Reference> references;

  private CrossRef(JSONObject json) throws DateTimeParseException, JSONException {
    publisher = json.getString("publisher");
    title = json.getJSONArray("title").getString(0);
    referenceCount = json.getInt("references-count");
    referencedByCount = json.getInt("is-referenced-by-count");
    source = json.getString("source");
    doiPrefix = json.getString("prefix");
    doi = json.getString("DOI");
    url = json.getString("URL");
    member = json.getString("member");
    type = json.getString("type");

    created = LocalDateTime.parse(json.getJSONObject("created").getString("date-time"),
        DateTimeFormatter.ISO_DATE_TIME);
    deposited = LocalDateTime.parse(json.getJSONObject("deposited").getString("date-time"),
        DateTimeFormatter.ISO_DATE_TIME);
    indexed = LocalDateTime.parse(json.getJSONObject("indexed").getString("date-time"),
        DateTimeFormatter.ISO_DATE_TIME);
    JSONArray issuedArray = json.getJSONObject("issued").getJSONArray("date-parts").getJSONArray(0);
    if (!issuedArray.isNull(0)) {
      issued = LocalDate.of(issuedArray.getInt(0), issuedArray.optInt(1, 1),
          issuedArray.optInt(2, 1));
    } else {
      issued = null;
    }

    issue = json.optString("issue", null);
    volume = json.optString("volume", null);
    page = json.optString("page", null);

    if (json.has("container-title") && !json.getJSONArray("container-title").isEmpty()) {
      journal = json.getJSONArray("container-title").getString(0);
    } else {
      journal = null;
    }

    if (json.has("ISSN") && !json.getJSONArray("ISSN").isEmpty()) {
      issn = json.getJSONArray("ISSN").getString(0);
    } else {
      issn = null;
    }

    ArrayList<CrossRef.Author> authors = new ArrayList<>();
    this.authors = Collections.unmodifiableList(authors);
    if (json.has("author")) {
      for (Object o : json.getJSONArray("author")) {
        authors.add(new CrossRef.Author((JSONObject)o));
      }
    }

    ArrayList<CrossRef.Reference> references = new ArrayList<>();
    this.references = Collections.unmodifiableList(references);
    if (json.has("reference")) {
      for (Object o : json.getJSONArray("reference")) {
        references.add(new CrossRef.Reference((JSONObject)o));
      }
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("CrossRef [");
    sb.append("publisher: ");
    sb.append(publisher);
    sb.append(" title: ");
    sb.append(title);
    sb.append(" referenceCount: ");
    sb.append(referenceCount);
    sb.append(" referencedByCount: ");
    sb.append(referencedByCount);
    sb.append(" source: ");
    sb.append(source);
    sb.append(" doiPrefix: ");
    sb.append(doiPrefix);
    sb.append(" doi: ");
    sb.append(doi);
    sb.append(" url: ");
    sb.append(url);
    sb.append(" member: ");
    sb.append(member);
    sb.append(" type: ");
    sb.append(type);
    sb.append(" created: ");
    sb.append(created);
    sb.append(" deposited: ");
    sb.append(deposited);
    sb.append(" indexed: ");
    sb.append(indexed);
    sb.append(" issued: ");
    sb.append(issued);
    sb.append(" issue: ");
    sb.append(issue);
    sb.append(" volume: ");
    sb.append(volume);
    sb.append(" page: ");
    sb.append(page);
    sb.append(" journal: ");
    sb.append(journal);
    sb.append(" issn: ");
    sb.append(issn);
    sb.append(" #references: ");
    sb.append(references.size());
    sb.append("]");
    return sb.toString();
  }

  private static int rate_limit = -1;
  private static int rate_limit_interval = -1;
  private static List<Long> previousRequests = new ArrayList<>();

  public synchronized static CrossRef getDoi(String doi) throws IOException {
    if (rate_limit > 0 && rate_limit_interval > 0) {
      long timenow = System.nanoTime();
      previousRequests.removeIf(t -> (timenow - t) > rate_limit_interval * 1000000000L);
      if (previousRequests.size() >= rate_limit) {
        long sleeptime = rate_limit_interval * 1000000000L +
            previousRequests.get(previousRequests.size() - rate_limit) - timenow;
        try {
          Thread.sleep(sleeptime / 1000000L, (int)(sleeptime % 1000000L));
        } catch (InterruptedException ex) {
        }
        return getDoi(doi);
      }
    }
    previousRequests.add(System.nanoTime());

    InputStream stream = null;
    String jsondata = null;
    try {
      URL url = new URL("https://api.crossref.org/works/" + doi);
      URLConnection connection = url.openConnection();
      connection.setRequestProperty("User-Agent", "JSnowball; (mailto:marcus@dansarie.se)");
      stream = (InputStream)connection.getContent();
      jsondata = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
      String limitstr = connection.getHeaderField("X-Rate-Limit-Limit");
      String intervalstr = connection.getHeaderField("X-Rate-Limit-Interval");
      if (limitstr != null && intervalstr != null) {
        intervalstr = intervalstr.trim();
        intervalstr = intervalstr.substring(0, intervalstr.length() - 1);
        try {
          int limit = Integer.parseInt(limitstr);
          int interval = Integer.parseInt(intervalstr);
          if (limit > 0 && interval > 0) {
            rate_limit = limit;
            rate_limit_interval = interval;
          }
        } catch (NumberFormatException ex) {
          System.out.println(ex);
        }
      }
    } catch (IOException ex) {
      System.out.println(ex);
      throw ex;
    } finally {
      if (stream != null) {
        try {
          stream.close();
        } catch (IOException ex) {
          System.out.println(ex);
        }
      }
    }

    try {
      JSONObject json = new JSONObject(jsondata);
      if (!json.getString("status").equals("ok")) {
        throw new JSONException("Status not ok.");
      }
      if (!json.getString("message-type").equals("work")) {
        throw new JSONException("Unexpected return message.");
      }
      return new CrossRef(json.getJSONObject("message"));
    } catch (DateTimeParseException | JSONException ex) {
      System.out.println(ex);
      ex.printStackTrace();
      throw ex;
    }
  }

  public synchronized static void addCrossRefReference(Article art, CrossRef.Reference ref)
      throws IOException {
    SnowballState state = art.getState();
    Article a = null;
    if (ref.doi != null) {
      a = Article.getByDoi(state, ref.doi);
    }
    if (a == null && ref.title != null) {
      a = Article.getByTitle(state, ref.title);
    }
    if (a == null && ref.doi != null) {
      CrossRef cr = getDoi(ref.doi);
      a = new Article(art.getState(), cr);
    } else if (a == null && ref.title != null) {
      a = new Article(art.getState());
      a.setTitle(ref.title);
      a.setIssue(ref.issue);
      a.setPages(ref.page);
      a.setVolume(ref.volume);
      a.setYear(ref.year);

      Journal j = null;
      if (ref.issn != null) {
        j = Journal.getByIssn(state, ref.issn);
      }
      if (j == null && ref.journal != null) {
        j = Journal.getByName(state, ref.journal);
        if (j == null) {
          j = new Journal(state);
          j.setName(ref.journal);
          if (ref.issn != null) {
            j.setIssn(ref.issn);
          }
        }
      }
      if (j != null) {
        a.setJournal(j);
      }

      if (ref.author != null) {
        se.dansarie.jsnowball.model.Author au =
            se.dansarie.jsnowball.model.Author.getByName(state, "", ref.author);
        if (au == null) {
          au = new se.dansarie.jsnowball.model.Author(state);
          au.setLastName(ref.author);
        }
        a.addAuthor(au);
      }
    }

    if (a != null) {
      art.addReference(a);
    }
  }

  public static class Author {
    public final String firstName;
    public final String lastName;
    public final String orcid;

    private Author(JSONObject a) throws JSONException {
      lastName = a.getString("family");
      firstName = a.optString("given", null);
      orcid = a.optString("ORCID", null);
    }
  }

  public static class Reference {
    public final String author;
    public final String doi;
    public final String issn;
    public final String issue;
    public final String journal;
    public final String key;
    public final String page;
    public final String title;
    public final String volume;
    public final String year;

    private Reference(JSONObject r) throws JSONException {
      author = r.optString("author", null);
      doi = r.optString("DOI", null);
      issn = r.optString("ISSN", null);
      issue = r.optString("issue", null);
      journal = r.optString("journal-title", null);
      key = r.getString("key");
      page = r.optString("first-page", null);
      title = r.optString("article-title", null);
      volume = r.optString("volume", null);
      year = r.optString("year", null);
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("CrossRef.Reference [");
      sb.append("author: ");
      sb.append(author);
      sb.append(" doi: ");
      sb.append(doi);
      sb.append(" issn: ");
      sb.append(issn);
      sb.append(" issue: ");
      sb.append(issue);
      sb.append(" journal: ");
      sb.append(journal);
      sb.append(" key: ");
      sb.append(key);
      sb.append(" page: ");
      sb.append(page);
      sb.append(" title: ");
      sb.append(title);
      sb.append(" volume: ");
      sb.append(volume);
      sb.append(" year: ");
      sb.append(year);
      sb.append("]");
      return sb.toString();
    }
  }
}
