package se.dansarie.jsnowball.model;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.List;

import javax.swing.ListModel;

public class SnowballState implements Serializable {
  private List<Article> articles = new ArrayList<>();
  private List<Author> authors = new ArrayList<>();
  private List<Journal> journals = new ArrayList<>();
  private List<Tag> tags = new ArrayList<>();
  private boolean saved = true;

  private SnowballListModel<Article> articleListModel = new SnowballListModel<>(articles);
  private SnowballListModel<Author> authorListModel = new SnowballListModel<>(authors);
  private SnowballListModel<Journal> journalListModel = new SnowballListModel<>(journals);
  private SnowballListModel<Tag> tagListModel = new SnowballListModel<>(tags);

  public SnowballState() {
  }

  private SnowballState(SerializationProxy sp) throws InvalidObjectException {
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
    saved = true;
  }

  void fireUpdated(SnowballStateMember updated) {
    Objects.requireNonNull(updated);
    saved = false;
    if (updated instanceof Article) {
      Collections.sort(articles);
      articleListModel.fireChanged((Article)updated);
    } else if (updated instanceof Author) {
      Collections.sort(authors);
      authorListModel.fireChanged((Author)updated);
    } else if (updated instanceof Journal) {
      Collections.sort(journals);
      journalListModel.fireChanged((Journal)updated);
    } else if (updated instanceof Tag) {
      Collections.sort(tags);
      tagListModel.fireChanged((Tag)updated);
    } else {
      throw new IllegalStateException();
    }
  }

  public boolean isSaved() {
    return saved;
  }

  void removeMember(SnowballStateMember member) {
    if (Objects.requireNonNull(member).getState() != this) {
      throw new IllegalArgumentException("Attempted to remove member from wrong state.");
    }

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
    int idx = list.indexOf(member);
    list.remove(idx);
    listModel.fireRemoved(idx);
  }

  void addMember(SnowballStateMember member) {
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
  }

  <E extends SnowballStateMember> void addMember(E member, List<E> li) {
    if (li.contains(Objects.requireNonNull(member))) {
      throw new IllegalStateException();
    }
    if (member.getState() != this) {
      throw new IllegalArgumentException("Attempted to add member belonging to other state.");
    }
    li.add(member);
    fireUpdated(member);
  }

  public Author getAuthorFromStrings(String firstName, String lastName) {
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
  }

  public Journal getJournalFromString(String name) {
    for (Journal j : journals) {
      if (j.getName().equals(name)) {
        return j;
      }
    }
    Journal j = new Journal(this);
    j.setName(name);
    return j;
  }

  public Journal getJournalFromIssn(String issn) {
    for (Journal j : journals) {
      if (j.getIssn().equals(issn)) {
        return j;
      }
    }
    Journal j = new Journal(this);
    j.setIssn(issn);
    return j;
  }

  public List<Article> getArticles() {
    return Collections.unmodifiableList(articles);
  }

  public List<Author> getAuthors() {
    return Collections.unmodifiableList(authors);
  }

  public List<Journal> getJournals() {
    return Collections.unmodifiableList(journals);
  }

  public List<Tag> getTags() {
    return Collections.unmodifiableList(tags);
  }

  public ListModel<Article> getArticleListModel() {
    return articleListModel;
  }

  public ListModel<Author> getAuthorListModel() {
    return authorListModel;
  }

  public ListModel<Journal> getJournalListModel() {
    return journalListModel;
  }

  public ListModel<Tag> getTagListModel() {
    return tagListModel;
  }

  private Object writeReplace() {
    saved = true;
    return new SerializationProxy(this);
  }

  private void readObject(ObjectInputStream ois) throws InvalidObjectException {
    throw new InvalidObjectException("Use of serialization proxy required.");
  }

  private static class SerializationProxy implements Serializable {
    static final long serialVersionUID = 4303753722544919601L;
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

    private Object readResolve() throws InvalidObjectException {
      return new SnowballState(this);
    }
  }
}
