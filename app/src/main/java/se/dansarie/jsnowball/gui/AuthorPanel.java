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

import java.util.ArrayList;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import se.dansarie.jsnowball.model.Article;
import se.dansarie.jsnowball.model.Author;

public class AuthorPanel extends SnowballMemberPanel<Author> {

  private JTextField firstName = new JTextField();
  private JTextField lastName = new JTextField();
  private JTextField organizationName = new JTextField();
  private JTextField orcid = new JTextField();
  private JTextField label = new JTextField();
  private JTextArea notes = new JTextArea();
  private JList<Article> articles = new JList<>();
  private JButton deleteButton = new JButton("Remove author");
  private JButton mergeButton = new JButton("Merge authors");

  public AuthorPanel() {
    addComponent("First name", firstName);
    addComponent("Last name", lastName);
    addComponent("Organization name", organizationName);
    addComponent("ORCID", orcid);
    addComponent("Label", label);
    addComponent("Notes", new JScrollPane(notes));
    addComponent("Articles", new JScrollPane(articles));
    addComponent("", deleteButton);
    addComponent("", mergeButton);
    disableComponents();

    deleteButton.addActionListener(ev -> {
      if (JOptionPane.showConfirmDialog(deleteButton, "Do you really wish to delete this author?",
          "Delete author", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) ==
              JOptionPane.YES_OPTION) {
            Author au = getItem();
            setItem(null);
            au.remove();
          }
    });

    mergeButton.addActionListener(ev -> {
      ArrayList<Author> authors = new ArrayList<>(getItem().getState().getAuthors());
      authors.remove(getItem());
      if (authors.size() == 0) {
        return;
      }
      Author au = (Author)JOptionPane.showInputDialog(mergeButton,
          "Select the author you wish to merge into this one.", "Merge authors",
          JOptionPane.QUESTION_MESSAGE, null, authors.toArray(), authors.get(0));
      if (au == null) {
        return;
      }
      getItem().merge(au);
    });
  }

  @Override
  public void update() {
    Author a = getItem();
    if (a == null) {
      disableComponents();
      firstName.setText("");
      lastName.setText("");
      organizationName.setText("");
      orcid.setText("");
      label.setText("");
      notes.setText("");
      articles.setListData(new Article[0]);
      return;
    }
    firstName.setText(a.getFirstName());
    lastName.setText(a.getLastName());
    organizationName.setText(a.getOrgName());
    orcid.setText(a.getOrcId());
    label.setText(a.getLabel());
    notes.setText(a.getNotes());
    articles.setListData(a.getArticles().toArray(new Article[0]));
    enableComponents();
  }

  @Override
  protected Consumer<String> textComponentUpdated(JTextComponent comp) {
    Author a = getItem();
    if (comp == firstName) {
      return s -> a.setFirstName(s);
    } else if (comp == lastName) {
      return s -> a.setLastName(s);
    } else if (comp == organizationName) {
      return s -> a.setOrgName(s);
    } else if (comp == orcid) {
      return s -> a.setOrcId(s);
    } else if (comp == notes) {
      return s -> a.setNotes(s);
    } else if (comp == label) {
      return s -> a.setLabel(s);
    }

    throw new IllegalArgumentException("Component not recognized: " + comp.toString());
  }
}
