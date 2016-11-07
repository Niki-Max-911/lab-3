package exceptions;

/**
 * Помилка авторизації
 * @author Maksim
 */
public class AuthenticationException extends Exception {
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
