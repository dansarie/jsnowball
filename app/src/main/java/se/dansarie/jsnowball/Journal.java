package se.dansarie.jsnowball;

public class Journal extends SnowballStateMember {
  private String name;
  private String issn;

  public Journal(SnowballState state) {
    super(state);
  }

  public String getIssn() {
    return issn;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setIssn(String issn) {
    this.issn = issn;
  }

  @Override
  public int compareTo(SnowballStateMember other) {
    Journal o = (Journal)other;
    if (getName() == null) {
      if (o.getName() == null) {
        return 0;
      }
      return -1;
    }
    return getName().compareTo(o.getName());
  }
}
