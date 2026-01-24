package Java.hogwarts;

public class HealingWizard extends Wizard {

  public HealingWizard(String name, int powerLevel) {
    super(name, powerLevel);
  }

  @Override
  public int castSpell(String spellName) {
    if (spellName.equals("Heal")) {
      return powerLevel * 2;
    } else if (spellName.equals("Revive")) {
      return powerLevel * 4;
    } else {
      return 0;
    }
  }
}
