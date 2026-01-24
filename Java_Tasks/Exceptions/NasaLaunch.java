package Java.Exceptions;

public class NasaLaunch {

    static class MissionAbortException extends Exception {
        MissionAbortException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static void validatingLaunch(int fuel, String weather)
            throws MissionAbortException {

        try {
            if (fuel < 80) {
                throw new Exception("Fuel is less than 80%");
            }

            if (!"CLEAR".equals(weather)) {
                throw new Exception("Weather is not clear");
            }

        } catch (Exception e) {
            throw new MissionAbortException("Mission aborted", e);
        }
    }

    public static void main(String[] args) {
        try {
            validatingLaunch(60, "STORM");
            System.out.println("Mission launched successfully");

        } catch (MissionAbortException e) {
            System.out.println("Launch failed!");
            System.out.println("Reason: " + e.getCause().getMessage());
            System.out.println("Retrying mission...");
        }
    }
}
