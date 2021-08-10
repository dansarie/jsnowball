package se.dansarie.jsnowball;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import java.util.function.Consumer;

public class JournalPanel extends SnowballMemberPanel<Journal> {

  private JTextField name = new JTextField();
  private JTextField issn = new JTextField();
  private JTextArea notes = new JTextArea();

  JournalPanel() {
    addComponent("Name", name);
    addComponent("ISSN", issn);
    addComponent("Notes", new JScrollPane(notes));
    disableComponents();
  }

  @Override
  public void update() {
    Journal j = getItem();
    if (j == null) {
      disableComponents();
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
