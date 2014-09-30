import java.awt.Color;
import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.net.Socket;

public class GameServer {

	public static final int QUEUE_SIZE = 300;  // we shouldn't ever hit a backlog this big
	public static final int GAME_TICK_TIME = 15;
	
	private List<Player> players;
	private List<Bullet> bullets;
	private Map map;
	private ServerNetwork network;
	private BlockingQueue<Message> queue;
	private int currentId;
	
	public GameServer(){
		players = new ArrayList<>();
		bullets = new ArrayList<>();
		map = new Map();
		network = new ServerNetwork(this);
		queue = new ArrayBlockingQueue<>(QUEUE_SIZE);
		currentId = 0;
	}
	
	public void run(){	
		Map map2 = Map.loadMap("/resources/map2.txt");
		if(map2 == null){
			System.out.println("Error loading map.");
			return;
		}
		
		map = map2;
		
		try {
			network.listen();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		List<Message> msgs = new ArrayList<>();
		while(true){
			long time = System.nanoTime();
			int count = queue.drainTo(msgs);
			for(int i = 0; i < count; ++i)
				processMsg(msgs.get(i));
			msgs.clear();
			
			processBullets();
			
			// delta time
			time = System.nanoTime() - time;
			time = time / (1000*1000);  // convert nano to milli
			
			try {
				// maintain 60 FPS and try to reduce lag by not sleeping too long if their were lots of messages
				Thread.sleep(Math.max(GAME_TICK_TIME - time, 0));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	// socket unexpectedly closed "u"
	// player socket connected   "s"
	// player connected     "c 12 b 274 384 t matt 3 1 2 80"
	// player name change   "n 12 matt"
	// player disconnected  "d 12"
	// player killed "k 12 20 2 3"
	// player hit "h 12"
	// player moved  "m 12 w t"
	// player rotategun "r 12 1.535"
	// player fired  "f 12"
	// player given id "i 12"
	// player spawned "p 12 274 384"
	public void processMsg(Message msg){
		String[] split = msg.getMsg().split(" ");
		switch(msg.getMsg().charAt(0)){
		case 'u':
			boolean flag = false;
			for(int i = 0; i < players.size(); ++i)
				if(players.get(i).getSocket() == (Socket) msg.getObj()){
					network.removeSocket((Socket) msg.getObj());
					network.sendAll("d " + players.get(i).getId());
					System.out.println("Player " + players.get(i).getId() + " d/c'ed");
					players.remove(i);
					flag = true;
					break;
				}

			if(!flag)
				System.out.println("Non-existent player d/c'ed");

			break;
		case 's':
			Point spawn = map.getSpawnPoint();
			players.add(new Player((int)spawn.getX(), (int)spawn.getY(), 0.0, Color.cyan, false, (Socket) msg.getObj(), currentId++));
			network.send("i " + players.get(players.size()-1).getId(), (Socket) msg.getObj());	
			
			for(int i = 0; i < players.size() - 1; ++i)
				network.send("c " + players.get(i).getInfo(), (Socket) msg.getObj());
			

			break;
		case 'n':
			Player player = getPlayerById(split[1]);
			if(player == null){
				System.out.println("Non-existent player renamed");
				break;
			}
			
			player.setName(split[2]);
			
			if(player.getInited())
				network.sendAll(msg.getMsg());  // player exists - rename
			else {
				network.sendAll("c " + players.get(players.size()-1).getInfo());  // new player connected
				new PlayerHitTask(players.get(players.size()-1).getId()).run();   // now lets spawn this new player
				player.setInited(true);
			}
			
			break;
		case 'm':	
			player = getPlayerById(split[1]);
			if(player == null){
				System.out.println("Non-existent player moved");
				break;
			}
			
			boolean isSprinting = split[3].equals("t");
			player.setSprinting(isSprinting);
			
			switch(split[2]){
			case "w":
				player.moveUp();
				break;
			case "a":
				player.moveLeft();
				break;
			case "s":
				player.moveDown();
				break;
			case "d":
				player.moveRight();
				break;
			}

			network.sendAll(msg.getMsg());
			break;
		case 'f':
			player = getPlayerById(split[1]);
			if(player == null){
				System.out.println("Non-existent player fired");
				break;
			}
			
			bullets.add(new Bullet(player.getGunTipX(), player.getGunTipY(), player.getAngle(), player.getId()));
			network.sendAll(msg.getMsg());
			
			break;
		case 'r':		
			player = getPlayerById(split[1]);
			if(player == null){
				System.out.println("Non-existent player rotated");
				break;
			}
			
			double angle = 0;
			try {
				angle = Double.parseDouble(split[2]);
				player.setAngle(angle);
			} catch (NumberFormatException e){
				System.out.println("Bad angle");
			}
			
			network.sendAll(msg.getMsg());
			
			break;
		default:
			System.out.println("Bad message");
			break;
		}
	}
	
	public void processBullets(){
		int index;
		for(int i = 0; i < bullets.size(); ++i){
			if(map.checkBulletCollision(bullets.get(i)))
				bullets.remove(i);
			else if((index = checkBulletHit(bullets.get(i))) >= 0){
				playerHit(players.get(index), bullets.get(i).getShooter());
				bullets.remove(i);
			} else
				bullets.get(i).move();
		}
	}
	
	// id is shooter
	public void playerHit(Player player, int id){	
		player.setHealth(player.getHealth()-20);
		if(player.getHealth() == 0){
			player.setDead(true);
			
			String str = "";	
			List<Integer> shooters = player.getShooters();
			shooters.remove(new Integer(id));
			for(Integer i : shooters)
				str += " " + i;		
			
			network.sendAll("k " + player.getId() + " " + id + str);
			player.setDeaths(player.getDeaths() + 1);
			
			Player shooter = getPlayerById(id);
			if(shooter != null)
				shooter.setKills(shooter.getKills() + 1);

			for(Integer i : shooters)
				if((shooter = getPlayerById(i)) != null)
					shooter.setAssists(shooter.getAssists() + 1);
				else
					System.out.println("Non-existent assister");
			
			shooters.clear();
			
			new Timer().schedule(new PlayerHitTask(player.getId()), 3*1000);
		} else {
			player.addShooter(id);
			network.sendAll("h " + player.getId());
		}
	}

	class PlayerHitTask extends TimerTask {

		private int id;
		
		public PlayerHitTask(int id){
			this.id = id;
		}
		
		@Override
		public void run() {
			// we do it this way in case they disconnect
			Player player = getPlayerById(id);
			if(player == null)
				return;
			
			Point spawn = map.getSpawnPoint();
			player.setX((int) spawn.getX());
			player.setY((int) spawn.getY());
			player.setDead(false);
			player.setHealth(Player.INIT_HEALTH);
			network.sendAll("p " + player.getId() + " " + player.getX() + " " + player.getY());
		}
	}
	
	// rectangle hit box around player
	public int checkBulletHit(Bullet bullet){
		for(int i = 0; i < players.size(); ++i){
			if(!players.get(i).getDead()  && players.get(i).getHitBox().contains(bullet.getMovedX(), bullet.getMovedY()))
				return i;
		}		
		
		return -1;
	}
	
	// false if msg not added
	public boolean addMsg(Message msg){
		return queue.offer(msg);
	}
	
	public Player getPlayerById(String idStr){
		int id = -1;
		try {
			id = Integer.parseInt(idStr);
		} catch (NumberFormatException e){}
		return getPlayerById(id);
	}
	
	public Player getPlayerById(int id){
		for(Player player : players)
			if(player.getId() == id)
				return player;
		return null;
	}
	
	public static void main(String[] args) {
		GameServer server = new GameServer();
		server.run();
	}
}
