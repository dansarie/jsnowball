package se.dansarie.jsnowball;

public class Journal extends SnowballStateMember {
  private String name;
  private String issn;

  Journal() {
  }

  public String getIssn() {
    return issn;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
    state.updated(this);
  }

  public void setIssn(String issn) {
    this.issn = issn;
    state.updated(this);
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
    if (o.getName() == null) {
      return 1;
    }
    return getName().compareToIgnoreCase(o.getName());
  }

  @Override
  public String toString() {
    return getName();
  }
}
