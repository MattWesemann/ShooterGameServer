import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.Rectangle;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Player {

	public static final int HEIGHT = 45;
	public static final int WIDTH = 45;
	public static final int GUN_WIDTH = 10;
	public static final int GUN_LENGTH = 50;
	public static final int INIT_HEALTH = 100;
	
	private int id;
	private int x;
	private int y;
	private double angle;
	private Color color;
	private boolean isMe;
	private Socket socket;
	private boolean isDead;
	private String name;
	private boolean inited;  // if the player has finished its initialization handshake
	private int kills;
	private int deaths;
	private int assists;
	private int health;
	private int distance;
	List<Integer> shooters;
	
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
		name = "";
		inited = false;
		kills = 0;
		deaths = 0;
		assists = 0;
		health = INIT_HEALTH;
		distance = 3;
		shooters = new ArrayList<>();
	}

	public int getId(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name= name;
	}
	
	public boolean getInited(){
		return inited;
	}
	
	public void setInited(boolean inited){
		this.inited = inited;
	}
	
	public int getScore(){
		return kills*50 + assists*10;
	}
	
	public int getKills(){
		return kills;
	}
	
	public void setKills(int kills){
		this.kills = kills;
	}
	
	public int getDeaths(){
		return deaths;
	}
	
	public void setDeaths(int deaths){
		this.deaths = deaths;
	}
	
	public int getAssists(){
		return assists;
	}
	
	public void setAssists(int assists){
		this.assists = assists;
	}
	
	public int getHealth(){
		return health;
	}
	
	public void setHealth(int health){
		this.health = health;
	}
	
	public void setSprinting(boolean sprinting){
		this.distance = sprinting ? 5 : 3;
	}
	
	
	public List<Integer> getShooters(){
		return shooters;
	}
	
	public void clearShooters(){
		shooters.clear();
	}
	
	public void addShooter(int shooter){
		if(!shooters.contains(shooter))
			shooters.add(shooter);
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
		y -= distance;
	}
	
	public void moveDown(){
		y += distance;
	}
	
	public void moveLeft(){
		x -= distance;
	}
	
	public void moveRight(){
		x += distance;
	}
	
	public Rectangle getHitBox(){
		return new Rectangle(x - WIDTH/2, y - HEIGHT/2, WIDTH, HEIGHT);
	}
	
	public String getInfo(){
		// b is replacing color.toString() which is an incredibly ugly string
		return id + " " + "b" + " " + x + " " + y + (isDead ? " t " : " f ") + name + 
				" " + " " + kills + " " + deaths + " " + assists + " " + health;
	}
	
	public Socket getSocket(){
		return socket;
	}
}
