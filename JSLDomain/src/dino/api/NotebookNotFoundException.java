package dino.api;

/**
 * This exception is thrown in response to attempts to look up a non-existent notebook.
 * @author Compaq_Owner
 *
 */
public class NotebookNotFoundException extends DiNoException {

	private static final long serialVersionUID = 6636689138638060759L;

	public NotebookNotFoundException() {
	}

	public NotebookNotFoundException(String arg0) {
		super(arg0);
	}

	public NotebookNotFoundException(Throwable arg0) {
		super(arg0);
	}

	public NotebookNotFoundException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
