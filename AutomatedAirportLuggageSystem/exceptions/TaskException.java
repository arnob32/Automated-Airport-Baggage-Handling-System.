package exceptions;

public class TaskException extends CustomExceptions {

    private static final long serialVersionUID = 1L;

    public TaskException(String message) {
        super("Task Error: " + message);
    }
}
