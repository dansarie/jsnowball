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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Arxiv {
  public final String id;
  public final String title;
  public final LocalDateTime published;
  public final LocalDateTime updated;
  public final List<Arxiv.Author> authors;

  private Arxiv(Document xml) {
    NodeList elementList = xml.getElementsByTagName("entry");
    Node element = elementList.item(0);
    id = getText(element, "id");
    published = LocalDateTime.parse(getText(element, "published"), DateTimeFormatter.ISO_DATE_TIME);
    title = getText(element, "title");
    if (title.equals("Error")) {
      throw new IllegalArgumentException();
    }
    updated = LocalDateTime.parse(getText(element, "updated"), DateTimeFormatter.ISO_DATE_TIME);
    ArrayList<Arxiv.Author> authorList = new ArrayList<>();
    for (Node no = element.getFirstChild(); no != null; no = no.getNextSibling()) {
      if (no.getNodeName().equals("author")) {
        authorList.add(new Arxiv.Author(no.getTextContent()));
      }
    }
    authors = Collections.unmodifiableList(authorList);
  }

  private String getText(Node element, String tag) {
    NodeList nl = element.getChildNodes();
    for (int i = 0; i < nl.getLength(); i++) {
      Node no = nl.item(i);
      if (no.getNodeName().equals(tag)) {
        return no.getTextContent().trim();
      }
    }
    return null;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("ArXiv [");
    sb.append("id: ");
    sb.append(id);
    sb.append("title: ");
    sb.append(title);
    sb.append("published: ");
    sb.append(published);
    sb.append("updated: ");
    sb.append(updated);
    sb.append("# authors: ");
    sb.append(authors.size());
    sb.append("]");
    return sb.toString();
  }

  public synchronized static Arxiv getArxiv(String id) throws IOException {

    InputStream stream = null;
    try {
      URL url = new URL("https://export.arxiv.org/api/query?id_list=" + id);
      URLConnection connection = url.openConnection();
      stream = (InputStream)connection.getContent();
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setIgnoringComments(true);
      factory.setIgnoringElementContentWhitespace(true);
      factory.setNamespaceAware(false);
      factory.setValidating(false);
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document xml = builder.parse(stream);
      return new Arxiv(xml);
    } catch (IOException ex) {
      System.out.println(ex);
      ex.printStackTrace();
      throw ex;
    } catch (ParserConfigurationException | SAXException ex) {
      System.out.println(ex);
      ex.printStackTrace();
      return null;
    } finally {
      if (stream != null) {
        try {
          stream.close();
        } catch (IOException ex) {
          System.out.println(ex);
        }
      }
    }
  }

  public static class Author {
    public final String firstName;
    public final String lastName;

    private Author(String author) {
      author = author.trim();
      int idx = author.lastIndexOf(" ");
      if (idx < 0) {
        firstName = "";
        lastName = author;
      } else {
        lastName = author.substring(idx + 1);
        firstName = author.substring(0, idx);
      }
    }
  }
}
