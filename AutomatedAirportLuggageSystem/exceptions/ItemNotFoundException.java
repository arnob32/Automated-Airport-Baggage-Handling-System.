package exceptions;


public class ItemNotFoundException extends CustomExceptions {

    private static final long serialVersionUID = 1L;

    public ItemNotFoundException(int id) {
        super("Baggage with ID " + id + " does not exist.");
    }
}

