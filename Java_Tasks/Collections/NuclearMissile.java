package Java.submarine;

public class NuclearMissile extends Missile {

  public NuclearMissile(String missileId, int range) {
    super(missileId, range);
  }

  @Override
  public boolean launch(int threatLevel, boolean hasClearance) {
    return threatLevel >= 9 && hasClearance;
  }
}
