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
      public void actionPerformed(ActionEvent e) {
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
    JMenu addArticleMenu = new JMenu("Add article");
    JMenuItem addArticleDoi = new JMenuItem("From DOI...");
    addArticleDoi.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent evt) {
        String doi = JOptionPane.showInputDialog(mainframe, "Enter a DOI",
            "Create article from DOI", JOptionPane.QUESTION_MESSAGE);
        if (doi == null) {
          return;
        }
        Article.fromDoi(state, doi);
      }
    });
    JMenuItem addArticleManually = new JMenuItem("Manually...");
    addArticleMenu.add(addArticleDoi);
    addArticleMenu.add(addArticleManually);
    JMenuItem addAuthorItem = new JMenuItem("Add author...");
    JMenuItem addJournalItem = new JMenuItem("Add journal...");
    operationsMenu.add(addArticleMenu);
    operationsMenu.add(addAuthorItem);
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

    JList<String> articleList = new JList<>(state.getArticleListModel());
    JList<String> authorList = new JList<>(state.getAuthorListModel());
    JList<String> journalList = new JList<>(state.getJournalListModel());

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
        articlePanel.setItem(state.getArticleList().get(articleList.getSelectedIndex()));
      }
    });

    authorList.addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent ev) {
        int idx = ev.getFirstIndex();
        System.out.println(idx);
        authorPanel.setItem(state.getAuthorList().get(authorList.getSelectedIndex()));
      }
    });

    journalList.addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent ev) {
        int idx = ev.getFirstIndex();
        System.out.println(idx);
        journalPanel.setItem(state.getJournalList().get(journalList.getSelectedIndex()));
      }
    });

    mainframe.getContentPane().add(splitpane);

    mainframe.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    mainframe.setSize(1024, 768);
    mainframe.setVisible(true);

    Article.fromDoi(state, "10.1016/0167-4048(88)90003-X");
    Article.fromDoi(state, "10.21105/joss.02946");
  }
}
