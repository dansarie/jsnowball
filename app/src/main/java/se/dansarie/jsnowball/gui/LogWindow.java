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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class LogWindow extends JFrame {

  private static LogWindow instance = new LogWindow();
  private JTextArea log = new JTextArea();
  private JScrollPane scrollPane = new JScrollPane(log);
  private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
  private WindowAdapter closeListener = new WindowAdapter() {
    @Override
    public void windowClosing(WindowEvent ev) {
      LogWindow.this.dispose();
    }
  };

  private LogWindow() {
    setSize(800, 600);
    setTitle("JSnowball debug log");
    log.setEditable(false);
    getContentPane().add(scrollPane);
  }

  public void addLogData(String logdata) {
    Objects.requireNonNull(logdata);
    StringBuilder sb = new StringBuilder(sdf.format(new Date()));
    sb.append('\n');
    sb.append(logdata);
    sb.append("\n\n");
    SwingUtilities.invokeLater(()-> log.append(sb.toString()));
  }

  public WindowListener getCloseListener() {
    return closeListener;
  }

  public static LogWindow getInstance() {
    return instance;
  }
}
