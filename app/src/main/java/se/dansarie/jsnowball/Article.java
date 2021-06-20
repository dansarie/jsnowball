package se.dansarie.jsnowball;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;

public class Article {

  private String title = "";

  public Article() {
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
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
      JsonObject jsonroot = JsonParser.parseString(doidata).getAsJsonObject();
      art = new Article();
      art.setTitle(jsonroot.getAsJsonPrimitive("title").getAsString());

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
}
