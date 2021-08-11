package se.dansarie.jsnowball;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.text.JTextComponent;

public class ArticlePanel extends SnowballMemberPanel<Article> implements ListDataListener {

  private JTextField title = new JTextField();
  private JList<String> authors =  new JList<>();
  private JComboBox<String> journal = new JComboBox<>();
  private JTextField doi = new JTextField();
  private JTextField year = new JTextField();
  private JTextField month = new JTextField();
  private JTextField volume = new JTextField();
  private JTextField issue = new JTextField();
  private JTextField pages = new JTextField();
  private JTextArea notes = new JTextArea();
  private JList<Article> references = new JList<>();
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

  ArticlePanel() {
    JScrollPane authorScrollPane = new JScrollPane(authors);
    JScrollPane notesScrollPane = new JScrollPane(notes);
    JScrollPane referencesScrollPane = new JScrollPane(references);
    authors.setPrototypeCellValue("AUTHOR AUTHOR AUTHOR AUTHOR");
    authors.setVisibleRowCount(6);
    Dimension dim = new Dimension(250, 100);
    authorScrollPane.setPreferredSize(dim);
    notesScrollPane.setPreferredSize(dim);
    referencesScrollPane.setPreferredSize(dim);
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
    disableComponents();
    journal.addActionListener(journalListener);
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
