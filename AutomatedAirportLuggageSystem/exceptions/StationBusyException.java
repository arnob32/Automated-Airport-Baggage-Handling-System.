package exceptions;


public class StationBusyException extends CustomExceptions {

    private static final long serialVersionUID = 1L;

    public StationBusyException(String stationName) {
        super("Charging Station '" + stationName + "' is currently occupied.");
    }
}
