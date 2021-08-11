package se.dansarie.jsnowball;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class JSnowball {
  private ArticlePanel articlePanel = new ArticlePanel();
  private AuthorPanel authorPanel = new AuthorPanel();
  private JournalPanel journalPanel = new JournalPanel();
  private SnowballState state;
  private JFrame frame = new JFrame("JSnowball");
  private JList<Article> articleList = new JList<>();
  private JList<Author> authorList = new JList<>();
  private JList<Journal> journalList = new JList<>();
  private JMenuBar menubar = new JMenuBar();
  private JScrollPane articleScrollPane = new JScrollPane(articlePanel,
      JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
  private JScrollPane authorScrollPane = new JScrollPane(authorPanel,
      JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
  private JScrollPane journalScrollPane = new JScrollPane(journalPanel,
      JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
  private JSplitPane leftSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
  private JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
  private JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
  private ListSelectionWatcher<Article> articleSelectionWatcher;
  private ListSelectionWatcher<Author> authorSelectionWatcher;
  private ListSelectionWatcher<Journal> journalSelectionWatcher;

  private JSnowball() {
    createFrame();
    setState(new SnowballState());
  }

  private void setState(SnowballState state) {
    if (this.state != null) {
      articleList.removeListSelectionListener(articleSelectionWatcher);
      authorList.removeListSelectionListener(authorSelectionWatcher);
      journalList.removeListSelectionListener(journalSelectionWatcher);
      this.state.getArticleListModel().removeListDataListener(articleSelectionWatcher);
      this.state.getAuthorListModel().removeListDataListener(authorSelectionWatcher);
      this.state.getJournalListModel().removeListDataListener(journalSelectionWatcher);
      this.state.getJournalListModel().removeListDataListener(articlePanel);
    }
    this.state = Objects.requireNonNull(state);
    articleList.setModel(state.getArticleListModel());
    authorList.setModel(state.getAuthorListModel());
    journalList.setModel(state.getJournalListModel());
    articleSelectionWatcher = new ListSelectionWatcher<>(articleList);
    authorSelectionWatcher = new ListSelectionWatcher<>(authorList);
    journalSelectionWatcher = new ListSelectionWatcher<>(journalList);
    articleList.addListSelectionListener(articleSelectionWatcher);
    authorList.addListSelectionListener(authorSelectionWatcher);
    journalList.addListSelectionListener(journalSelectionWatcher);
    state.getArticleListModel().addListDataListener(articleSelectionWatcher);
    state.getAuthorListModel().addListDataListener(authorSelectionWatcher);
    state.getJournalListModel().addListDataListener(journalSelectionWatcher);
    state.getJournalListModel().addListDataListener(articlePanel);
  }

  private boolean saveState() {
    JFileChooser chooser = new JFileChooser();
    if (chooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) {
      return false;
    }
    File fi = chooser.getSelectedFile();
    if (fi == null) {
      return false;
    }
    try (FileOutputStream fos = new FileOutputStream(fi);
        ObjectOutputStream oos = new ObjectOutputStream(fos)) {
      oos.writeObject(state);
    } catch (IOException ex) {
      System.out.println(ex);
      JOptionPane.showMessageDialog(frame, "An error occured while attempting to save the project"
          + " file.", "File error", JOptionPane.ERROR_MESSAGE);
      return false;
    }
    return true;
  }

  private void createMenuBar() {
    JMenuItem newprojectitem = new JMenuItem("New project");
    JMenuItem openprojectitem = new JMenuItem("Open project...");
    JMenuItem saveprojectitem = new JMenuItem("Save project...");
    JMenuItem exititem = new JMenuItem("Quit");

    JMenu filemenu = new JMenu("File");
    filemenu.add(newprojectitem);
    filemenu.addSeparator();
    filemenu.add(openprojectitem);
    filemenu.add(saveprojectitem);
    filemenu.addSeparator();
    filemenu.add(exititem);

    JMenu operationsMenu = new JMenu("Operations");
    JMenuItem addArticleDoi = new JMenuItem("Add article from DOI...");
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
    JMenu helpmenu = new JMenu("Help");
    helpmenu.add(aboutitem);
    menubar.add(filemenu);
    menubar.add(operationsMenu);
    menubar.add(helpmenu);

    KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK);
    newprojectitem.setAccelerator(stroke);
    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK);
    openprojectitem.setAccelerator(stroke);
    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK);
    saveprojectitem.setAccelerator(stroke);
    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK);
    exititem.setAccelerator(stroke);
    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK);
    addArticleDoi.setAccelerator(stroke);

    newprojectitem.addActionListener(ev -> {
      if (!state.isSaved()) {
        int result = JOptionPane.showConfirmDialog(frame, "Current project has not been saved. "
            + "Do you wish to destroy unsaved work?", "New project", JOptionPane.YES_NO_OPTION);
        if (result != JOptionPane.YES_OPTION) {
          return;
        }
      }
      setState(new SnowballState());
    });

    openprojectitem.addActionListener(ev -> {
      if (!state.isSaved()) {
        int result = JOptionPane.showConfirmDialog(frame, "Current project has not been saved. "
            + "Do you wish to destroy unsaved work?", "Open project", JOptionPane.YES_NO_OPTION);
        if (result != JOptionPane.YES_OPTION) {
          return;
        }
      }
      JFileChooser chooser = new JFileChooser();
      if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
        return;
      }
      File fi = chooser.getSelectedFile();
      if (fi == null) {
        return;
      }
      try (FileInputStream fis = new FileInputStream(fi);
          ObjectInputStream ois = new ObjectInputStream(fis)) {
        SnowballState newState = (SnowballState)ois.readObject();
        setState(newState);
      } catch (ClassCastException | ClassNotFoundException | IOException ex) {
        System.out.println(ex);
        JOptionPane.showMessageDialog(frame, "An error occured while attempting to read the project"
            + " file.", "File error", JOptionPane.ERROR_MESSAGE);
      }
    });

    saveprojectitem.addActionListener(ev -> saveState());

    exititem.addActionListener(ev -> {
      if (!state.isSaved()) {
        int result = JOptionPane.showConfirmDialog(frame, "Current project has not been saved. "
            + "Do you wish to save it before quitting?", "Quit", JOptionPane.YES_NO_CANCEL_OPTION);
        if (result == JOptionPane.CANCEL_OPTION) {
          return;
        }
        if (result == JOptionPane.YES_OPTION) {
          if (!saveState()) {
            return;
          }
        }
      }
      frame.setVisible(false);
      frame.dispose();
    });

    addArticleDoi.addActionListener(ev -> {
      String doi = JOptionPane.showInputDialog(frame, "Enter a DOI",
          "Create article from DOI", JOptionPane.QUESTION_MESSAGE);
      if (doi == null) {
        return;
      }
      Article.fromDoi(state, doi);
    });

    addArticleManually.addActionListener(ev -> {
      Article art = state.createArticle();
      art.setTitle("New article");
      tabbedPane.setSelectedIndex(0);
      articleList.setSelectedIndex(state.getArticleList().indexOf(art));
    });

    addAuthorItem.addActionListener(ev -> {
      Author au = state.createAuthor();
      au.setState(state);
      au.setLastName("New");
      au.setFirstName("Author");
      tabbedPane.setSelectedIndex(1);
      authorList.setSelectedIndex(state.getAuthorList().indexOf(au));
    });

    addJournalItem.addActionListener(ev -> {
      Journal jo = state.createJournal();
      jo.setState(state);
      jo.setName("New journal");
      tabbedPane.setSelectedIndex(2);
      journalList.setSelectedIndex(state.getJournalList().indexOf(jo));
    });

    aboutitem.addActionListener(ev -> JOptionPane.showMessageDialog(frame,
        "<html><center>JSnowball<br/><br/>Copyright (C) 2021 Marcus Dansarie</center></html>",
        "About JSnowball", JOptionPane.INFORMATION_MESSAGE));
  }

  private void createListPane() {
    int vsbPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS;
    int hsbPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER;
    JScrollPane articleScroll = new JScrollPane(articleList, vsbPolicy, hsbPolicy);
    JScrollPane authorScroll = new JScrollPane(authorList, vsbPolicy, hsbPolicy);
    JScrollPane journalScroll = new JScrollPane(journalList, vsbPolicy, hsbPolicy);

    tabbedPane.addTab("Articles", articleScroll);
    tabbedPane.addTab("Authors", authorScroll);
    tabbedPane.addTab("Journals", journalScroll);

    tabbedPane.addChangeListener(ev -> {
      switch (tabbedPane.getSelectedIndex()) {
        case 0:
          leftSplitPane.setBottomComponent(articleScrollPane);
          break;
        case 1:
          leftSplitPane.setBottomComponent(authorScrollPane);
          break;
        case 2:
          leftSplitPane.setBottomComponent(journalScrollPane);
          break;
        default:
          break;
      }
    });

    articleList.addListSelectionListener(ev -> {
      int idx = articleList.getSelectedIndex();
      if (idx < 0) {
        articlePanel.setItem(null);
      } else {
        articlePanel.setItem(state.getArticleList().get(idx));
      }
    });

    authorList.addListSelectionListener(ev -> {
      int idx = authorList.getSelectedIndex();
      if (idx < 0) {
        authorPanel.setItem(null);
      } else {
        authorPanel.setItem(state.getAuthorList().get(idx));
      }
    });

    journalList.addListSelectionListener(ev -> {
      int idx = journalList.getSelectedIndex();
      if (idx < 0) {
        journalPanel.setItem(null);
      } else {
        journalPanel.setItem(state.getJournalList().get(idx));
      }
    });
  }

  private void createLeftSplitPane() {
    createListPane();
    leftSplitPane.setTopComponent(tabbedPane);
    leftSplitPane.setBottomComponent(articleScrollPane);
  }

  private void createMainSplitPane() {
    createLeftSplitPane();
    mainSplitPane.setLeftComponent(leftSplitPane);
    mainSplitPane.setRightComponent(new JPanel());
  }

  private void createFrame() {
    createMenuBar();
    frame.setJMenuBar(menubar);
    createMainSplitPane();
    frame.getContentPane().add(mainSplitPane);
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    int width = 1024;
    int height = 768;
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.setSize(width, height);
    frame.setLocation((int)(dim.getWidth() - width) / 2, (int)(dim.getHeight() - height) / 2);
    frame.setVisible(true);
  }
  public static void main(String[] args) {
    new JSnowball();

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
      SwingUtilities.invokeLater(() -> list.setSelectedValue(selected, true));
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
    }
  }
}
