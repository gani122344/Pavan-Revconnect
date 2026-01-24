package Java.hogwarts;

public abstract class Wizard {
  protected String name;
  protected int powerLevel;

  public Wizard(String name, int powerLevel) {
    this.name = name;
    this.powerLevel = powerLevel;
  }

  public abstract int castSpell(String spellName);

  public String getName() {
    return name;
  }
}
