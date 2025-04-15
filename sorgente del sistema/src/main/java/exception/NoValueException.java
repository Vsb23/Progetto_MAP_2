package exception;

/**
 * eccezione lanciata quando l'operatore aggregato non da risultati.
 */
public class NoValueException extends Exception {
	public NoValueException(final String s) {
		super(s);
	}
}
