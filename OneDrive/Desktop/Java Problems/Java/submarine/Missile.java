package Java.submarine;

public abstract class Missile {
  protected String missileId;
  protected int range;

  public Missile(String missileId, int range) {
    this.missileId = missileId;
    this.range = range;
  }

  public abstract boolean launch(int threatLevel, boolean hasClearance);

  public String getMissileId() {
    return missileId;
  }
}
