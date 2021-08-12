package se.dansarie.jsnowball;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.function.Consumer;

import javax.swing.JComboBox;
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
  private JList<Article> references = new JList<>();
  private JList<Article> referencedBy = new JList<>();

  private ActionListener journalListener = new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent ev) {
      int idx = journal.getSelectedIndex();
      if (idx < 0) {
        return;
      }
      Article a = getItem();
      Journal j = a.getState().getJournalList().get(idx);
      a.setJournal(j);
    }
  };

  private MouseListener authorsListener = new MouseAdapter() {
    @Override
    public void mouseClicked(MouseEvent ev) {
      Article art = getItem();
      if (!SwingUtilities.isRightMouseButton(ev) || art == null) {
        return;
      }

      JMenuItem addAuthorItem = new JMenuItem("Add author...");
      ArrayList<Author> newAuthors = new ArrayList<>(art.getState().getAuthorList());
      newAuthors.removeAll(art.getAuthors());
      if (newAuthors.size() == 0) {
        addAuthorItem.setEnabled(false);
      } else {
        addAuthorItem.addActionListener(ae -> {
          Author newAuthor = (Author)JOptionPane.showInputDialog(authors,
              "Select the author to add.", "Add author", JOptionPane.QUESTION_MESSAGE, null,
              newAuthors.toArray(), newAuthors.get(0));
          if (newAuthor != null) {
            art.addAuthor(newAuthor);
          }
        });
      }

      JMenuItem removeAuthorItem = new JMenuItem("Remove author");
      Author selectedAuthor = authors.getSelectedValue();
      if (selectedAuthor == null) {
        removeAuthorItem.setEnabled(false);
      } else {
        removeAuthorItem.addActionListener(ae -> {
          art.removeAuthor(selectedAuthor);
        });
      }

      JPopupMenu popup = new JPopupMenu();
      popup.add(addAuthorItem);
      popup.add(removeAuthorItem);
      popup.setLocation(ev.getXOnScreen(), ev.getYOnScreen());
      popup.show(authors, ev.getX(), ev.getY());
    }
  };

  private MouseListener referencesListener = new MouseAdapter() {
    @Override
    public void mouseClicked(MouseEvent ev) {
      Article art = getItem();
      SnowballState state = art.getState();
      if (!SwingUtilities.isRightMouseButton(ev) || art == null) {
        return;
      }

      JMenuItem addReferenceItem = new JMenuItem("Add reference...");
      ArrayList<Article> newReferences = new ArrayList<>(state.getArticleList());
      newReferences.remove(art);
      newReferences.removeAll(state.getReferenceList(art));
      if (newReferences.size() == 0) {
        addReferenceItem.setEnabled(false);
      } else {
        addReferenceItem.addActionListener(ae -> {
          Article reference = (Article)JOptionPane.showInputDialog(references,
              "Select the reference to add.", "Add reference", JOptionPane.QUESTION_MESSAGE, null,
              newReferences.toArray(), newReferences.get(0));
          if (reference != null) {
            state.addReference(art, reference);
          }
        });
      }

      JMenuItem removeReferenceItem = new JMenuItem("Remove reference");
      Article selectedReference = references.getSelectedValue();
      if (selectedReference == null) {
        removeReferenceItem.setEnabled(false);
      } else {
        removeReferenceItem.addActionListener(ae -> {
          state.removeReference(art, selectedReference);
        });
      }

      JPopupMenu popup = new JPopupMenu();
      popup.add(addReferenceItem);
      popup.add(removeReferenceItem);
      popup.setLocation(ev.getXOnScreen(), ev.getYOnScreen());
      popup.show(references, ev.getX(), ev.getY());
    }
  };

  ArticlePanel() {
    JScrollPane authorScrollPane = new JScrollPane(authors);
    JScrollPane notesScrollPane = new JScrollPane(notes);
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
    addComponent("References", referencesScrollPane);
    addComponent("Referenced by", referencedByScrollPane);
    disableComponents();

    journal.addActionListener(journalListener);
    authors.addMouseListener(authorsListener);
    references.addMouseListener(referencesListener);
  }

  @Override
  public void update() {
    Article a = getItem();
    if (a == null) {
      disableComponents();
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
    for (Journal j : s.getJournalList()) {
      String name = j.getName();
      journal.addItem(name);
      if (j == a.getJournal()) {
        journal.setSelectedItem(name);
      }
    }
    journal.addActionListener(journalListener);
  }
}
