package se.dansarie.jsnowball;

public class AuthorPanel extends SnowballMemberPanel<Author> {

  AuthorPanel() {
    setPanelFields(
        new PanelField("First name", () -> getItem().getFirstName()),
        new PanelField("Last name", () -> getItem().getLastName()),
        new PanelField("Organization name", () -> getItem().getOrgName()),
        new PanelField("ORCID", () -> getItem().getOrcId()),
        new PanelField("Notes", () -> getItem().getNotes())
      );
  }
}
