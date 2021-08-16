package se.dansarie.jsnowball.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.text.JTextComponent;

import se.dansarie.jsnowball.model.Article;
import se.dansarie.jsnowball.model.Author;
import se.dansarie.jsnowball.model.Journal;
import se.dansarie.jsnowball.model.SnowballState;
import se.dansarie.jsnowball.model.Tag;

public class ArticlePanel extends SnowballMemberPanel<Article> implements ListDataListener {

  private JTextField title = new JTextField();
  private JList<Author> authors =  new JList<>();
  private JComboBox<String> journal = new JComboBox<>();
  private JTextField doi = new JTextField();
  private JTextField year = new JTextField();
  private JTextField month = new JTextField();
  private JTextField volume = new JTextField();
  private JTextField issue = new JTextField();
  private JTextField pages = new JTextField();
  private JTextArea notes = new JTextArea();
  private JList<Tag> tags = new JList<>();
  private JList<Article> references = new JList<>();
  private JList<Article> referencedBy = new JList<>();
  private JButton deleteButton = new JButton("Remove article");
  private JButton importReferencesButton = new JButton("Import references");

  private ActionListener journalListener = new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent ev) {
      int idx = journal.getSelectedIndex();
      if (idx < 0) {
        return;
      }
      Article a = getItem();
      if (idx == 0) {
        a.setJournal(null);
      }
      Journal j = a.getState().getJournalList().get(idx - 1);
      a.setJournal(j);
    }
  };

  private AddRemovePreferencesListener<Author> authorsListener =
      new AddRemovePreferencesListener<>(authors, "author",
        () -> getItem().getState().getAuthorList(),
        () -> getItem().getAuthors(),
        () -> authors.getSelectedValue(),
        au -> getItem().addAuthor(au),
        au -> getItem().removeAuthor(au));

  private AddRemovePreferencesListener<Article> referencesListener =
      new AddRemovePreferencesListener<Article>(references, "reference",
        () -> getItem().getState().getArticleList(),
        () -> getItem().getState().getReferenceList(getItem()),
        () -> references.getSelectedValue(),
        ref -> getItem().getState().addReference(getItem(), ref),
        ref -> getItem().getState().removeReference(getItem(), ref));

  private AddRemovePreferencesListener<Tag> tagsListener =
      new AddRemovePreferencesListener<>(tags, "tag",
        () -> getItem().getState().getTagList(),
        () -> getItem().getState().getTagList(getItem()),
        () -> tags.getSelectedValue(),
        tag -> getItem().getState().addTag(getItem(), tag),
        tag -> getItem().getState().removeTag(getItem(), tag));

  public ArticlePanel() {
    JScrollPane authorScrollPane = new JScrollPane(authors);
    JScrollPane notesScrollPane = new JScrollPane(notes);
    JScrollPane tagsScrollPane = new JScrollPane(tags);
    JScrollPane referencesScrollPane = new JScrollPane(references);
    JScrollPane referencedByScrollPane = new JScrollPane(referencedBy);
    addComponent("Title", title);
    addComponent("Authors", authorScrollPane);
    addComponent("Journal", journal);
    addComponent("DOI", doi);
    addComponent("Year", year);
    addComponent("Month", month);
    addComponent("Volume", volume);
    addComponent("Issue", issue);
    addComponent("Pages", pages);
    addComponent("Notes", notesScrollPane);
    addComponent("Tags", tagsScrollPane);
    addComponent("References", referencesScrollPane);
    addComponent("Referenced by", referencedByScrollPane);
    addComponent("", deleteButton);
    addComponent("", importReferencesButton);
    disableComponents();

    journal.addActionListener(journalListener);
    authors.addMouseListener(authorsListener);
    tags.addMouseListener(tagsListener);
    references.addMouseListener(referencesListener);
    deleteButton.addActionListener(ev -> {
      if (JOptionPane.showConfirmDialog(deleteButton, "Do you really wish to delete this article?",
          "Delete article", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) ==
              JOptionPane.YES_OPTION) {
            Article art = getItem();
            setItem(null);
            art.getState().removeArticle(art);
          }
    });
    importReferencesButton.addActionListener(ev -> getItem().importReferences());
  }

  @Override
  public void update() {
    Article a = getItem();
    if (a == null) {
      disableComponents();
      title.setText("");
      authors.setModel(new DefaultListModel<>());
      updateJournals();
      doi.setText("");
      year.setText("");
      month.setText("");
      volume.setText("");
      issue.setText("");
      pages.setText("");
      notes.setText("");
      tags.setModel(new DefaultListModel<>());
      references.setModel(new DefaultListModel<>());
      referencedBy.setModel(new DefaultListModel<>());
      return;
    }
    title.setText(a.getTitle());
    authors.setModel(a.getAuthorsListModel());
    updateJournals();
    doi.setText(a.getDoi());
    year.setText(a.getYear());
    month.setText(a.getMonth());
    volume.setText(a.getVolume());
    issue.setText(a.getIssue());
    pages.setText(a.getPages());
    notes.setText(a.getNotes());
    tags.setModel(a.getState().getTagListModel(a));
    references.setModel(a.getState().getReferenceListModel(a));
    referencedBy.setModel(a.getState().getReferencedByListModel(a));
    enableComponents();
  }

  @Override
  protected Consumer<String> textComponentUpdated(JTextComponent comp) {
    Article a = getItem();
    if (comp == title) {
      return s -> a.setTitle(s);
    } else if (comp == doi) {
      return s -> a.setDoi(s);
    } else if (comp == year) {
      return s -> a.setYear(s);
    } else if (comp == month) {
      return s -> a.setMonth(s);
    } else if (comp == volume) {
      return s -> a.setVolume(s);
    } else if (comp == issue) {
      return s -> a.setIssue(s);
    } else if (comp == pages) {
      return s -> a.setPages(s);
    } else if (comp == notes) {
      return s -> a.setNotes(s);
    }
    throw new IllegalArgumentException("Component not recognized: " + comp.toString());
  }

  @Override
  public void contentsChanged(ListDataEvent ev) {
    updateJournals();
  }

  @Override
  public void intervalAdded(ListDataEvent ev) {
    updateJournals();
  }

  @Override
  public void intervalRemoved(ListDataEvent ev) {
    updateJournals();
  }

  private void updateJournals() {
    Article a = getItem();
    if (a == null) {
      return;
    }
    SnowballState s = a.getState();
    journal.removeActionListener(journalListener);
    journal.removeAllItems();
    journal.addItem("<No journal>");
    for (Journal j : s.getJournalList()) {
      String name = j.getName();
      journal.addItem(name);
      if (j == a.getJournal()) {
        journal.setSelectedItem(name);
      }
    }
    if (a.getJournal() == null) {
      journal.setSelectedIndex(0);
    }
    journal.addActionListener(journalListener);
  }

  private class AddRemovePreferencesListener<E> extends MouseAdapter {
    private JComponent parent;
    private String propertyName;
    private Supplier<List<E>> propertyListSupplier;
    private Supplier<List<E>> existingPropertiesSupplier;
    private Supplier<E> selectedPropertySupplier;
    private Consumer<E> newPropertyConsumer;
    private Consumer<E> removePropertyConsumer;

    private AddRemovePreferencesListener(JComponent parent, String propertyName,
        Supplier<List<E>> propertyListSupplier,
        Supplier<List<E>> existingPropertiesSupplier,
        Supplier<E> selectedPropertySupplier,
        Consumer<E> newPropertyConsumer,
        Consumer<E> removePropertyConsumer) {
      this.parent = Objects.requireNonNull(parent);
      this.propertyName = Objects.requireNonNull(propertyName);
      this.propertyListSupplier = Objects.requireNonNull(propertyListSupplier);
      this.existingPropertiesSupplier = Objects.requireNonNull(existingPropertiesSupplier);
      this.selectedPropertySupplier = Objects.requireNonNull(selectedPropertySupplier);
      this.newPropertyConsumer = Objects.requireNonNull(newPropertyConsumer);
      this.removePropertyConsumer = Objects.requireNonNull(removePropertyConsumer);
    }

    @Override
    public void mouseClicked(MouseEvent ev) {
      Article art = getItem();
      if (art == null || !SwingUtilities.isRightMouseButton(ev)) {
        return;
      }

      JMenuItem addPropertyItem = new JMenuItem("Add " + propertyName + "...");
      ArrayList<E> newProperties = new ArrayList<>(propertyListSupplier.get());
      newProperties.removeAll(existingPropertiesSupplier.get());
      if (newProperties.size() == 0) {
        addPropertyItem.setEnabled(false);
      } else {
        addPropertyItem.addActionListener(ae -> {
          @SuppressWarnings("unchecked")
          E pr = (E)JOptionPane.showInputDialog(parent, "Select the " + propertyName + " to add.",
              "Add " + propertyName, JOptionPane.QUESTION_MESSAGE, null, newProperties.toArray(),
              newProperties.get(0));
          if (pr != null) {
            newPropertyConsumer.accept(pr);
          }
        });
      }

      JMenuItem removePropertyItem = new JMenuItem("Remove " + propertyName);
      E selectedProperty = selectedPropertySupplier.get();
      if (selectedProperty == null) {
        removePropertyItem.setEnabled(false);
      } else {
        removePropertyItem.addActionListener(ae -> removePropertyConsumer.accept(selectedProperty));
      }

      JPopupMenu popup = new JPopupMenu();
      popup.add(addPropertyItem);
      popup.add(removePropertyItem);
      popup.setLocation(ev.getXOnScreen(), ev.getYOnScreen());
      popup.show(parent, ev.getX(), ev.getY());
    }
  }
}
