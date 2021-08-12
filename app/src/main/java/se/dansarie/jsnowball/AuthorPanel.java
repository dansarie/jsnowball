package se.dansarie.jsnowball;

import java.util.function.Consumer;

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

public class AuthorPanel extends SnowballMemberPanel<Author> {

  private JTextField firstName = new JTextField();
  private JTextField lastName = new JTextField();
  private JTextField organizationName = new JTextField();
  private JTextField orcid = new JTextField();
  private JTextArea notes = new JTextArea();
  private JList<Article> articles = new JList<>();

  AuthorPanel() {
    addComponent("First name", firstName);
    addComponent("Last name", lastName);
    addComponent("Organization name", organizationName);
    addComponent("ORCID", orcid);
    addComponent("Notes", new JScrollPane(notes));
    addComponent("Articles", new JScrollPane(articles));
    disableComponents();
  }

  @Override
  public void update() {
    Author a = getItem();
    if (a == null) {
      disableComponents();
      return;
    }
    firstName.setText(a.getFirstName());
    lastName.setText(a.getLastName());
    organizationName.setText(a.getOrgName());
    orcid.setText(a.getOrcId());
    notes.setText(a.getNotes());
    articles.setListData(a.getState().getArticleList(a).toArray(new Article[0]));
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
    }
    throw new IllegalArgumentException("Component not recognized: " + comp.toString());
  }
}
