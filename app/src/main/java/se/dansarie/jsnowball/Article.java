package se.dansarie.jsnowball;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.util.ArrayList;

public class Article {

  private String title = null;
  private String doi = null;
  private ArrayList<Author> authors = new ArrayList<>();
  private Journal journal = null;

  public Article() {
  }

  public String getDoi() {
    return doi;
  }

  public String getTitle() {
    return title;
  }

  public void setDoi(String doi) {
    this.doi = doi;
  }

  public void setTitle(String title) {
    this.title = title;
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

  public static Article fromDoi(String doi) {
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
      art = parseDoiJson(doidata);

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

  private static Article parseDoiJson(String doidata) {
    JsonObject jsonroot = JsonParser.parseString(doidata).getAsJsonObject();
    String title = jsonroot.getAsJsonPrimitive("title").getAsString();
    Article art = new Article();
    art.setTitle(title);
    System.out.println("Title: " + title);
    for (JsonElement p : jsonroot.getAsJsonArray("author")) {
      String given = p.getAsJsonObject().getAsJsonPrimitive("given").getAsString();
      String family = p.getAsJsonObject().getAsJsonPrimitive("family").getAsString();
      Author author = new Author(given, family);
      art.addAuthor(author);
    }
    String journal = jsonroot.getAsJsonPrimitive("container-title").getAsString();
    System.out.println("Journal: " + journal);

    return art;
  }
}
