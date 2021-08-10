package se.dansarie.jsnowball;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

public abstract class SnowballMemberPanel<E> extends JPanel implements DocumentListener {

  private E item = null;
  protected GridBagConstraints gb = new GridBagConstraints();
  private List<String> captions = new ArrayList<String>();
  private List<JComponent> components = new ArrayList<JComponent>();

  SnowballMemberPanel() {
    setLayout(new GridBagLayout());
    gb.gridy = 0;
  }

  protected void addComponent(String caption, JComponent comp) {
    captions.add(caption);
    components.add(comp);

    JComponent childComp = comp;
    if (comp instanceof JScrollPane) {
      childComp = (JComponent)(((JScrollPane)comp).getViewport().getView());
    }

    if (childComp instanceof JTextComponent) {
      JTextComponent tc = (JTextComponent)childComp;
      Document doc = tc.getDocument();
      doc.addDocumentListener(this);
      doc.putProperty("parentcomponent", tc);
    }

    gb.fill = GridBagConstraints.NONE;
    gb.anchor = GridBagConstraints.NORTHEAST;
    gb.gridx = 0;
    gb.weightx = 0;
    add(new JLabel(caption), gb);
    gb.fill = GridBagConstraints.HORIZONTAL;
    gb.anchor = GridBagConstraints.NORTHWEST;
    gb.gridx = 1;
    gb.weightx = 1;
    add(comp, gb);
    gb.gridy += 1;
  }

  public void setItem(E item) {
    this.item = item;
    update();
  }

  public E getItem() {
    return item;
  }

  protected String getCaption(int idx) {
    return captions.get(idx);
  }

  protected JComponent getField(int idx) {
    return components.get(idx);
  }

  protected int getNumFields() {
    return components.size();
  }

  protected void enableComponents() {
    enableDisableComponents(true);
  }

  protected void disableComponents() {
    enableDisableComponents(false);
  }

  private void enableDisableComponents(boolean enable) {
    for (JComponent c : components) {
      if (c instanceof JScrollPane) {
        c.setEnabled(enable);
        c = (JComponent)(((JScrollPane)c).getViewport().getView());
      }
      c.setEnabled(enable);
    }
  }

  @Override
  public void changedUpdate(DocumentEvent ev) {
    documentUpdated(ev);
  }

  @Override
  public void insertUpdate(DocumentEvent ev) {
    documentUpdated(ev);
  }

  @Override
  public void removeUpdate(DocumentEvent ev) {
    documentUpdated(ev);
  }

  protected void documentUpdated(DocumentEvent ev) {
    if (getItem() == null) {
      return;
    }
    Document doc = ev.getDocument();
    JTextComponent pc = (JTextComponent)doc.getProperty("parentcomponent");
    try {
      Consumer<String> updater = textComponentUpdated(pc);
      updater.accept(doc.getText(0, doc.getLength()));
    } catch (BadLocationException ex) {
      throw new RuntimeException(ex);
    }
  }

  public abstract void update();
  protected abstract Consumer<String> textComponentUpdated(JTextComponent comp);
}
