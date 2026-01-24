package Java.submarine;

public class SubmarineLaunchSystem {

  public static void main(String[] args) {

    // Test Case 1
    Missile missile1 = new NuclearMissile("NUC-778", 10000);
    int threatLevel1 = 8;
    boolean clearance1 = true;

    System.out.println("Missile ID: " + missile1.getMissileId());

    if (missile1.launch(threatLevel1, clearance1)) {
      System.out.println("Launch Status: APPROVED");
      System.out.println("Nuclear missile launched");
    } else {
      System.out.println("Launch Status: DENIED");
      System.out.println("Reason: Threat level insufficient for nuclear launch");
    }

    System.out.println();

    // Test Case 2
    Missile missile2 = new DefenseInterceptor("DEF-109", 2000);
    int threatLevel2 = 4;
    boolean clearance2 = false;

    System.out.println("Missile ID: " + missile2.getMissileId());

    if (missile2.launch(threatLevel2, clearance2)) {
      System.out.println("Launch Status: APPROVED");
      System.out.println("Interceptor launched successfully");
    } else {
      System.out.println("Launch Status: DENIED");
    }
  }
}
