import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.Rectangle;
import java.net.Socket;

public class Player {

	public static final int HEIGHT = 45;
	public static final int WIDTH = 45;
	public static final int GUN_WIDTH = 10;
	public static final int GUN_LENGTH = 50;
	
	private int id;
	private int x;
	private int y;
	private double angle;
	private Color color;
	private boolean isMe;
	private Socket socket;
	private boolean isDead;
	
	public enum Direction {
		Up, Down, Left, Right
	}
	
	public Player(){
		this(0, 0, 0, Color.blue, false, null, -1);
	}
	
	public Player(int x, int y, double angle, Color color, boolean isMe, Socket socket, int id){
		this.x = x;
		this.y = y;
		this.angle = angle;
		this.color = color;
		this.isMe = isMe;
		this.socket = socket;
		this.id = id;
		isDead = false;
	}

	public int getId(){
		return id;
	}
	
	public void computeAngle(Point p, int width, int height){
		angle = Math.atan2(p.getY() - height/2, p.getX() - width/2);
	}
	
	public void setAngle(double angle){
		this.angle = angle;
	}
	
	public boolean getDead(){
		return isDead;
	}
	
	public void setDead(boolean isDead){
		this.isDead = isDead;
	}
	
	// stacks corresponds to how hot the gun is
	public void draw(Graphics2D g, int width, int height, int stacks){			
		int xDraw = x;
		int yDraw = y;
		
		if(isMe){
			xDraw = width / 2;
			yDraw = height / 2;
		}
		
		g.setColor(color);
		g.fillOval(xDraw-WIDTH/2, yDraw-HEIGHT/2, WIDTH, HEIGHT);
		
		g.setColor(new Color(stacks/100.0f, 0f, 0f));
		
		g.translate((width/2), (height/2));
		g.rotate(angle);
		g.fillRect(-GUN_WIDTH/2, -GUN_WIDTH/2, GUN_LENGTH, GUN_WIDTH);
		g.setTransform(new AffineTransform());
	}
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
	
	public void setX(int x){
		this.x = x;
	}
	
	public void setY(int y){
		this.y = y;
	}
	
	public double getAngle(){
		return angle;
	}
	
	public int getGunTipX(){
		return (int) (Math.cos(angle)*GUN_LENGTH + x);
	}
	
	public int getGunTipY(){
		return (int) (Math.sin(angle)*GUN_LENGTH + y);
	}
	
	public void moveUp(){
		y -= 3;
	}
	
	public void moveDown(){
		y += 3;
	}
	
	public void moveLeft(){
		x -= 3;
	}
	
	public void moveRight(){
		x += 3;
	}
	
	public Rectangle getHitBox(){
		return new Rectangle(x - WIDTH/2, y - HEIGHT/2, WIDTH, HEIGHT);
	}
	
	public String getInfo(){
		// b is replacing color.toString() which is an incredibly ugly string
		return id + " " + "b" + " " + x + " " + y + (isDead ? " t" : " f");
	}
	
	public Socket getSocket(){
		return socket;
	}
}
