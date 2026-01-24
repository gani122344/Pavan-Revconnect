package Java.hogwarts;

public class ElementalWizard extends Wizard {

  public ElementalWizard(String name, int powerLevel) {
    super(name, powerLevel);
  }

  @Override
  public int castSpell(String spellName) {
    if (spellName.equals("Fireball")) {
      return powerLevel * 2;
    } else if (spellName.equals("Lightning")) {
      return powerLevel * 3;
    } else {
      return powerLevel;
    }
  }
}
