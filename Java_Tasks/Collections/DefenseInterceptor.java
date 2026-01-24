package Java.submarine;

public class DefenseInterceptor extends Missile {

  public DefenseInterceptor(String missileId, int range) {
    super(missileId, range);
  }

  @Override
  public boolean launch(int threatLevel, boolean hasClearance) {
    return threatLevel >= 3;
  }
}
