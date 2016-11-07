package exceptions;

public class ProgramException extends Exception {
	private static final long serialVersionUID = 1L;
	private String message;
	
	public ProgramException(String msg){
		super();
		this.message = msg;
	}
	
	public String getMessage(){
		return new String(this.message);
	}
}
