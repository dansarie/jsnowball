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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import se.dansarie.jsnowball.model.Tag;

public class TagPanel extends SnowballMemberPanel<Tag> {

  private JTextField name = new JTextField();
  private JTextArea notes = new JTextArea();
  private JButton deleteButton = new JButton("Remove tag");
  private JButton mergeButton = new JButton("Merge tags");
  private JButton colorButton;

  private AbstractAction moveUpAction = new AbstractAction("Move up") {
    @Override
    public void actionPerformed(ActionEvent ev) {
      getItem().moveUp();
    }
  };

  private AbstractAction moveDownAction = new AbstractAction("Move down") {
    @Override
    public void actionPerformed(ActionEvent ev) {
      getItem().moveDown();
    }
  };

  private JButton upButton = new JButton(moveUpAction);
  private JButton downButton = new JButton(moveDownAction);

  private AbstractAction selectColorAction = new AbstractAction() {
    @Override
    public void actionPerformed(ActionEvent ev) {
      Color col = JColorChooser.showDialog(TagPanel.this, "Select tag color",
          getItem() == null ? Color.black : new Color(getItem().getColor()));
      if (col != null) {
        getItem().setColor(col.getRed() << 16 | col.getGreen() << 8 | col.getBlue());
        colorButton.setBackground(col);
      }
    }
  };

  public TagPanel() {
    colorButton = new JButton(selectColorAction);
    colorButton.setBackground(Color.BLACK);
    Dimension dim = new Dimension(40, 40);
    colorButton.setPreferredSize(dim);

    addComponent("Name", name);
    addComponent("Notes", new JScrollPane(notes));
    addComponent("Color", colorButton);
    addComponent("", upButton);
    addComponent("", downButton);
    addComponent("", deleteButton);
    addComponent("", mergeButton);

    disableComponents();

    deleteButton.addActionListener(ev -> {
      if (JOptionPane.showConfirmDialog(deleteButton, "Do you really wish to delete this tag?",
          "Delete tag", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) ==
              JOptionPane.YES_OPTION) {
            Tag tag = getItem();
            setItem(null);
            tag.remove();
          }
    });

    mergeButton.addActionListener(ev -> {
      ArrayList<Tag> tags = new ArrayList<>(getItem().getState().getTags());
      tags.remove(getItem());
      if (tags.size() == 0) {
        return;
      }
      Tag tag = (Tag)JOptionPane.showInputDialog(mergeButton,
          "Select the tag you wish to merge into this one.", "Merge tags",
          JOptionPane.QUESTION_MESSAGE, null, tags.toArray(), tags.get(0));
      if (tag == null) {
        return;
      }
      getItem().merge(tag);
    });
  }

  @Override
  public void update() {
    Tag ta = getItem();
    if (ta == null) {
      disableComponents();
      name.setText("");
      notes.setText("");
      colorButton.setBackground(Color.BLACK);
      return;
    }
    name.setText(ta.getName());
    notes.setText(ta.getNotes());
    colorButton.setBackground(new Color(ta.getColor()));
    enableComponents();
  }

  @Override
  protected Consumer<String> textComponentUpdated(JTextComponent comp) {
    Tag ta = getItem();
    if (comp == name) {
      return s -> ta.setName(s);
    } else if (comp == notes) {
      return s -> ta.setNotes(s);
    }
    throw new IllegalArgumentException("Component not recognized: " + comp.toString());
  }
}
