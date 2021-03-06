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
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import se.dansarie.jsnowball.model.Journal;

public class JournalPanel extends SnowballMemberPanel<Journal> {

  private JTextField name = new JTextField();
  private JTextField issn = new JTextField();
  private JTextArea notes = new JTextArea();
  private JButton deleteButton = new JButton("Remove journal");
  private JButton mergeButton = new JButton("Merge journals");

  public JournalPanel() {
    addComponent("Name", name);
    addComponent("ISSN", issn);
    addComponent("Notes", new JScrollPane(notes));
    addComponent("", deleteButton);
    addComponent("", mergeButton);
    disableComponents();

    deleteButton.addActionListener(ev -> {
      if (JOptionPane.showConfirmDialog(deleteButton, "Do you really wish to delete this journal?",
          "Delete journal", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) ==
              JOptionPane.YES_OPTION) {
            Journal jo = getItem();
            setItem(null);
            jo.remove();
          }
    });

    mergeButton.addActionListener(ev -> {
      ArrayList<Journal> journals = new ArrayList<>(getItem().getState().getJournals());
      journals.remove(getItem());
      if (journals.size() == 0) {
        return;
      }
      Journal jo = (Journal)JOptionPane.showInputDialog(mergeButton,
          "Select the journal you wish to merge into this one.", "Merge journals",
          JOptionPane.QUESTION_MESSAGE, null, journals.toArray(), journals.get(0));
      if (jo == null) {
        return;
      }
      getItem().merge(jo);
    });
  }

  @Override
  public void update() {
    Journal j = getItem();
    if (j == null) {
      disableComponents();
      name.setText("");
      issn.setText("");
      notes.setText("");
      return;
    }
    name.setText(j.getName());
    issn.setText(j.getIssn());
    notes.setText(j.getNotes());
    enableComponents();
  }

  @Override
  protected Consumer<String> textComponentUpdated(JTextComponent comp) {
    Journal j = getItem();
    if (comp == name) {
      return s -> j.setName(s);
    } else if (comp == issn) {
      return s -> j.setIssn(s);
    } else if (comp == notes) {
      return s -> j.setNotes(s);
    }
    throw new IllegalArgumentException("Component not recognized: " + comp.toString());
  }
}
