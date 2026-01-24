package Java.hogwarts;

public class DarkWizard extends Wizard {

  public DarkWizard(String name, int powerLevel) {
    super(name, powerLevel);
  }

  @Override
  public int castSpell(String spellName) {
    if (spellName.equals("Crucio")) {
      return powerLevel * 3;
    } else if (spellName.equals("AvadaKedavra")) {
      return powerLevel * 5;
    } else {
      return powerLevel;
    }
  }
}
