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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import se.dansarie.jsnowball.model.Article;
import se.dansarie.jsnowball.model.ScopusCsv;
import se.dansarie.jsnowball.model.SnowballState;

public class ScopusReferenceImportAction extends AbstractAction {
  private static File lastdir = null;
  private Component parent;
  private SnowballState state;
  private Consumer<Article> processor = a -> {};

  public ScopusReferenceImportAction(String name, Component parent) {
    this(name, null, parent);
  }

  public ScopusReferenceImportAction(String name, SnowballState state, Component parent) {
    super(name);
    this.parent = Objects.requireNonNull(parent);
    setState(state);
  }

  public void setState(SnowballState state) {
    this.state = state;
  }

  public void setArticleProcessor(Consumer<Article> processor) {
    this.processor = Objects.requireNonNull(processor);
  }

  @Override
  public void actionPerformed(ActionEvent ev) {
    if (state == null) {
      return;
    }
    JFileChooser chooser = new JFileChooser(lastdir);
    if (chooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
      return;
    }
    lastdir = chooser.getSelectedFile();
    while (!lastdir.isDirectory()) {
      lastdir = lastdir.getParentFile();
    }
    String note = JOptionPane.showInputDialog(parent, "Enter an input note",
        "Add articles from Scopus CSV", JOptionPane.QUESTION_MESSAGE);
    if (note == null) {
      return;
    }
    try {
      for (ScopusCsv csv : ScopusCsv.fromFile(chooser.getSelectedFile())) {
        Article art = null;
        if (csv.doi != null) {
          art = Article.getByDoi(state, csv.doi);
        }
        if (art == null) {
          art = new Article(state, csv);
          art.setNotes(note);
        }
        processor.accept(art);
      }
    } catch (IllegalArgumentException | IOException ex) {
      LogWindow.getInstance().addThrowable(ex);
      JOptionPane.showMessageDialog(parent, "An error occured while reading the input file.",
          "Add articles from Scopus CSV", JOptionPane.ERROR_MESSAGE);
    }
  }
}
