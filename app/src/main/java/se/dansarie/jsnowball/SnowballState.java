package se.dansarie.jsnowball;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
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
  private List<Tag> tags = new ArrayList<>();
  private Map<Article, List<Article>> references = new HashMap<>();
  private Map<Article, List<Tag>> articleTags = new HashMap<>();
  private Map<Article, SnowballListModel<Article>> referenceListModels = new HashMap<>();
  private Map<Article, SnowballListModel<Article>> referencedByListModels = new HashMap<>();
  private Map<Article, SnowballListModel<Tag>> articleTagListModels = new HashMap<>();
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

    for (Tag tag : sp.tags) {
      addTag(tag);
    }

    for (int i = 0; i < sp.articles.length; i++) {
      for (Article reference : sp.references[i]) {
        addReference(sp.articles[i], reference);
      }
      for (Tag tag : sp.articleTags[i]) {
        addTag(sp.articles[i], tag);
      }
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

  private SnowballListModel<Tag> tagListModel = new SnowballListModel<Tag>() {
    @Override
    public Tag getElementAt(int idx) {
      return tags.get(idx);
    }

    @Override
    public int getSize() {
      return tags.size();
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

  public void removeArticle(Article art) {
    int idx = articles.indexOf(art);
    if (idx < 0) {
      return;
    }
    articles.remove(art);
    references.remove(art);
    articleTags.remove(art);
    referenceListModels.remove(art);
    referencedByListModels.remove(art);
    articleTagListModels.remove(art);
    art.setState(null);
    saved = false;
    articleListModel.fireRemoved(idx);
  }

  public void removeAuthor(Author au) {
    int idx = authors.indexOf(au);
    if (idx < 0) {
      return;
    }
    for (Article art : new ArrayList<>(articles)) {
      if (art.getAuthors().contains(au)) {
        art.removeAuthor(au);
      }
    }
    authors.remove(idx);
    au.setState(null);
    saved = false;
    authorListModel.fireRemoved(idx);
  }

  public void removeJournal(Journal jo) {
    int idx = journals.indexOf(jo);
    if (idx < 0) {
      return;
    }
    for (Article art : new ArrayList<>(articles)) {
      if (art.getJournal() == jo) {
        art.setJournal(null);
      }
    }
    journals.remove(idx);
    jo.setState(null);
    saved = false;
    journalListModel.fireRemoved(idx);
  }

  public void removeTag(Tag tag) {
    int idx = tags.indexOf(tag);
    if (idx < 0) {
      return;
    }
    for (Article art : articles) {
      int tagidx = articleTags.get(art).indexOf(tag);
      if (tagidx >= 0) {
        articleTags.get(art).remove(tagidx);
        articleTagListModels.get(art).fireRemoved(tagidx);
      }
    }
    tags.remove(idx);
    tag.setState(null);
    saved = false;
    tagListModel.fireRemoved(idx);
  }

  private void addArticle(Article art) {
    if (articles.contains(art)) {
      return;
    }
    for (Author au : art.getAuthors()) {
      if (!authors.contains(au)) {
        throw new IllegalStateException("Invariant violated: unknown article author.");
      }
    }
    art.setState(this);
    articles.add(art);
    Collections.sort(articles);
    references.put(art, new ArrayList<>());
    articleTags.put(art, new ArrayList<>());
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
    referencedByListModels.put(art, new SnowballListModel<Article>() {

      @Override
      public Article getElementAt(int idx) {
        return getReferencedByList(art).get(idx);
      }

      @Override
      public int getSize() {
        return getReferencedByList(art).size();
      }
    });
    articleTagListModels.put(art, new SnowballListModel<Tag>() {
      @Override
      public Tag getElementAt(int idx) {
        return articleTags.get(art).get(idx);
      }

      @Override
      public int getSize() {
        return articleTags.get(art).size();
      }
    });
    int idx = articles.indexOf(art);
    articleListModel.fireChanged(idx, idx);
  }

  public void mergeAuthors(Author a1, Author a2) {
    if (Objects.requireNonNull(a1) == Objects.requireNonNull(a2) ||
        !authors.contains(a1) || !authors.contains(a2)) {
      throw new IllegalArgumentException();
    }
    for (Article art : getArticleList(a2)) {
      art.addAuthor(a1);
      art.removeAuthor(a2);
    }
    removeAuthor(a2);
  }

  public void mergeJournals(Journal a1, Journal a2) {
    if (Objects.requireNonNull(a1) == Objects.requireNonNull(a2) ||
        !journals.contains(a1) || !journals.contains(a2)) {
      throw new IllegalArgumentException();
    }
    for (Article art : new ArrayList<>(articles)) {
      if (art.getJournal() == a2) {
        art.setJournal(a1);
      }
    }
    removeJournal(a2);
  }

  public void mergeTags(Tag t1, Tag t2) {
    if (Objects.requireNonNull(t1) == Objects.requireNonNull(t2) ||
        !tags.contains(t1) || !tags.contains(t2)) {
      throw new IllegalArgumentException();
    }
    for (Article art : new ArrayList<>(articles)) {
      if (getTagList(art).contains(t2)) {
        removeTag(art, t2);
        if (!getTagList(art).contains(t1)) {
          addTag(art, t1);
        }
      }
    }
    removeTag(t2);
  }

  public Author createAuthor() {
    Author au = new Author();
    addAuthor(au);
    return au;
  }

  private void addAuthor(Author au) {
    if (authors.contains(au)) {
      return;
    }
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
    if (journals.contains(jo)) {
      return;
    }
    jo.setState(this);
    journals.add(jo);
    Collections.sort(journals);
    journalListModel.fireAdded(journals.indexOf(jo));
  }

  public Tag createTag() {
    Tag ta = new Tag();
    addTag(ta);
    return ta;
  }

  private void addTag(Tag ta) {
    ta.setState(this);
    tags.add(ta);
    Collections.sort(tags);
    tagListModel.fireAdded(tags.indexOf(ta));
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

  public List<Article> getArticleList(Author au) {
    ArrayList<Article> ret = new ArrayList<>();
    for (Article art : articles) {
      if (art.getAuthors().contains(au)) {
        ret.add(art);
      }
    }
    return Collections.unmodifiableList(ret);
  }

  public List<Author> getAuthorList() {
    return Collections.unmodifiableList(authors);
  }

  public List<Journal> getJournalList() {
    return Collections.unmodifiableList(journals);
  }

  public List<Tag> getTagList() {
    return Collections.unmodifiableList(tags);
  }

  public List<Tag> getTagList(Article art) {
    return Collections.unmodifiableList(articleTags.get(art));
  }

  public List<Article> getReferenceList(Article art) {
    return Collections.unmodifiableList(references.get(art));
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

  public ListModel<Tag> getTagListModel(Article art) {
    return articleTagListModels.get(art);
  }

  public void addTag(Article art, Tag tag) {
    List<Tag> tags = articleTags.get(art);
    if (!tags.contains(tag)) {
      tags.add(Objects.requireNonNull(tag));
      Collections.sort(tags);
      articleTagListModels.get(art).fireAdded(tags.indexOf(tag));
      updated(art);
    }
  }

  public void removeTag(Article art, Tag tag) {
    List<Tag> tags = articleTags.get(art);
    int idx = tags.indexOf(tag);
    if (idx >= 0) {
      tags.remove(idx);
      tag.setState(null);
      articleTagListModels.get(art).fireRemoved(idx);
    }
  }

  public ListModel<Article> getReferenceListModel(Article art) {
    return referenceListModels.get(art);
  }

  public ListModel<Article> getReferencedByListModel(Article art) {
    return referencedByListModels.get(art);
  }

  public List<Article> getReferencedByList(Article art) {
    ArrayList<Article> referencing = new ArrayList<>();
    for (Article a : articles) {
      if (references.get(a).contains(art)) {
        referencing.add(a);
      }
    }
    return referencing;
  }

  public void addReference(Article art, Article reference) {
    if (Objects.requireNonNull(art) == Objects.requireNonNull(reference)) {
      throw new IllegalArgumentException();
    }
    List<Article> refs = references.get(art);
    if (!refs.contains(reference)) {
      refs.add(reference);
      Collections.sort(refs);
      referenceListModels.get(art).fireAdded(refs.indexOf(reference));
    }
  }

  public void removeReference(Article art, Article reference) {
    if (Objects.requireNonNull(art) == Objects.requireNonNull(reference)) {
      throw new IllegalArgumentException();
    }
    List<Article> refs = references.get(art);
    int idx = refs.indexOf(reference);
    if (idx >= 0) {
      refs.remove(idx);
      referenceListModels.get(art).fireRemoved(idx);
    }
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

  void updated(Tag ta) {
    int idx = tags.indexOf(ta);
    if (idx >= 0) {
      saved = false;
      Collections.sort(tags);
      tagListModel.fireChanged(0, journals.size() - 1);
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

    private void fireRemoved(int idx) {
      ListDataEvent ev = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, idx, idx);
      for (ListDataListener li : listeners) {
        li.intervalRemoved(ev);
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
    private Tag[] tags;
    private Article[][] references;
    private Tag[][] articleTags;

    private SerializationProxy(SnowballState st) {
      articles = st.articles.toArray(new Article[st.articles.size()]);
      authors = st.authors.toArray(new Author[st.authors.size()]);
      journals = st.journals.toArray(new Journal[st.journals.size()]);
      tags = st.tags.toArray(new Tag[st.tags.size()]);
      references = new Article[articles.length][];
      articleTags = new Tag[articles.length][];
      for (int i = 0; i < articles.length; i++) {
        List<Article> refs = st.getReferenceList(articles[i]);
        List<Tag> tags = st.getTagList(articles[i]);
        references[i] = new Article[refs.size()];
        articleTags[i] = new Tag[tags.size()];
        for (int j = 0; j < refs.size(); j++) {
          references[i][j] = refs.get(j);
        }
        for (int j = 0; j < tags.size(); j++) {
          articleTags[i][j] = tags.get(j);
        }
      }
    }

    private Object readResolve() throws InvalidObjectException {
      return new SnowballState(this);
    }
  }
}
