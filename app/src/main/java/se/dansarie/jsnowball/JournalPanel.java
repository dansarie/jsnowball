package se.dansarie.jsnowball;

public class JournalPanel extends SnowballMemberPanel<Journal> {

  JournalPanel() {
    setPanelFields(
        new PanelField("Name", () -> getItem().getName()),
        new PanelField("ISSN", () -> getItem().getIssn()),
        new PanelField("Notes", () -> getItem().getNotes())
      );
  }
}
