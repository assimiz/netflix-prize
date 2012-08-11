package dbanalysis;

public class ValueOverflowException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ValueOverflowException(int value) {
		super("Value overflowed " + value);
	}
	
	

}
