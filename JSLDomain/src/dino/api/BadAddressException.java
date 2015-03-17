package dino.api;

/**
 * This exception is thrown in response to invalid URLs, host:port specifications, and destination names
 * @author Don Swartwout
 *
 */
public class BadAddressException extends DiNoException {

	private static final long serialVersionUID = 2196474181557752023L;

	public BadAddressException() {
	}

	public BadAddressException(String arg0) {
		super(arg0);
	}

	public BadAddressException(Throwable arg0) {
		super(arg0);
	}

	public BadAddressException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
