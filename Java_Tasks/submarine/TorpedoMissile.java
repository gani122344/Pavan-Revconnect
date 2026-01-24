package Java.submarine;

public class TorpedoMissile extends Missile {

  public TorpedoMissile(String missileId, int range) {
    super(missileId, range);
  }

  @Override
  public boolean launch(int threatLevel, boolean hasClearance) {
    return threatLevel >= 5;
  }
}
