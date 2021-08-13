package se.dansarie.jsnowball;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Objects;
import java.util.prefs.Preferences;

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
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

public class JSnowball {
  private ArticlePanel articlePanel = new ArticlePanel();
  private AuthorPanel authorPanel = new AuthorPanel();
  private JournalPanel journalPanel = new JournalPanel();
  private TagPanel tagPanel = new TagPanel();
  private SnowballState state;
  private JFrame frame = new JFrame("JSnowball");
  private JList<Article> articleList = new JList<>();
  private JList<Author> authorList = new JList<>();
  private JList<Journal> journalList = new JList<>();
  private JList<Tag> tagList = new JList<>();
  private JMenuBar menubar = new JMenuBar();
  private JMenu openrecentmenu = new JMenu("Open recent");
  private JScrollPane articleScrollPane = new JScrollPane(articlePanel,
      JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
  private JScrollPane authorScrollPane = new JScrollPane(authorPanel,
      JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
  private JScrollPane journalScrollPane = new JScrollPane(journalPanel,
      JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
  private JScrollPane tagScrollPane = new JScrollPane(tagPanel,
      JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
  private JSplitPane leftSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
  private JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
  private JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
  private JTabbedPane rightTabbedPane = new JTabbedPane(JTabbedPane.TOP);
  private ListSelectionWatcher<Article> articleSelectionWatcher;
  private ListSelectionWatcher<Author> authorSelectionWatcher;
  private ListSelectionWatcher<Journal> journalSelectionWatcher;
  private ListSelectionWatcher<Tag> tagSelectionWatcher;
  private File currentFile = null;

  private SnowballTableModel articleTableModel = new SnowballTableModel() {
    @Override
    public int getRowCount() {
      if (state == null) {
        return 0;
      }
      return state.getArticleList().size();
    }

    @Override
    public String getColumnName(int col) {
      switch (col) {
        case 0: return "Article";
        case 1: return "References";
        default: throw new IllegalArgumentException();
      }
    }

    @Override
    public Object getValueAt(int row, int col) {
      Article art = state.getArticleList().get(row);
      switch (col) {
        case 0: return art.toString();
        case 1: return Integer.valueOf(state.getReferencedByList(art).size());
      }
      throw new IllegalArgumentException();
    }
  };

  private SnowballTableModel authorTableModel = new SnowballTableModel() {
    @Override
    public int getRowCount() {
      if (state == null) {
        return 0;
      }
      return state.getAuthorList().size();
    }

    @Override
    public String getColumnName(int col) {
      switch (col) {
        case 0: return "Author";
        case 1: return "Articles";
        case 2: return "References";
        default: throw new IllegalArgumentException();
      }
    }

    @Override
    public Class<?> getColumnClass(int col) {
      switch (col) {
        case 0: return String.class;
        case 1: return Integer.class;
        case 2: return Integer.class;
        default: throw new IllegalArgumentException();
      }
    }

    @Override
    public int getColumnCount() {
      return 3;
    }

    @Override
    public Object getValueAt(int row, int col) {
      Author au = state.getAuthorList().get(row);
      int i = 0;
      switch (col) {
        case 0: return au.toString();
        case 1:
          for (Article art : state.getArticleList()) {
            if (art.getAuthors().contains(au)) {
              i += 1;
            }
          }
          return Integer.valueOf(i);
        case 2:
          for (Article art : state.getArticleList()) {
            if (art.getAuthors().contains(au)) {
              i += state.getReferencedByList(art).size();
            }
          }
          return Integer.valueOf(i);
      }
      throw new IllegalArgumentException();
    }
  };

  private SnowballTableModel journalTableModel = new SnowballTableModel() {
    @Override
    public int getRowCount() {
      if (state == null) {
        return 0;
      }
      return state.getJournalList().size();
    }

    @Override
    public int getColumnCount() {
      return 3;
    }

    @Override
    public String getColumnName(int col) {
      switch (col) {
        case 0: return "Journal";
        case 1: return "Articles";
        case 2: return "References";
        default: throw new IllegalArgumentException();
      }
    }

    @Override
    public Class<?> getColumnClass(int col) {
      switch (col) {
        case 0: return String.class;
        case 1: return Integer.class;
        case 2: return Integer.class;
        default: throw new IllegalArgumentException();
      }
    }

    @Override
    public Object getValueAt(int row, int col) {
      Journal jo = state.getJournalList().get(row);
      int i = 0;
      switch (col) {
        case 0: return jo.toString();
        case 1:
          for (Article art : state.getArticleList()) {
            if (art.getJournal() == jo) {
              i += 1;
            }
          }
          return Integer.valueOf(i);
        case 2:
          for (Article art : state.getArticleList()) {
            if (art.getJournal() == jo) {
              i += state.getReferencedByList(art).size();
            }
          }
          return Integer.valueOf(i);
      }
      throw new IllegalArgumentException();
    }
  };

  private SnowballTableModel tagTableModel = new SnowballTableModel() {
    @Override
    public int getRowCount() {
      if (state == null) {
        return 0;
      }
      return state.getTagList().size();
    }

    @Override
    public int getColumnCount() {
      return 2;
    }

    @Override
    public String getColumnName(int col) {
      switch (col) {
        case 0: return "Tag";
        case 1: return "Articles";
        default: throw new IllegalArgumentException();
      }
    }

    @Override
    public Object getValueAt(int row, int col) {
      Tag tag = state.getTagList().get(row);
      switch (col) {
        case 0: return tag.toString();
        case 1:
          int i = 0;
          for (Article art : state.getArticleList()) {
            if (state.getTagList(art).contains(tag)) {
              i += 1;
            }
          }
          return Integer.valueOf(i);
      }
      throw new IllegalArgumentException();
    }
  };

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
    tagList.setModel(state.getTagListModel());
    articleSelectionWatcher = new ListSelectionWatcher<>(articleList);
    authorSelectionWatcher = new ListSelectionWatcher<>(authorList);
    journalSelectionWatcher = new ListSelectionWatcher<>(journalList);
    tagSelectionWatcher = new ListSelectionWatcher<>(tagList);
    articleList.addListSelectionListener(articleSelectionWatcher);
    authorList.addListSelectionListener(authorSelectionWatcher);
    journalList.addListSelectionListener(journalSelectionWatcher);
    tagList.addListSelectionListener(tagSelectionWatcher);
    state.getArticleListModel().addListDataListener(articleSelectionWatcher);
    state.getAuthorListModel().addListDataListener(authorSelectionWatcher);
    state.getJournalListModel().addListDataListener(journalSelectionWatcher);
    state.getTagListModel().addListDataListener(tagSelectionWatcher);
    state.getJournalListModel().addListDataListener(articlePanel);
    articleTableModel.setState(state);
    authorTableModel.setState(state);
    journalTableModel.setState(state);
    tagTableModel.setState(state);
  }

  private boolean saveState(boolean showDialog) {
    File fi;
    if (showDialog || currentFile == null) {
      JFileChooser chooser = new JFileChooser();
      chooser.setCurrentDirectory(getDirectoryPreference());
      if (chooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) {
        return false;
      }
      fi = chooser.getSelectedFile();
      if (fi == null) {
        return false;
      }
      saveDirectoryPreference(fi);
    } else {
      fi = currentFile;
    }

    try (FileOutputStream fos = new FileOutputStream(fi);
        ObjectOutputStream oos = new ObjectOutputStream(fos)) {
      oos.writeObject(state);
    } catch (IOException ex) {
      JOptionPane.showMessageDialog(frame, "An error occured while attempting to save the project"
          + " file.", "File error", JOptionPane.ERROR_MESSAGE);
      return false;
    }
    updateRecentFiles(fi);
    currentFile = fi;
    return true;
  }

  private void createMenuBar() {
    JMenuItem newprojectitem = new JMenuItem("New project");
    JMenuItem openprojectitem = new JMenuItem("Open project...");
    JMenuItem saveprojectitem = new JMenuItem("Save project");
    JMenuItem saveasprojectitem = new JMenuItem("Save project as...");
    JMenuItem exititem = new JMenuItem("Quit");

    createRecentFilesMenu();

    JMenu filemenu = new JMenu("File");
    filemenu.add(newprojectitem);
    filemenu.addSeparator();
    filemenu.add(openprojectitem);
    filemenu.add(openrecentmenu);
    filemenu.addSeparator();
    filemenu.add(saveprojectitem);
    filemenu.add(saveasprojectitem);
    filemenu.addSeparator();
    filemenu.add(exititem);

    JMenu operationsMenu = new JMenu("Operations");
    JMenuItem addArticleDoi = new JMenuItem("Add article from DOI...");
    JMenuItem addArticleManually = new JMenuItem("New article");
    JMenuItem addAuthorItem = new JMenuItem("New author");
    JMenuItem addJournalItem = new JMenuItem("New journal");
    JMenuItem addTagItem = new JMenuItem("New tag");
    operationsMenu.add(addArticleManually);
    operationsMenu.add(addArticleDoi);
    operationsMenu.addSeparator();
    operationsMenu.add(addAuthorItem);
    operationsMenu.addSeparator();
    operationsMenu.add(addJournalItem);
    operationsMenu.addSeparator();
    operationsMenu.add(addTagItem);

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
      chooser.setCurrentDirectory(getDirectoryPreference());
      if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
        return;
      }
      File fi = chooser.getSelectedFile();
      if (fi == null) {
        return;
      }
      saveDirectoryPreference(fi);
      updateRecentFiles(fi);
      try (FileInputStream fis = new FileInputStream(fi);
          ObjectInputStream ois = new ObjectInputStream(fis)) {
        SnowballState newState = (SnowballState)ois.readObject();
        setState(newState);
        currentFile = fi;
      } catch (ClassCastException | ClassNotFoundException | IOException ex) {
        JOptionPane.showMessageDialog(frame, "An error occured while attempting to read the project"
            + " file.", "File error", JOptionPane.ERROR_MESSAGE);
      }
    });

    saveprojectitem.addActionListener(ev -> saveState(false));
    saveasprojectitem.addActionListener(ev -> saveState(true));

    exititem.addActionListener(ev -> {
      if (!state.isSaved()) {
        int result = JOptionPane.showConfirmDialog(frame, "Current project has not been saved. "
            + "Do you wish to save it before quitting?", "Quit", JOptionPane.YES_NO_CANCEL_OPTION);
        if (result == JOptionPane.CANCEL_OPTION) {
          return;
        }
        if (result == JOptionPane.YES_OPTION) {
          if (!saveState(false)) {
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

    addTagItem.addActionListener(ev -> {
      Tag ta = state.createTag();
      ta.setName("New tag");
      tabbedPane.setSelectedIndex(3);
      tagList.setSelectedIndex(state.getTagList().indexOf(ta));
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
    JScrollPane tagScroll = new JScrollPane(tagList, vsbPolicy, hsbPolicy);

    tabbedPane.addTab("Articles", articleScroll);
    tabbedPane.addTab("Authors", authorScroll);
    tabbedPane.addTab("Journals", journalScroll);
    tabbedPane.addTab("Tags", tagScroll);

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
        case 3:
          leftSplitPane.setBottomComponent(tagScrollPane);
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

    tagList.addListSelectionListener(ev -> {
      int idx = tagList.getSelectedIndex();
      if (idx < 0) {
        tagPanel.setItem(null);
      } else {
        tagPanel.setItem(state.getTagList().get(idx));
      }
    });
  }

  private void createLeftSplitPane() {
    createListPane();
    leftSplitPane.setTopComponent(tabbedPane);
    leftSplitPane.setBottomComponent(articleScrollPane);
  }

  private void createRightTabbedPane() {
    JTable articleTable = new JTable(articleTableModel);
    JTable authorTable = new JTable(authorTableModel);
    JTable journalTable = new JTable(journalTableModel);
    JTable tagTable = new JTable(tagTableModel);
    articleTable.setAutoCreateRowSorter(true);
    authorTable.setAutoCreateRowSorter(true);
    journalTable.setAutoCreateRowSorter(true);
    tagTable.setAutoCreateRowSorter(true);

    rightTabbedPane.add(new JPanel(), "Article graph");
    rightTabbedPane.add(new JPanel(), "Author graph");
    rightTabbedPane.add(new JScrollPane(articleTable), "Articles");
    rightTabbedPane.add(new JScrollPane(authorTable), "Authors");
    rightTabbedPane.add(new JScrollPane(journalTable), "Journals");
    rightTabbedPane.add(new JScrollPane(tagTable), "Tags");
    rightTabbedPane.add(new JPanel(), "Years");
  }

  private void createMainSplitPane() {
    createLeftSplitPane();
    createRightTabbedPane();
    mainSplitPane.setLeftComponent(leftSplitPane);
    mainSplitPane.setRightComponent(rightTabbedPane);
  }

  private void createFrame() {
    createMenuBar();
    frame.setJMenuBar(menubar);
    createMainSplitPane();
    frame.getContentPane().add(mainSplitPane);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.setSize(getFrameDimensionPreference());
    frame.setLocation(getFrameLocationPreference());

    frame.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentMoved(ComponentEvent ev) {
        Preferences prefs = getPreferences();
        Point location = frame.getLocation();
        prefs.putInt("location_x", location.x);
        prefs.putInt("location_y", location.y);
      }

      @Override
      public void componentResized(ComponentEvent ev) {
        Preferences prefs = getPreferences();
        Dimension size = frame.getSize();
        prefs.putInt("dimension_x", size.width);
        prefs.putInt("dimension_y", size.height);
      }
    });

    frame.setVisible(true);
  }

  private Preferences getPreferences() {
    return Preferences.userNodeForPackage(this.getClass());
  }

  private Point getFrameLocationPreference() {
    Preferences prefs = getPreferences();
    int x = prefs.getInt("location_x", 0);
    int y = prefs.getInt("location_y", 0);
    return new Point(x, y);
  }

  private Dimension getFrameDimensionPreference() {
    Preferences prefs = getPreferences();
    int width = prefs.getInt("dimension_x", 1024);
    int height = prefs.getInt("dimension_y", 768);
    return new Dimension(width, height);
  }

  private void saveDirectoryPreference(File dir) {
    if (!dir.isDirectory()) {
      dir = dir.getParentFile();
    }
    String path = null;
    if (dir != null) {
      path = dir.getPath();
    }
    getPreferences().put("last_dir", path);
  }

  private void updateRecentFiles(File fi) {
    Preferences prefs = getPreferences();
    String newpath = fi.getPath();
    ArrayList<String> paths = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      String val = prefs.get("latest_file_" + i, null);
      if (val == null) {
        break;
      }
      paths.add(val);
    }
    for (String path : paths.toArray(new String[paths.size()])) {
      if (path.equals(newpath)) {
        paths.remove(path);
      }
    }
    paths.add(0, newpath);
    for (int i = 0; i < 5; i++) {
      String key = "latest_file_" + i;
      if (i < paths.size()) {
        prefs.put(key, paths.get(i));
      } else {
        prefs.remove(key);
      }
    }
    createRecentFilesMenu();
  }

  private void createRecentFilesMenu() {
    openrecentmenu.removeAll();
    Preferences prefs = getPreferences();
    for (int i = 0; i < 5; i++) {
      String path = prefs.get("latest_file_" + i, null);
      if (path == null) {
        continue;
      }
      File fi = new File(path);
      if (!fi.exists()) {
        continue;
      }
      JMenuItem recentItem = new JMenuItem(path);
      recentItem.addActionListener(ev -> {
        if (!state.isSaved()) {
          int result = JOptionPane.showConfirmDialog(frame, "Current project has not been saved. "
              + "Do you wish to destroy unsaved work?", "Open project", JOptionPane.YES_NO_OPTION);
          if (result != JOptionPane.YES_OPTION) {
            return;
          }
        }
        try (FileInputStream fis = new FileInputStream(fi);
            ObjectInputStream ois = new ObjectInputStream(fis)) {
          SnowballState newState = (SnowballState)ois.readObject();
          setState(newState);
          currentFile = fi;
        } catch (ClassCastException | ClassNotFoundException | IOException ex) {
          JOptionPane.showMessageDialog(frame, "An error occured while attempting to read the "
              + "project file.", "File error", JOptionPane.ERROR_MESSAGE);
        }
      });
      openrecentmenu.add(recentItem);
    }
  }

  private File getDirectoryPreference() {
    Preferences prefs = getPreferences();
    String path = prefs.get("last_dir", null);
    if (path == null) {
      return null;
    }
    return new File(path);
  }

  public static void main(String[] args) {
    new JSnowball();

    //Article.fromDoi(state, "10.1016/0167-4048(88)90003-X");
    //Article.fromDoi(state, "10.21105/joss.02946");
    //Article.fromDoi(state, "10.1007/3-540-48519-8_18");
  }

  private static abstract class SnowballTableModel extends AbstractTableModel
      implements ListDataListener {
    private SnowballState state;

    public void setState(SnowballState newstate) {
      if (state != null) {
        state.getArticleListModel().removeListDataListener(this);
      }
      state = newstate;
      state.getArticleListModel().addListDataListener(this);
      fireTableDataChanged();
    }

    @Override
    public void contentsChanged(ListDataEvent ev) {
      fireTableDataChanged();
    }

    @Override
    public void intervalAdded(ListDataEvent ev) {
      fireTableRowsInserted(ev.getIndex0(), ev.getIndex1());
    }

    @Override
    public void intervalRemoved(ListDataEvent ev) {
      fireTableRowsDeleted(ev.getIndex0(), ev.getIndex1());
    }

    @Override
    public int getColumnCount() {
      return 2;
    }

    @Override
    public Class<?> getColumnClass(int col) {
      switch (col) {
        case 0: return String.class;
        case 1: return Integer.class;
        default: throw new IllegalArgumentException();
      }
    }
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
