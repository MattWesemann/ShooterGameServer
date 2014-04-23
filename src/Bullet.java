import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;


public class Bullet {

	private double x;
	private double y;
	private double angle;
	
	private int WIDTH = 5;
	private int HEIGHT = 5;
	
	public static final int SPEED = 7;
	public static final int FIRE_RATE = 5; // shots per second
	
	public Bullet(){
		this(0, 0, 0);
	}
	
	public Bullet(int x, int y, double angle){
		this.x = x;
		this.y = y;
		this.angle = angle;
	}
	
	// for collision detection
	public double getMovedX(){
		return SPEED*Math.cos(angle) + x;
	}
	
	// for collision detection
	public double getMovedY(){
		return SPEED*Math.sin(angle) + y;
	}
	
	public void move(){
		x = getMovedX();
		y = getMovedY();
	}
	
	public void draw(Graphics2D g, Player me, int width, int height){
		g.translate(x - me.getX() + width/2, y - me.getY() + height/2);
		g.rotate(angle);
		g.setColor(Color.black);
		g.fillOval(-WIDTH/2, -HEIGHT/2, WIDTH, HEIGHT);
		g.setTransform(new AffineTransform());
	}
	
	public int getX(){
		return (int) x;
	}
	
	public int getY(){
		return (int) y;
	}
}
