import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


public class ServerNetwork {

	public static final int PORT = 5456;
	
	List<ClientThread> clients;
	ServerSocket listener;
	GameServer server;
	
	public ServerNetwork(GameServer server){
		clients = new ArrayList<>();
		this.server = server;
	}
	
	public void listen() throws IOException {
		listener = new ServerSocket(PORT);
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(true){
					try {
						Socket sock = listener.accept();
						sock.setTcpNoDelay(true);
						server.addMsg(new Message("s", sock));
						ClientThread c = new ClientThread(sock);
						new Thread(c).start();
						addClient(c);
					} catch (IOException e) {
						System.out.println("Error listening on socket");
					}
				}
			}
		}).start();
		
	}
	
	public void addClient(ClientThread c){
		synchronized (clients) {
			clients.add(c);
		}
	}
	
	public void removeSocket(Socket s){
		synchronized (clients) {
			for(int i = 0; i < clients.size(); ++i){
				if(clients.get(i).getSocket() == s){
					clients.get(i).close();
					clients.remove(i);
					break;
				}
			}
		}
	}
	
	public void send(String msg, Socket socket){
		synchronized (clients) {
			for(ClientThread c : clients)
				if(c.getSocket() == socket){
					c.send(msg);
					break;
				}
		}
	}
	
	public void sendAll(String msg){
		synchronized (clients) {
			for(ClientThread c : clients)
				c.send(msg);
		}
	}
	
	class ClientThread implements Runnable {

		private Socket socket;
		private OutputStreamWriter out;
		private boolean shouldClose;
		private BlockingQueue<String> queue;
		public static final int QUEUE_SIZE = 100;  // we shouldn't ever hit a backlog this big
		
		public ClientThread(Socket socket){
			this.socket = socket;
			shouldClose = false;
			queue = new ArrayBlockingQueue<>(QUEUE_SIZE);
		}
		
		public Socket getSocket(){
			return socket;
		}
		
		public void close(){
			shouldClose = true;
			try {
				if(!socket.isClosed()){
					socket.close();	
				}
			} catch (IOException e1) {}
		}
		
		class SendThread implements Runnable {
			
			@Override
			public void run() {
				try {
					while(!shouldClose){
						try {								
							out.write(queue.take() + "\r\n");
							out.flush();
						} catch (InterruptedException e) {}
					}
				} catch (Exception e){
					server.addMsg(new Message("u", socket));
				}
			}
		}
		
		// false if msg not added
		public boolean send(String msg){
			boolean flag = queue.offer(msg);
			if(!flag)
				System.out.println("Queue full");
			return flag;
		}
		
		@Override
		public void run() {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new OutputStreamWriter(socket.getOutputStream());
				
				new Thread(new SendThread()).start();
				
				while(!shouldClose){
					String line = in.readLine();
					
					// if line is null then socket closed
					if(line == null){
						server.addMsg(new Message("u", socket));
						break;
					}
					
					server.addMsg(new Message(line));
				}			
				
			} catch (IOException e) {
				server.addMsg(new Message("u", socket));
			}
		}
	}
}
