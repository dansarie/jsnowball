package se.dansarie.jsnowball;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class JSnowball {
  public static void main(String[] args) {
    SnowballState state = new SnowballState();
    JFrame mainframe = new JFrame("JSnowball");

    JMenuItem newprojectitem = new JMenuItem("New project");
    JMenuItem saveprojectitem = new JMenuItem("Save project");
    JMenuItem exititem = new JMenuItem("Exit");
    exititem.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent ev) {
        mainframe.setVisible(false);
        mainframe.dispose();
      }
    });
    JMenu filemenu = new JMenu("File");
    filemenu.add(newprojectitem);
    filemenu.add(saveprojectitem);
    filemenu.addSeparator();
    filemenu.add(exititem);

    JMenu operationsMenu = new JMenu("Operations");
    JMenuItem addArticleDoi = new JMenuItem("Add article from DOI...");
    addArticleDoi.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent ev) {
        String doi = JOptionPane.showInputDialog(mainframe, "Enter a DOI",
            "Create article from DOI", JOptionPane.QUESTION_MESSAGE);
        if (doi == null) {
          return;
        }
        Article.fromDoi(state, doi);
      }
    });
    JMenuItem addArticleManually = new JMenuItem("New article");
    JMenuItem addAuthorItem = new JMenuItem("New author");
    JMenuItem addJournalItem = new JMenuItem("New journal");
    operationsMenu.add(addArticleManually);
    operationsMenu.add(addArticleDoi);
    operationsMenu.addSeparator();
    operationsMenu.add(addAuthorItem);
    operationsMenu.addSeparator();
    operationsMenu.add(addJournalItem);

    JMenuItem aboutitem = new JMenuItem("About JSnowball...");
    aboutitem.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent evt) {
        JOptionPane.showMessageDialog(mainframe, "JSnowball\n\nCopyright (C) 2021 Marcus Dansarie",
            "About JSnowball", JOptionPane.INFORMATION_MESSAGE);
      }
    });
    JMenu helpmenu = new JMenu("Help");
    helpmenu.add(aboutitem);
    JMenuBar menubar = new JMenuBar();
    menubar.add(filemenu);
    menubar.add(operationsMenu);
    menubar.add(helpmenu);

    mainframe.setJMenuBar(menubar);

    JList<Article> articleList = new JList<>(state.getArticleListModel());
    JList<Author> authorList = new JList<>(state.getAuthorListModel());
    JList<Journal> journalList = new JList<>(state.getJournalListModel());

    // ListSelectionWatcher<Article> articleSelectionWatcher = new ListSelectionWatcher<>(articleList);
    // articleList.addListSelectionListener(articleSelectionWatcher);
    // state.getArticleListModel().addListDataListener(articleSelectionWatcher);

    // ListSelectionWatcher<Author> authorSelectionWatcher = new ListSelectionWatcher<>(authorList);
    // authorList.addListSelectionListener(authorSelectionWatcher);
    // state.getAuthorListModel().addListDataListener(authorSelectionWatcher);

    // ListSelectionWatcher<Journal> journalSelectionWatcher = new ListSelectionWatcher<>(journalList);
    // journalList.addListSelectionListener(journalSelectionWatcher);
    // state.getJournalListModel().addListDataListener(journalSelectionWatcher);

    JTabbedPane tabbedpane = new JTabbedPane(JTabbedPane.TOP);
    tabbedpane.addTab("Articles", new JScrollPane(articleList));
    tabbedpane.addTab("Authors", new JScrollPane(authorList));
    tabbedpane.addTab("Journals", new JScrollPane(journalList));

    JSplitPane splitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    splitpane.setLeftComponent(tabbedpane);

    ArticlePanel articlePanel = new ArticlePanel();
    AuthorPanel authorPanel = new AuthorPanel();
    JournalPanel journalPanel = new JournalPanel();
    splitpane.setRightComponent(articlePanel);

    tabbedpane.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent ev) {
        switch (tabbedpane.getSelectedIndex()) {
          case 0:
            splitpane.setRightComponent(articlePanel);
            break;
          case 1:
            splitpane.setRightComponent(authorPanel);
            break;
          case 2:
            splitpane.setRightComponent(journalPanel);
            break;
          default:
            break;
        }
      }
    });

    articleList.addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent ev) {
        int idx = articleList.getSelectedIndex();
        if (idx < 0) {
          articlePanel.setItem(null);
        } else {
          articlePanel.setItem(state.getArticleList().get(idx));
        }
      }
    });

    authorList.addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent ev) {
        int idx = authorList.getSelectedIndex();
        if (idx < 0) {
          authorPanel.setItem(null);
        } else {
          authorPanel.setItem(state.getAuthorList().get(idx));
        }
      }
    });

    journalList.addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent ev) {
        int idx = journalList.getSelectedIndex();
        if (idx < 0) {
          journalPanel.setItem(null);
        } else {
          journalPanel.setItem(state.getJournalList().get(idx));
        }
      }
    });

    addArticleManually.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent ev) {
        Article art = state.createArticle();
        art.setTitle("New article");
        tabbedpane.setSelectedIndex(0);
        articleList.setSelectedIndex(state.getArticleList().indexOf(art));
      }
    });

    addAuthorItem.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent ev) {
        Author au = state.createAuthor();
        au.setState(state);
        au.setLastName("New");
        au.setFirstName("Author");
        tabbedpane.setSelectedIndex(1);
        authorList.setSelectedIndex(state.getAuthorList().indexOf(au));
      }
    });

    addJournalItem.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent ev) {
        Journal jo = state.createJournal();
        jo.setState(state);
        jo.setName("New journal");
        tabbedpane.setSelectedIndex(2);
        journalList.setSelectedIndex(state.getJournalList().indexOf(jo));
      }
    });

    state.getJournalListModel().addListDataListener(articlePanel);

    mainframe.getContentPane().add(splitpane);

    mainframe.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    mainframe.setSize(1024, 768);
    mainframe.setVisible(true);

    //Article.fromDoi(state, "10.1016/0167-4048(88)90003-X");
    //Article.fromDoi(state, "10.21105/joss.02946");
    //Article.fromDoi(state, "10.1007/3-540-48519-8_18");
  }

  private static class ListSelectionWatcher<E> implements ListDataListener, ListSelectionListener {
    private E selected = null;
    private JList<E> list;

    private ListSelectionWatcher(JList<E> list) {
      this.list = list;
    }

    private void updateSelection() {
      list.setSelectedValue(selected, true);
      String str = selected == null ? "null" : selected.toString();
      System.out.println("Updated: " + str);
    }

    @Override
    public void contentsChanged(ListDataEvent ev) {
      updateSelection();
    }

    @Override
    public void intervalAdded(ListDataEvent ev) {
      updateSelection();
    }

    @Override
    public void intervalRemoved(ListDataEvent ev) {
      updateSelection();
    }

    @Override
    public void valueChanged(ListSelectionEvent ev) {
      selected = list.getSelectedValue();
      String str = selected == null ? "null" : selected.toString();
      System.out.println("Selected: " + str);
    }
  }
}
