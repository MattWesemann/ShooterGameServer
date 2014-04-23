import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Map {

	public static final int GRID_SIZE = 50; // 50x50 squares
	
	enum GridType {
		Empty, Wall
	}
	
	List<List<GridType>> grid;
	
	List<Point> starting;
	Random random;
	
	public Map(){
		random = new Random();
		grid = new ArrayList<>();
		starting = new ArrayList<>();
	}
	
	static public Map loadMap(String name){
		Map map = new Map();
		if(!map.load(name))
			return null;
		return map;
	}
	
	// load a map from this file
	// false if error
	private boolean load(String name) {	
		InputStream stream = Map.class.getClass().getResourceAsStream(name);
		if(stream == null)
			return false;
		try (BufferedReader in = new BufferedReader(new InputStreamReader(stream));) {		
			String line;
			while((line = in.readLine()) != null){
				grid.add(new ArrayList<GridType>());
				if(!processLine(line))
					return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private boolean processLine(String line){
		for(int i = 0; i < line.length(); ++i){
			char c = line.charAt(i);
			if(c != ' ' && c != '|' && c != '-' && c != 'x')
				return false;
			
			List<GridType> list = grid.get(grid.size()-1);
			
			if(c == ' ')
				list.add(GridType.Empty);
			else if(c == 'x'){
				starting.add(new Point(i*GRID_SIZE, (grid.size()-1)*GRID_SIZE));
				list.add(GridType.Empty);
			} else
				list.add(GridType.Wall);		
		}
		
		return true;
	}
	
	public Point getSpawnPoint(){
		return starting.get(random.nextInt(starting.size()));
	}
	
	public boolean checkBulletCollision(Bullet bullet){
		
		int xIndex = (int) (bullet.getMovedX() / GRID_SIZE);
		int yIndex = (int) (bullet.getMovedY() / GRID_SIZE);
		
		if(xIndex < 0 || yIndex < 0)
			return true;
		
		if(grid.size() <= yIndex || grid.get(yIndex).size() <= xIndex)
			return true;
		
		if(grid.get(yIndex).get(xIndex) == GridType.Wall)
			return true;
		
		return false;
	}
	
	// width and height of player
	public boolean checkCollision(Player.Direction direction, int x, int y, int width, int height){
		int xIndex = 0;
		int yIndex = 0;
		
		switch(direction){
		case Up:
			xIndex = x / GRID_SIZE;
			yIndex = (y - height/2) / GRID_SIZE;
			break;
		case Down:
			xIndex = x / GRID_SIZE;
			yIndex = (y + height/2) / GRID_SIZE;
			break;
		case Left:
			xIndex = (x - width/2) / GRID_SIZE;
			yIndex = y / GRID_SIZE;
			break;
		case Right:
			xIndex = (x + width/2) / GRID_SIZE;
			yIndex = y / GRID_SIZE;
			break;
		}
				
		if(grid.get(yIndex).get(xIndex) == GridType.Wall)
			return true;
		
		return false;
	}
	
	public void draw(Graphics2D g, int x, int y, GridType type){
		Color c = Color.black;
		if(type == GridType.Empty)
			c = Color.white;
		
		g.setColor(c);
		g.fillRect(x, y, GRID_SIZE, GRID_SIZE);
	}
	
	// draw map centered around this point
	// height and width of the window
	public void draw(Graphics2D g, int x, int y, int width, int height){
		int xIndex = x / GRID_SIZE;
		int yIndex = y / GRID_SIZE;
		
		int xOff = x % GRID_SIZE;
		int yOff = y % GRID_SIZE;
		
		int xTotal = width / GRID_SIZE;
		int yTotal = height / GRID_SIZE;
		
		int xStart = xIndex - xTotal/2;
		int yStart = yIndex - yTotal/2;
		
		int xEnd = xTotal + xStart + 1;
		int yEnd = yTotal + yStart + 1;
		
		int xCurrent = 0;
		int yCurrent = 0;
		
		if(yStart < 0){
			yCurrent = -yStart * GRID_SIZE;
			yStart = 0;
		}
			
		for(int i = yStart; i < yEnd && i < grid.size(); ++i){
			int j = xStart;
			xCurrent = 0;
			if(xStart < 0){
				xCurrent = -xStart * GRID_SIZE;
				j = 0;
			}
			
			for(; j < xEnd && j < grid.get(i).size(); ++j){
				draw(g, xCurrent - xOff, yCurrent - yOff, grid.get(i).get(j));				
				xCurrent += GRID_SIZE;
			}
			
			yCurrent += GRID_SIZE;
		}		
	}
}
