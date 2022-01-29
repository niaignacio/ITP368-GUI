package hw07;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ScrabbleIgnacio extends Application{
	public static void main(String [] args) { launch(args); }
	
	VBox root;
	Scene scene;
	GridPane board;
	FlowPane shared = new FlowPane();
	ScrollPane conversationPane;
	TextArea conversation;
	String theText;
	HBox ctrlPane = new HBox();
	VBox rtSide = new VBox();
	ArrayList<String> letters = new ArrayList<String>(
			Arrays.asList(
				"A", "A", "A", "A", "A", "A", "A", "A", "A", 
				"B", "B", "C", "C", "D", "D", "D", "D", 
				"E", "E", "E", "E", "E", "E", "E", "E", "E", "E", "E", "E", 
				"F", "F", "G", "G", "G", "H", "H",
				"I", "I", "I", "I", "I", "I", "I", "I", "I", "J", "K",
				"L", "L", "L", "L", "M", "M", "N", "N", "N", "N", "N", "N", 
				"O", "O", "O", "O", "O", "O", "O", "O", "P", "P", "Q", 
				"R", "R", "R", "R", "R", "R", "S", "S", "S", "S", 
				"T", "T", "T", "T", "T", "T", "U", "U", "U", "U", "V", "V", 
				"W", "W", "X", "Y", "Y", "Z"
					
	));
	HBox myPieces = new HBox();
	StackPane currTile = new StackPane();
	int state;
	String selectedLetter;
	int numPiecesPlayed = 7;
	Ear oe; //listens for what the other end says
	ServerSocket serverSock; //allows client to connect 
	Socket clientSock; 
	String ip;
	int socketNumber = 12658; //random big number
	BufferedReader myIn=null; //how we read from the socket
	PrintWriter myOut=null; //how we write to the socket
	TextField talker; //where user types a new thing to say
	HBox scoreboard = new HBox();
	int myScore = 0;
	int theirScore = 0;
    Label myScoreLabel = new Label("My Score: " + '\n' + myScore);
    Label theirScoreLabel = new Label("Their Score: " + '\n' + theirScore);
	@Override
	public void start(Stage stage) {
		root = new VBox(); 
		scene = new Scene(root, 600, 600);
		stage.setTitle("Scrabble Ignacio");
		stage.setScene(scene);
		stage.show();
		
		state = 0; //need to click on a letter
		boardInit();
		txtFieldInit(); //create scrollPane
		root.getChildren().add(shared);
		root.getChildren().add(myPieces);
	    Button endTurn = new Button("end turn");
	    ctrlPane.getChildren().add(endTurn);
	    endTurn.setOnAction((ActionEvent e) -> {
	    	System.out.println("end turn");
	    	selectPieces();
	    	send("end turn");
	    });
	    scoreboard.getChildren().add(endTurn);
	    myScoreLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
	    theirScoreLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
	    scoreboard.getChildren().add(myScoreLabel);
	    scoreboard.getChildren().add(theirScoreLabel);
	    root.getChildren().add(scoreboard);
	  // set it so that when you close the application window,
      // it exits BOTH this end and the other end
 	  stage.setOnCloseRequest
 	  ( (WindowEvent w) -> 
 	    { try{ send("byebyebye"); System.exit(0); } 
 	      catch (Exception e) { System.out.println("can't stop"); } 
 	    } 
 	  );
	}
	
	public void boardInit() {
		board = new GridPane();
		shared.getChildren().add(board);
		for(int i = 0; i<10; i++) {
			for(int j = 0; j<10; j++) {
				StackPane sp = new StackPane();
				Rectangle cell = new Rectangle(35, 35);
				cell.setFill(Color.WHITE);
				cell.setStroke(Color.BLACK);
				cell.setStrokeWidth(1);
				sp.getChildren().add(cell);
				board.add(sp, i, j); //GridPane.add(Node, column, row)
				int ii = i;
				int jj = j;
				/*
				 * when you selected a letter, then clicked on a spot on the board,
				 * place that letter on the board
				 * */
				sp.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent m) -> {
					if(state == 1) {
						Label l = new Label(selectedLetter);
						l.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
						sp.getChildren().add(l);
						myPieces.getChildren().remove(currTile);
						numPiecesPlayed++;
						send("played " + selectedLetter + " " + ii + " " + jj);
						myScore++;
						myScoreLabel.setText("My Score: \n" + myScore);
					}
				});
			}
		}
	}
	
	public void txtFieldInit() {
		conversationPane = new ScrollPane();
		rtSide.getChildren().add(conversationPane);
		conversation = new TextArea();
		conversationPane.setContent(conversation);
		conversationPane.setPrefSize(200, 330);
		
		Button host = new Button("host");
		ctrlPane.getChildren().add(host);
		host.setOnAction((ActionEvent e) -> {
			System.out.println("host button clicked.");
			selectPieces();
			startTHISend();
			new SetupHost().start();
		});
		
		Button clientButton = new Button("client");
	      ctrlPane.getChildren().add(clientButton);
	      clientButton.setOnAction((ActionEvent e)->{
	    	  System.out.println("client button clicked.");
	    	  ctrlPane.getChildren().clear();
	    	  Label ipLabel = new Label("IP#?");
	    	  ctrlPane.getChildren().add(ipLabel);
	    	  
	    	  TextField iptf = new TextField("localhost");
	    	  ctrlPane.getChildren().add(iptf);
	    	  iptf.setOnAction(f -> {ip = iptf.getText(); setupClient(); });
	      });
		rtSide.getChildren().add(ctrlPane);
		shared.getChildren().add(rtSide);
	}
   public class SetupHost extends Thread
   {
	   // sets up this to be the first player / host ...
	   // First player opens a socket and announces the
	   // IP and number, then waits (hangs) until 2nd connects.  
   	@Override
	   public void run()
	   {
	   	try
	   	{
	   	   serverSock = new ServerSocket(socketNumber);
	   	   //InetAddress ad = serverSock.getInetAddress();
	   	   //System.out.println(ad); //just prints 0s
	   	   say("socket is open, number="+socketNumber);
	   	   
	   	   // wait for client to make the connection ...
	   	   // the next line hangs until client connects
	   	   clientSock = serverSock.accept(); 
	   	   say("server says client connected ...");
	   	   
	   	   // once connected set up i/o, do handshake.
	   	   // handshake is: server reads one line from client, 
	   	   // then sends one line to client.
	   	   InputStream in = clientSock.getInputStream();
	   	   myIn = new BufferedReader( new InputStreamReader(in));
	   	   String msg = myIn.readLine();
	   	   say("just read="+msg);
	   	   //
	   	   myOut = new PrintWriter( clientSock.getOutputStream(),true);
	   	   String msg2 = "you rang?";
	   	   say("about to write to client= "+msg2);
	   	   myOut.println(msg2);
	   	   myOut.flush();
	         say("just tried to write to client ....");
	         say("Here is what we wrote: "+msg2);
	         
	         // start the Ear thread, which listens for messages
	         // from the other.
	         oe = new Ear();
	         oe.start();     
	   	}
	   	catch(Exception e) 
	   	{ System.out.println("socket open error e="+e); }
	   }
   }
   public void setupClient()
   {
   	say("client setup: starting ...");
   	try
   	{
   		say("about to try to call "+ip+"/"+socketNumber);
   		
   		// connect to server.  Use ip="localhost" for server
   		// on the same machine (for testing)
   		clientSock = new Socket(ip,socketNumber);
   		
   		say("if you see this, client is connected!");
         InputStream in = clientSock.getInputStream();
         myIn = new BufferedReader( new InputStreamReader(in) );
   	   myOut = new PrintWriter( clientSock.getOutputStream(),true);
   	   say("about to greet the server");
         myOut.println("greetings");    
         myOut.flush();
         say("now listening for server reply");
         String line;
         line = myIn.readLine();
         say("read from server = "+line);
         
         startTHISend();
         
         // start the Ear thread, which listens for messages
         // from the other end.
         oe = new Ear();
         oe.start();     
         selectPieces();
   	}
   	catch( Exception e )
   	{ say("client setup error e="+e); }
   }
   public void selectPieces() {
		for(int i = 0; i<numPiecesPlayed; i++) {
			StackPane sp = new StackPane();
			Rectangle r = new Rectangle(40, 40);
			r.setFill(Color.ALICEBLUE); 
			int x = (int)(Math.random()*letters.size());
			String str = letters.get(x);
			letters.remove(x);
			System.out.print(str + " ");
			Label l = new Label(str);
			l.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
			letters.remove(x);
			send("removed " + str);
			sp.getChildren().addAll(r, l);
			myPieces.getChildren().add(sp);
			
			sp.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
				r.setFill(Color.YELLOW);
				state = 1; //you selected a piece to place on the board
				selectedLetter = str;
				currTile = sp;
			});
		}
		numPiecesPlayed = 0;
		System.out.println("remaining pieces: " + letters);
	}
   public void startTHISend()
   {
      ctrlPane.getChildren().clear();
	   talker = new TextField();
	   ctrlPane.getChildren().add(talker);
	   talker.setOnAction
	   ( g-> { String s = talker.getText();
	   	     say( "me: " +s ); 
	   	     send(s);
	   	     talker.setText(""); 
	         } 
	   );
   }
	// Ear is the thread that listens for information coming
   // from the other user.  Go into a loop reading
   // whatever they send and add it to the conversation.
   // If the other end sends "byebyebye", exit this app.
   public class Ear extends Thread
   {
	   @Override
	   public void run()
	   {
	   	while (true)
	   	{
	   		try
	   		{
	   			String s = myIn.readLine(); // hangs for input
		         //say( "you: "+ s );
		         if ( s.equals("byebyebye") ) { 
		        	 System.exit(0); 
		         } else {
		        	 try {
		        		 StringTokenizer st = new StringTokenizer(s);
		        		 String cmd = st.nextToken();
		        		 if(cmd.equals("removed")) {
		        			 String str = st.nextToken(); //str = which letter to remove
		        			 Platform.runLater(()->{
		        				 for(int i = 0; i<letters.size(); i++) {
		        					 if(letters.get(i) == str) {
		        						 letters.remove(i);
		        					 }
		        				 }
		        				 System.out.println(letters);
		        			 });
		        		 } else if (cmd.equals("played")) { //send("played " + selectedLetter + " " + ii + " " + jj);
		        			 String str = st.nextToken(); //which letter was played
		        			 String stri = st.nextToken(); //column
		        			 String strj = st.nextToken(); //row
		        			 Platform.runLater(()->{
		        				 System.out.println("played");
			        			 int i=Integer.parseInt(stri);
			        			 int j = Integer.parseInt(strj);
			        			 Label l = new Label(str);
								 l.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
								 StackPane sp = new StackPane();
								 Rectangle r = new Rectangle(35, 35);
								 r.setFill(Color.WHITE);
								 r.setStroke(Color.BLACK);
								 r.setStrokeWidth(1);
								 sp.getChildren().add(r);
								 sp.getChildren().add(l);
			        			 board.add(sp,  i,  j);
			        			 theirScore++;
			        			 theirScoreLabel.setText("Their Score: \n" + theirScore);
		        			 });
		        		 } else if(cmd.equals("end")) {
		        			 
		        		 }
		        	 } catch(Exception e) {System.out.println("almost ..."+e);}
		         }
		         
	   		}
	   		catch(Exception h)
	   		{ say("couldn't read from the other end"); }
	   	}
	   }
   }
   
// add this string to the conversation.
   public void say(String s) 
   {
   	theText += s + "\n";
   	conversation.setText(theText);

      //System.out.println(s); 
   	
   	conversationPane.setVvalue(1.0); // todo: fix this
   }
   // if the output is established, send s to it.
   public void send( String s )
   {
   	if ( myOut!=null )
   	{
   		myOut.println(s);
   	}
   }
}
