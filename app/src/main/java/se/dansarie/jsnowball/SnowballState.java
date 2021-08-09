package se.dansarie.jsnowball;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.List;
import java.util.Set;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class SnowballState {
  private List<Article> articles = new ArrayList<>();
  private List<Author> authors = new ArrayList<>();
  private List<Journal> journals = new ArrayList<>();

  private SnowballListModel articleListModel = new SnowballListModel() {
    @Override
    public String getElementAt(int idx) {
      return articles.get(idx).getTitle();
    }

    @Override
    public int getSize() {
      return articles.size();
    }
  };

  private SnowballListModel authorListModel = new SnowballListModel() {
    @Override
    public String getElementAt(int idx) {
      Author a = authors.get(idx);
      return a.getLastName() + ", " + a.getFirstName();
    }

    @Override
    public int getSize() {
      return authors.size();
    }
  };

  private SnowballListModel journalListModel = new SnowballListModel() {
    @Override
    public String getElementAt(int idx) {
      return journals.get(idx).getName();
    }

    @Override
    public int getSize() {
      return journals.size();
    }
  };

  public void articleFromDoi(String doi) {

  }

  void addmember(SnowballStateMember m) {
    if (m instanceof Article) {
      articles.add((Article)m);
      Collections.sort(articles);
      articleListModel.fireAdded(articles.indexOf(m));
    } else if (m instanceof Author) {
      authors.add((Author)m);
      Collections.sort(authors);
      authorListModel.fireAdded(authors.indexOf(m));
    } else if (m instanceof Journal) {
      journals.add((Journal)m);
      Collections.sort(journals);
      journalListModel.fireAdded(journals.indexOf(m));
    } else {
      throw new IllegalArgumentException();
    }
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

  public List<Article> getArticleList() {
    return Collections.unmodifiableList(articles);
  }

  public List<Author> getAuthorList() {
    return Collections.unmodifiableList(authors);
  }

  public List<Journal> getJournalList() {
    return Collections.unmodifiableList(journals);
  }

  public ListModel<String> getArticleListModel() {
    return articleListModel;
  }

  public ListModel<String> getAuthorListModel() {
    return authorListModel;
  }

  public ListModel<String> getJournalListModel() {
    return journalListModel;
  }

  private abstract class SnowballListModel implements ListModel<String> {
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
  }
}
