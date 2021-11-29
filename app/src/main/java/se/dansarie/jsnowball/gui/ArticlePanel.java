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

package se.dansarie.jsnowball.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.text.JTextComponent;

import se.dansarie.jsnowball.model.Article;
import se.dansarie.jsnowball.model.Author;
import se.dansarie.jsnowball.model.CrossRef;
import se.dansarie.jsnowball.model.Journal;
import se.dansarie.jsnowball.model.SnowballState;
import se.dansarie.jsnowball.model.Tag;
import se.dansarie.jsnowball.model.Article.ArticleStatus;

public class ArticlePanel extends SnowballMemberPanel<Article> implements ListDataListener {

  private ButtonGroup buttonGroup = new ButtonGroup();
  private JTextField title = new JTextField();
  private JList<Author> authors =  new JList<>();
  private JComboBox<String> journal = new JComboBox<>();
  private JRadioButton includedButton = new JRadioButton("Included");
  private JRadioButton excludedButton = new JRadioButton("Excluded");
  private JRadioButton undecidedButton = new JRadioButton("Undecided", true);
  private JTextField doi = new JTextField();
  private JTextField year = new JTextField();
  private JTextField month = new JTextField();
  private JTextField volume = new JTextField();
  private JTextField issue = new JTextField();
  private JTextField pages = new JTextField();
  private JTextArea notes = new JTextArea();
  private JCheckBox startSet = new JCheckBox("In starting set");
  private JList<Tag> tags = new JList<>();
  private JList<Article> references = new JList<>();
  private JList<Article> referencedBy = new JList<>();

  private Action deleteAction = new AbstractAction("Remove article") {
    @Override
    public void actionPerformed(ActionEvent ev) {
      if (JOptionPane.showConfirmDialog(deleteButton, "Do you really wish to delete this article?",
        "Delete article", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) ==
            JOptionPane.YES_OPTION) {
          Article art = getItem();
          setItem(null);
          art.remove();
        }
      }
  };
  private JButton deleteButton = new JButton(deleteAction);

  private Action importReferencesAction = new AbstractAction("Import references") {
    @Override
    public void actionPerformed(ActionEvent ev) {
      String doi = getItem().getDoi();
      if (doi == null || doi.trim().length() == 0) {
        JOptionPane.showMessageDialog(ArticlePanel.this, "Importing references requires a DOI.",
            "Import references", JOptionPane.ERROR_MESSAGE);
        return;
      }

      ProgressMonitor pm = new ProgressMonitor(ArticlePanel.this, "Retrieving references",
          "Retrieving references list.", 0, 1);

      pm.setMillisToPopup(500);
      pm.setMillisToDecideToPopup(100);
      Article article = getItem();

      SwingWorker<Void, Void> w = new SwingWorker<>() {
        @Override
        protected Void doInBackground() {
          CrossRef cr;
          try {
            cr = CrossRef.getDoi(doi);
          } catch (IOException ex) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(ArticlePanel.this,
                "Error when retrieving article metadata.", "Import references",
                JOptionPane.ERROR_MESSAGE));
            return null;
          }
          int i = 1;
          pm.setMaximum(cr.references.size());
            for (CrossRef.Reference ref : cr.references) {
              StringBuilder note = new StringBuilder("Retrieving");
              if (ref.title != null) {
                note.append(" \"");
                note.append(ref.title);
                note.append("\"");
              } else if (ref.doi != null) {
                note.append(" ");
                note.append(ref.doi);
              }
              note.append("...");
              pm.setNote(note.toString());
              try {
                CrossRef.addCrossRefReference(article, ref);
              } catch (IOException | RuntimeException ex) {
                LogWindow.getInstance().addLogData(ex.toString());
              }
              pm.setProgress(i++);
            }
          return null;
        }

        @Override
        protected void done() {
          pm.close();
        }
      };
      w.execute();
    }
  };
  private JButton importReferencesButton = new JButton(importReferencesAction);

  private Action mergeArticlesAction = new AbstractAction("Merge articles") {
    @Override
    public void actionPerformed(ActionEvent ev) {
      ArrayList<Article> articles = new ArrayList<>(getItem().getState().getArticles());
      articles.remove(getItem());
      if (articles.size() == 0) {
        return;
      }
      Article art = (Article)JOptionPane.showInputDialog(ArticlePanel.this,
          "Select the article you wish to merge into this one.", "Merge articles",
          JOptionPane.QUESTION_MESSAGE, null, articles.toArray(), articles.get(0));
      if (art == null) {
        return;
      }
      getItem().merge(art);
    }
  };
  private JButton mergeButton = new JButton(mergeArticlesAction);

  ScopusReferenceImportAction scopusRefsAction = new ScopusReferenceImportAction(
        "References from Scopus CSV...", this);
    ScopusReferenceImportAction scopusRefsToAction = new ScopusReferenceImportAction(
        "References to from Scopus CSV...", this);

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
      } else {
        Journal j = a.getState().getJournals().get(idx - 1);
        a.setJournal(j);
      }
    }
  };

  private AddRemovePreferencesListener<Author> authorsListener =
      new AddRemovePreferencesListener<>(authors, "author",
        () -> getItem().getState().getAuthors(),
        () -> getItem().getAuthors(),
        () -> authors.getSelectedValue(),
        au -> getItem().addAuthor(au),
        au -> getItem().removeAuthor(au));

  private AddRemovePreferencesListener<Article> referencesListener =
      new AddRemovePreferencesListener<>(references, "reference",
        () -> getItem().getState().getArticles(),
        () -> {
          List<Article> refs = new ArrayList<>(getItem().getReferences());
          refs.add(getItem());
          return refs;
        },
        () -> references.getSelectedValue(),
        ref -> getItem().addReference(ref),
        ref -> getItem().removeReference(ref));

  private AddRemovePreferencesListener<Article> referencedByListener =
      new AddRemovePreferencesListener<>(referencedBy, "reference to",
        () -> getItem().getState().getArticles(),
        () -> {
          List<Article> refs = new ArrayList<>(getItem().getReferencesTo());
          refs.add(getItem());
          return refs;
        },
        () -> referencedBy.getSelectedValue(),
        ref -> ref.addReference(getItem()),
        ref -> ref.removeReference(getItem()));

  private AddRemovePreferencesListener<Tag> tagsListener =
      new AddRemovePreferencesListener<>(tags, "tag",
        () -> getItem().getState().getTags(),
        () -> getItem().getTags(),
        () -> tags.getSelectedValue(),
        tag -> getItem().addTag(tag),
        tag -> getItem().removeTag(tag));

  public ArticlePanel() {
    scopusRefsAction.setArticleProcessor(art -> getItem().addReference(art));
    scopusRefsToAction.setArticleProcessor(art -> art.addReference(getItem()));
    buttonGroup.add(includedButton);
    buttonGroup.add(excludedButton);
    buttonGroup.add(undecidedButton);
    JScrollPane authorScrollPane = new JScrollPane(authors);
    JScrollPane notesScrollPane = new JScrollPane(notes);
    JScrollPane tagsScrollPane = new JScrollPane(tags);
    JScrollPane referencesScrollPane = new JScrollPane(references);
    JScrollPane referencedByScrollPane = new JScrollPane(referencedBy);
    addComponent("", startSet);
    addComponent("", includedButton);
    addComponent("", excludedButton);
    addComponent("", undecidedButton);
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
    addComponent("", new JButton(scopusRefsAction));
    addComponent("", new JButton(scopusRefsToAction));
    addComponent("", mergeButton);
    disableComponents();

    journal.addActionListener(journalListener);
    authors.addMouseListener(authorsListener);
    tags.addMouseListener(tagsListener);
    references.addMouseListener(referencesListener);
    referencedBy.addMouseListener(referencedByListener);

    startSet.addActionListener(ev -> getItem().setStartSet(startSet.isSelected()));
    includedButton.addActionListener(ev -> getItem().setStatus(ArticleStatus.INCLUDED));
    excludedButton.addActionListener(ev -> getItem().setStatus(ArticleStatus.EXCLUDED));
    undecidedButton.addActionListener(ev -> getItem().setStatus(ArticleStatus.UNDECIDED));
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
    startSet.setSelected(a.inStartSet());
    tags.setModel(a.getTagListModel());
    references.setModel(a.getReferenceListModel());
    referencedBy.setModel(a.getReferencesToListModel());
    scopusRefsAction.setState(a.getState());
    scopusRefsToAction.setState(a.getState());
    switch (a.getStatus()) {
      case INCLUDED:  includedButton.setSelected(true);  break;
      case EXCLUDED:  excludedButton.setSelected(true);  break;
      case UNDECIDED: undecidedButton.setSelected(true); break;
    }
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
    for (Journal j : s.getJournals()) {
      String name = j.getName();
      journal.addItem(name);
    }
    if (a.getJournal() == null) {
      journal.setSelectedIndex(0);
    } else {
      journal.setSelectedItem(a.getJournal().getName());
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
        removePropertyItem.addActionListener(ae -> {
          if (JOptionPane.showConfirmDialog(parent, "Do you really wish to remove the "
              + propertyName, "Remove " + propertyName, JOptionPane.YES_NO_OPTION,
              JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
            removePropertyConsumer.accept(selectedProperty);
          }
        });
      }

      JPopupMenu popup = new JPopupMenu();
      popup.add(addPropertyItem);
      popup.add(removePropertyItem);
      popup.setLocation(ev.getXOnScreen(), ev.getYOnScreen());
      popup.show(parent, ev.getX(), ev.getY());
    }
  }
}
