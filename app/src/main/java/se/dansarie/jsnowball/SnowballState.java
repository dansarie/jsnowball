package se.dansarie.jsnowball;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.List;
import java.util.Set;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class SnowballState implements Serializable {
  private List<Article> articles = new ArrayList<>();
  private List<Author> authors = new ArrayList<>();
  private List<Journal> journals = new ArrayList<>();
  private Map<Article, List<Article>> references = new HashMap<>();
  private Map<Article, SnowballListModel<Article>> referenceListModels = new HashMap<>();
  private boolean saved = true;

  public SnowballState() {
  }

  private SnowballState(SerializationProxy sp) throws InvalidObjectException {
    for (Journal jo : sp.journals) {
      addJournal(jo);
    }

    for (Author au : sp.authors) {
      addAuthor(au);
    }

    for (Article art : sp.articles) {
      addArticle(art);
    }
  }

  private SnowballListModel<Article> articleListModel = new SnowballListModel<Article>() {
    @Override
    public Article getElementAt(int idx) {
      return articles.get(idx);
    }

    @Override
    public int getSize() {
      return articles.size();
    }
  };

  private SnowballListModel<Author> authorListModel = new SnowballListModel<Author>() {
    @Override
    public Author getElementAt(int idx) {
      return authors.get(idx);
    }

    @Override
    public int getSize() {
      return authors.size();
    }
  };

  private SnowballListModel<Journal> journalListModel = new SnowballListModel<Journal>() {
    @Override
    public Journal getElementAt(int idx) {
      return journals.get(idx);
    }

    @Override
    public int getSize() {
      return journals.size();
    }
  };

  public boolean isSaved() {
    return saved;
  }

  public Article createArticle() {
    Article art = new Article();
    addArticle(art);
    return art;
  }

  private void addArticle(Article art) {
    for (Author au : art.getAuthors()) {
      if (!authors.contains(au)) {
        throw new IllegalStateException("Invariant violated: unknown article author.");
      }
    }
    art.setState(this);
    articles.add(art);
    Collections.sort(articles);
    references.put(art, new ArrayList<>());
    referenceListModels.put(art, new SnowballListModel<Article>() {
      @Override
      public Article getElementAt(int idx) {
        return references.get(art).get(idx);
      }

      @Override
      public int getSize() {
        return references.get(art).size();
      }
    });
    articleListModel.fireAdded(articles.indexOf(art));
  }

  public Author createAuthor() {
    Author au = new Author();
    addAuthor(au);
    return au;
  }

  private void addAuthor(Author au) {
    au.setState(this);
    authors.add(au);
    Collections.sort(authors);
    authorListModel.fireAdded(authors.indexOf(au));
  }

  public Journal createJournal() {
    Journal jo = new Journal();
    addJournal(jo);
    return jo;
  }

  private void addJournal(Journal jo) {
    jo.setState(this);
    journals.add(jo);
    Collections.sort(journals);
    journalListModel.fireAdded(journals.indexOf(jo));
  }

  public Author getAuthorFromStrings(String firstName, String lastName) {
    for (Author a : authors) {
      if (a.getFirstName().equalsIgnoreCase(firstName)
          && a.getLastName().equalsIgnoreCase(lastName)) {
        return a;
      }
    }
    Author a = createAuthor();
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
    Journal j = createJournal();
    j.setName(name);
    return j;
  }

  public Journal getJournalFromIssn(String issn) {
    for (Journal j : journals) {
      if (j.getIssn().equals(issn)) {
        return j;
      }
    }
    Journal j = createJournal();
    j.setIssn(issn);
    return j;
  }

  public List<Article> getArticleList() {
    return Collections.unmodifiableList(articles);
  }

  public List<Author> getAuthorList() {
    return Collections.unmodifiableList(authors);
  }

  public List<Journal> getJournalList() {
    return Collections.unmodifiableList(journals);
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

  public ListModel<Article> getReferenceListModel(Article art) {
    return referenceListModels.get(art);
  }

  void updated(Article art) {
    int idx = articles.indexOf(art);
    if (idx >= 0) {
      saved = false;
      Collections.sort(articles);
      articleListModel.fireChanged(0, articles.size() - 1);
    }
  }

  void updated(Author au) {
    int idx = authors.indexOf(au);
    if (idx >= 0) {
      saved = false;
      Collections.sort(authors);
      authorListModel.fireChanged(0, authors.size() - 1);
    }
  }

  void updated(Journal jo) {
    int idx = journals.indexOf(jo);
    if (idx >= 0) {
      saved = false;
      Collections.sort(journals);
      journalListModel.fireChanged(0, journals.size() - 1);
    }
  }

  private abstract class SnowballListModel<E> implements ListModel<E> {
    private Set<ListDataListener> listeners = new HashSet<>();

    @Override
    public void addListDataListener(ListDataListener li) {
      listeners.add(Objects.requireNonNull(li));
    }

    @Override
    public void removeListDataListener(ListDataListener li) {
      listeners.remove(li);
    }

    private void fireAdded(int idx) {
      ListDataEvent ev = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, idx, idx);
      for (ListDataListener li : listeners) {
        li.intervalAdded(ev);
      }
    }

    private void fireChanged(int lo, int hi) {
      ListDataEvent ev = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, lo, hi);
      for (ListDataListener li : listeners) {
        li.contentsChanged(ev);
      }
    }
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
    private Article[] articles;
    private Author[] authors;
    private Journal[] journals;

    private SerializationProxy(SnowballState st) {
      articles = st.articles.toArray(new Article[st.articles.size()]);
      authors = st.authors.toArray(new Author[st.authors.size()]);
      journals = st.journals.toArray(new Journal[st.journals.size()]);
    }

    private Object readResolve() throws InvalidObjectException {
      return new SnowballState(this);
    }
  }
}
