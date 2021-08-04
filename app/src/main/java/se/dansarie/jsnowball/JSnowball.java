package se.dansarie.jsnowball;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

public class JSnowball {
  public static void main(String[] args) {
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

    JMenuItem aboutitem = new JMenuItem("About JSnowball...");
    JMenu helpmenu = new JMenu("Help");
    helpmenu.add(aboutitem);
    JMenuBar menubar = new JMenuBar();
    menubar.add(filemenu);
    menubar.add(helpmenu);

    mainframe.setJMenuBar(menubar);

    JSplitPane articlessplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    articlessplit.setTopComponent(new JList());
    articlessplit.setBottomComponent(new JList());

    JTabbedPane tabbedpane = new JTabbedPane(JTabbedPane.TOP);
    tabbedpane.addTab("Articles", articlessplit);
    mainframe.getContentPane().add(tabbedpane);

    mainframe.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    mainframe.setSize(1024, 768);
    mainframe.setVisible(true);

    Article.fromDoi("10.1016/0167-4048(88)90003-X");
  }
}
