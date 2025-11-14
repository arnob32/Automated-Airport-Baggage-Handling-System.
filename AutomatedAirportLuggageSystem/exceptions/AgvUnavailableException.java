package exceptions;



public class AgvUnavailableException extends CustomExceptions {

    private static final long serialVersionUID = 1L;

    public AgvUnavailableException(String agvName) {
        super("AGV '" + agvName + "' is currently busy or unavailable.");
    }
}