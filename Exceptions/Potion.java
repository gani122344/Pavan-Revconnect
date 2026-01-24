package Java.Exceptions;

public class Potion {

    static class PotionExplosionException extends Exception {
        public PotionExplosionException(String message) {
            super(message);
        }
    }

    public static void brewPotion(int dragonBloodDrops)
            throws PotionExplosionException {

        if (dragonBloodDrops > 5) {
            throw new PotionExplosionException(
                    "Potion exploded because of too much Dragon Blood: " + dragonBloodDrops);
        }
        System.out.println("Potion successfully brewed with "
                + dragonBloodDrops + " drops.");
    }

    public static void main(String[] args) {
        try {
            brewPotion(6); 
        } catch (PotionExplosionException e) {
            System.out.println("Brewing failed: " + e.getMessage());
        }
    }
}
