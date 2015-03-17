package dino.api;

/**
 * This is the base class for DiNo-related exceptions.
 * Normally this exception will not be thrown directly; one of its subclasses will thrown instead.
 * @author Compaq_Owner
 *
 */
public class DiNoException extends Exception {

	private static final long serialVersionUID = -6742406331432498333L;

	public DiNoException() {
	}

	public DiNoException(String arg0) {
		super(arg0);
	}

	public DiNoException(Throwable arg0) {
		super(arg0);
	}

	public DiNoException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
