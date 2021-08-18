package se.dansarie.jsnowball.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.List;

import javax.swing.ListModel;
import javax.swing.SwingUtilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SnowballState {
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

  private SnowballState(SerializationProxy sp) {
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

  public static SnowballState fromJson(String json) throws JSONException {
    return new SnowballState(new SnowballState.SerializationProxy(new JSONObject(json)));
  }

  synchronized void fireUpdated(SnowballStateMember updated) {
    Objects.requireNonNull(updated);
    saved = false;
    List<? extends SnowballStateMember> list = null;
    SnowballListModel<? extends SnowballStateMember> listModel = null;
    if (updated instanceof Article) {
      list = articles;
      listModel = articleListModel;
    } else if (updated instanceof Author) {
      list = authors;
      listModel = authorListModel;
    } else if (updated instanceof Journal) {
      list = journals;
      listModel = journalListModel;
    } else if (updated instanceof Tag) {
      list = tags;
      listModel = tagListModel;
    } else {
      throw new IllegalStateException();
    }
    Collections.sort(list);
    final SnowballListModel<SnowballStateMember> lm =
        (SnowballListModel<SnowballStateMember>)listModel;
    SwingUtilities.invokeLater(() -> lm.fireChanged(updated));
  }

  public synchronized boolean isSaved() {
    return saved;
  }

  synchronized void removeMember(SnowballStateMember member) {
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
    final int idx = list.indexOf(member);
    list.remove(idx);
    final SnowballListModel<? extends SnowballStateMember> lm = listModel;
    SwingUtilities.invokeLater(() -> lm.fireRemoved(idx));
  }

  synchronized void addMember(SnowballStateMember member) {
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

  synchronized <E extends SnowballStateMember> void addMember(E member, List<E> li) {
    if (li.contains(Objects.requireNonNull(member))) {
      throw new IllegalStateException();
    }
    if (member.getState() != this) {
      throw new IllegalArgumentException("Attempted to add member belonging to other state.");
    }
    li.add(member);
    fireUpdated(member);
  }

  synchronized public Author getAuthorFromStrings(String firstName, String lastName) {
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

  synchronized public Journal getJournalFromString(String name) {
    for (Journal j : journals) {
      if (j.getName().equals(name)) {
        return j;
      }
    }
    Journal j = new Journal(this);
    j.setName(name);
    return j;
  }

  synchronized public Journal getJournalFromIssn(String issn) {
    for (Journal j : journals) {
      if (j.getIssn().equals(issn)) {
        return j;
      }
    }
    Journal j = new Journal(this);
    j.setIssn(issn);
    return j;
  }

  synchronized public List<Article> getArticles() {
    return Collections.unmodifiableList(new ArrayList<>(articles));
  }

  synchronized public List<Author> getAuthors() {
    return Collections.unmodifiableList(new ArrayList<>(authors));
  }

  synchronized public List<Journal> getJournals() {
    return Collections.unmodifiableList(new ArrayList<>(journals));
  }

  synchronized public List<Tag> getTags() {
    return Collections.unmodifiableList(new ArrayList<>(tags));
  }

  synchronized public ListModel<Article> getArticleListModel() {
    return articleListModel;
  }

  synchronized public ListModel<Author> getAuthorListModel() {
    return authorListModel;
  }

  synchronized public ListModel<Journal> getJournalListModel() {
    return journalListModel;
  }

  synchronized public ListModel<Tag> getTagListModel() {
    return tagListModel;
  }

  public SerializationProxy getSerializationProxy() {
    return new SerializationProxy(this);
  }

  public static class SerializationProxy {
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

    SerializationProxy(JSONObject json) throws JSONException {
      if (!json.getString("version").equals("1.0")) {
        throw new RuntimeException();
      }
      JSONArray ar = json.getJSONArray("articles");
      articles = new Article.SerializationProxy[ar.length()];
      for (int i = 0; i < ar.length(); i++) {
        articles[i] = new Article.SerializationProxy(ar.getJSONObject(i));
      }

      JSONArray au = json.getJSONArray("authors");
      authors = new Author.SerializationProxy[au.length()];
      for (int i = 0; i < au.length(); i++) {
        authors[i] = new Author.SerializationProxy(au.getJSONObject(i));
      }

      JSONArray jo = json.getJSONArray("journals");
      journals = new Journal.SerializationProxy[jo.length()];
      for (int i = 0; i < jo.length(); i++) {
        journals[i] = new Journal.SerializationProxy(jo.getJSONObject(i));
      }

      JSONArray ta = json.getJSONArray("tags");
      tags = new Tag.SerializationProxy[ta.length()];
      for (int i = 0; i < ta.length(); i++) {
        tags[i] = new Tag.SerializationProxy(ta.getJSONObject(i));
      }
    }

    public String toJson() {
      JSONArray articleArray = new JSONArray();
      for (Article.SerializationProxy a : articles) {
        articleArray.put(a.toJson());
      }

      JSONArray authorArray = new JSONArray();
      for (Author.SerializationProxy a : authors) {
        authorArray.put(a.toJson());
      }

      JSONArray journalArray = new JSONArray();
      for (Journal.SerializationProxy j : journals) {
        journalArray.put(j.toJson());
      }

      JSONArray tagArray = new JSONArray();
      for (Tag.SerializationProxy t : tags) {
        tagArray.put(t.toJson());
      }

      JSONObject json = new JSONObject();
      json.put("version", "1.0");
      json.put("articles", articleArray);
      json.put("authors", authorArray);
      json.put("journals", journalArray);
      json.put("tags", tagArray);
      return json.toString(2);
    }
  }
}
