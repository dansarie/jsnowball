package se.dansarie.jsnowball;

public class ArticlePanel extends SnowballMemberPanel<Article> {

  ArticlePanel() {
    setPanelFields(
        new PanelField("Title", () -> getItem().getTitle()),
        new PanelField("DOI", () -> getItem().getDoi()),
        new PanelField("Year", () -> getItem().getYear()),
        new PanelField("Month", () -> getItem().getMonth()),
        new PanelField("Volume", () -> getItem().getVolume()),
        new PanelField("Issue", () -> getItem().getIssue()),
        new PanelField("Pages", () -> getItem().getPages()),
        new PanelField("Notes", () -> getItem().getNotes())
      );
  }
}
