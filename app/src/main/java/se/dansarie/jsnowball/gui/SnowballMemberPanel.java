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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
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
  protected boolean updating = false;

  SnowballMemberPanel() {
    setLayout(new GridBagLayout());
    gb.gridy = 0;
    gb.insets = new Insets(5, 5, 5, 5);
  }

  protected void addComponent(String caption, JComponent comp) {
    captions.add(caption);
    components.add(comp);

    JComponent childComp = comp;
    if (comp instanceof JScrollPane) {
      childComp = (JComponent)(((JScrollPane)comp).getViewport().getView());
      Dimension dim = new Dimension(250, 100);
      comp.setPreferredSize(dim);
    }

    gb.fill = GridBagConstraints.NONE;
    gb.anchor = GridBagConstraints.NORTHEAST;
    gb.gridx = 0;
    gb.weightx = 0;
    add(new JLabel(caption), gb);

    gb.weighty = 0;
    gb.fill = GridBagConstraints.HORIZONTAL;
    if (childComp instanceof JTextComponent) {
      JTextComponent tc = (JTextComponent)childComp;
      Document doc = tc.getDocument();
      doc.addDocumentListener(this);
      doc.putProperty("parentcomponent", tc);
    } else if (childComp instanceof JList) {
      ((JList<?>)childComp).setVisibleRowCount(6);
      gb.weighty = 1;
      gb.fill = GridBagConstraints.BOTH;
    } else if (childComp instanceof JButton) {
      gb.fill = GridBagConstraints.NONE;
    }
    gb.anchor = GridBagConstraints.NORTHWEST;
    gb.gridx = 1;
    gb.weightx = 1;
    add(comp, gb);
    gb.gridy += 1;
  }

  public void setItem(E item) {
    this.item = item;
    updating = true;
    update();
    updating = false;
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
    if (getItem() == null || updating) {
      return;
    }
    Document doc = ev.getDocument();
    JTextComponent pc = (JTextComponent)doc.getProperty("parentcomponent");
    try {
      Consumer<String> updater = textComponentUpdated(pc);
      updater.accept(doc.getText(0, doc.getLength()));
    } catch (BadLocationException ex) {
      LogWindow.getInstance().addThrowable(ex);
      throw new RuntimeException(ex);
    }
  }

  public abstract void update();
  protected abstract Consumer<String> textComponentUpdated(JTextComponent comp);
}
