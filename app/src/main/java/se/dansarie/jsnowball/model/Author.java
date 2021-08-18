package se.dansarie.jsnowball.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class Author extends SnowballStateMember {
  private String firstname = "";
  private String lastname = "";
  private String orgname = "";
  private String orcid = "";

  public Author(SnowballState state) {
    super(state);
  }

  public Author(SnowballState state, CrossRef.Author r) {
    super(state);
    setFirstName(r.firstName);
    setLastName(r.lastName);
    setOrcId(r.orcid);
  }

  public synchronized List<Article> getArticles() {
    ArrayList<Article> articles = new ArrayList<>();
    for (Article art : getState().getArticles()) {
      if (art.getAuthors().contains(this)) {
        articles.add(art);
      }
    }
    return articles;
  }

  public synchronized String getFirstName() {
    return firstname;
  }

  public synchronized String getLastName() {
    return lastname;
  }

  public synchronized String getOrgName() {
    return orgname;
  }

  public synchronized String getOrcId() {
    return orcid;
  }

  public synchronized void merge(Author merged) {
    if (Objects.requireNonNull(merged) == this) {
      throw new IllegalArgumentException("Attempted to merge author with itself.");
    }
    if (merged.getState() != getState()) {
      throw new IllegalArgumentException("Attempted to merge authors belonging to different "
          + "states.");
    }
    for (Article art : new ArrayList<>(getState().getArticles())) {
      List<Author> authors = art.getAuthors();
      if (authors.contains(merged)) {
        art.removeAuthor(merged);
        if (!authors.contains(this)) {
          art.addAuthor(this);
        }
      }
    }
    merged.remove();
  }

  @Override
  public synchronized void remove() {
    for (Article art : new ArrayList<>(getState().getArticles())) {
      if (art.getAuthors().contains(this)) {
        art.removeAuthor(this);
      }
    }
    getState().removeMember(this);
  }

  public synchronized void setFirstName(String name) {
    firstname = Objects.requireNonNullElse(name, "");
    fireUpdated();
  }

  public synchronized void setLastName(String name) {
    lastname = Objects.requireNonNullElse(name, "");
    fireUpdated();
  }

  public synchronized void setOrgName(String name) {
    orgname = Objects.requireNonNullElse(name, "");
    fireUpdated();
  }

  public synchronized void setOrcId(String id) {
    orcid = Objects.requireNonNullElse(id, "");
    fireUpdated();
  }

  @Override
  public synchronized int compareTo(SnowballStateMember other) {
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
  }

  @Override
  public synchronized String toString() {
    return getLastName() + ", " + getFirstName();
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

  synchronized SerializationProxy getSerializationProxy() {
    return new SerializationProxy(this);
  }

  synchronized void restoreFromProxy(SerializationProxy proxy) {
    setFirstName(proxy.firstname);
    setLastName(proxy.lastname);
    setNotes(proxy.notes);
    setOrcId(proxy.orcid);
    setOrgName(proxy.orgname);
  }

  static class SerializationProxy implements Serializable {
    static final long serialVersionUID = 5154001552990566490L;
    private String firstname;
    private String lastname;
    private String notes;
    private String orcid;
    private String orgname;

    private SerializationProxy(Author au) {
      firstname = au.firstname;
      lastname = au.lastname;
      notes = au.getNotes();
      orcid = au.orgname;
      orgname = au.orgname;
    }
  }
}
