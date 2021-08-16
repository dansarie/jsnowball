package se.dansarie.jsnowball.gui;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import java.util.ArrayList;
import java.util.function.Consumer;

import se.dansarie.jsnowball.model.Tag;

public class TagPanel extends SnowballMemberPanel<Tag> {

  private JTextField name = new JTextField();
  private JTextArea notes = new JTextArea();
  private JButton deleteButton = new JButton("Remove tag");
  private JButton mergeButton = new JButton("Merge tags");

  public TagPanel() {
    addComponent("Name", name);
    addComponent("Notes", new JScrollPane(notes));
    addComponent("", deleteButton);
    addComponent("", mergeButton);
    disableComponents();

    deleteButton.addActionListener(ev -> {
      if (JOptionPane.showConfirmDialog(deleteButton, "Do you really wish to delete this tag?",
          "Delete tag", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) ==
              JOptionPane.YES_OPTION) {
            Tag art = getItem();
            setItem(null);
            art.getState().removeTag(art);
          }
    });

    mergeButton.addActionListener(ev -> {
      ArrayList<Tag> tags = new ArrayList<>(getItem().getState().getTagList());
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
      getItem().getState().mergeTags(getItem(), tag);
    });
  }

  @Override
  public void update() {
    Tag ta = getItem();
    if (ta == null) {
      disableComponents();
      name.setText("");
      notes.setText("");
      return;
    }
    name.setText(ta.getName());
    notes.setText(ta.getNotes());
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
