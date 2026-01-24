package Java.hogwarts;

public class HogwartsSpellCombat {

  public static void main(String[] args) {

    Wizard wizard1 = new DarkWizard("Voldemort", 80);
    String spell1 = "AvadaKedavra";

    System.out.println("Wizard: " + wizard1.getName());
    System.out.println("Spell Casted: " + spell1);
    System.out.println("Damage Dealt: " + wizard1.castSpell(spell1));

    System.out.println();

    Wizard wizard2 = new HealingWizard("Hermione", 50);
    String spell2 = "Fireball";

    System.out.println("Wizard: " + wizard2.getName());
    System.out.println("Spell Casted: " + spell2);
    System.out.println("Damage Dealt: " + wizard2.castSpell(spell2));
  }
}
