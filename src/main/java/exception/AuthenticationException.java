package exception;

/**
 * Помилка авторизації
 * @author Maksim
 */
public class AuthenticationException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private String message;
	
	public AuthenticationException(String msg){
		super();
		this.message = msg;
	}
	
	public String getMessage(){
		return new String(this.message);
	}
}
