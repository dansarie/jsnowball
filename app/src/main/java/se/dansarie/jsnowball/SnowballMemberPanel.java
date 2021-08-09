package se.dansarie.jsnowball;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class SnowballMemberPanel<E> extends JPanel {
  
  private E item = null;
  private List<PanelField> fields;
  private List<JTextField> inputFields = new ArrayList<>();

  SnowballMemberPanel() {
    setLayout(new GridBagLayout());
  }

  void setPanelFields(PanelField... fields) {
    removeAll();
    this.fields = Arrays.asList(fields);
    GridBagConstraints gb = new GridBagConstraints();
    gb.gridy = 0;
    //gb.weighty = 1;
    for (PanelField f : this.fields) {
      gb.gridx = 0;
      gb.weightx = 0;
      gb.fill = GridBagConstraints.NONE;
      gb.anchor = GridBagConstraints.NORTHEAST;
      add(new JLabel(f.fieldName), gb);
      gb.gridx = 1;
      gb.weightx = 1;
      gb.fill = GridBagConstraints.HORIZONTAL;
      gb.anchor = GridBagConstraints.NORTHWEST;
      JTextField tf = new JTextField();
      inputFields.add(tf);
      add(tf, gb);
      gb.gridy += 1;
    }
  }

  public void setItem(E item) {
    this.item = Objects.requireNonNull(item);
    for (int i = 0; i < fields.size(); i++) {
      inputFields.get(i).setText(fields.get(i).fieldValueSupplier.get());
    }
  }

  public E getItem() {
    return item;
  }

  static class PanelField {
    public final String fieldName;
    public final Supplier<String> fieldValueSupplier;

    PanelField(String fieldName, Supplier<String> fieldValueSupplier) {
      this.fieldName = Objects.requireNonNull(fieldName);
      this.fieldValueSupplier = Objects.requireNonNull(fieldValueSupplier);
    }
  }
}
