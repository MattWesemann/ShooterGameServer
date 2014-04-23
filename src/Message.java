
public class Message {

	private String msg;
	private Object obj;
	
	public Message(String msg){
		this(msg, null);
	}
	
	public Message(String msg, Object obj){
		this.msg = msg;
		this.obj = obj;
	}
	
	public String getMsg(){
		return msg;
	}
	
	public Object getObj(){
		return obj;
	}
}
