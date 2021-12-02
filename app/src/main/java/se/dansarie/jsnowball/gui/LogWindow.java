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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
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

  private LogWindow() {
    setSize(800, 600);
    setTitle("JSnowball debug log");
    log.setEditable(false);
    getContentPane().add(scrollPane);
  }

  public void addThrowable(Throwable thr) {
    Objects.requireNonNull(thr);
    StringBuilder sb = new StringBuilder(sdf.format(new Date()));
    sb.append('\n');
    try (
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      PrintStream ps = new PrintStream(baos, false, StandardCharsets.UTF_8)
    ) {
      thr.printStackTrace(ps);
      ps.flush();
      sb.append(baos.toString());
    } catch (IOException ex) {
      sb.append("Exception when getting stack trace: ");
      sb.append(ex.toString());
    }
    SwingUtilities.invokeLater(()-> log.append(sb.toString()));
  }

  public static LogWindow getInstance() {
    return instance;
  }
}
