//MinesweeperIgnacio.java
import java.util.ArrayList;

import javafx.animation.PathTransition;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MinesweeperIgnacio extends Application {
	public static void main(String[] args) {
		launch(args);
	}
	
	HBox root;
	Scene scene;
	double sceneHeight = 500;
	double sceneWidth = 600;
	double gameWidth = 400;
	double rootSpacing = 50.0;
	double ctrlPaneWidth = sceneWidth-gameWidth-rootSpacing;
	Cell[][] world;
	int worldSize = 9; //beginner = 9, intermediate = 16, expert = 30
	int numMines = 10; //beginner = 10, intermediate = 40, expert = 99
	GridPane worldPane;
	double scale;
	double topHeight;
	ArrayList<Image> numbers = new ArrayList<Image>();
	Integer numRemBombs = 10;
	Integer numFoundBombs = 0;
	int state = 0; //0 = uncovering things, 1 = setting flags
	Text remainingNum;
	boolean inGame = true; //use to know when we lose the game
	int numUncovered = 0; //use to help know when we win the game
	int numNeeded;
	Text status;
	HBox hbox = new HBox();
	VBox ctrl;
	VBox game;
	StackPane gameStack;
	String mode; //easy by default
	StackPane remaining; //#of bombs on the field - #of flags placed
	Rectangle remainingRec = new Rectangle(scale*2, scale); //background rectangle for remaining StackPane
//	double flagStartX;
//	double flagStartY;
    HBox middle = new HBox();
	//StackPane flagBtn = new StackPane();
//	PathTransition pt;
	ArrayList<Flag> flags = new ArrayList<Flag>();
	@Override
	public void start(Stage stage) {
		root = new HBox(); 
		scene = new Scene(root, sceneWidth, sceneHeight);
		stage.setTitle("Minsweeper Ignacio");
		stage.setScene(scene);
		stage.show();
		
		mode = "easy"; //easy by default
		remaining = new StackPane();
		remainingRec = new Rectangle(scale*2, scale);
		ctrl = new VBox();
		game = new VBox();
		gameStack = new StackPane();
		worldPane = new GridPane();
		scale = gameWidth/worldSize;
		topHeight = scale;
		initCtrlPane();
		initImages();
		game.getChildren().add(hbox);
		game.getChildren().add(worldPane);
		status = new Text("You're doing good!");
		status.setFont(Font.font("Courier", FontWeight.BOLD, 30));
		root.setSpacing(rootSpacing);
		game.getChildren().add(status);
		gameStack.getChildren().add(game);
		root.getChildren().add(ctrl);
		root.getChildren().add(gameStack);
		reset();
	}
	public void reset() {
		numUncovered = 0;
		numNeeded = worldSize*worldSize-numMines;
		System.out.println("num needed = " + numNeeded);
		System.out.println("mode = " + mode);
		for(Flag f:flags) {
			gameStack.getChildren().remove(f);
		}
		scale = gameWidth/worldSize;
		System.out.println("scale = " + scale);
		worldPane.getChildren().clear();
		remaining.getChildren().clear();
		initTop();
		initBoard();
		assignNeighbors();
		coverEverything();
		status.setText("You're doing good!");
	}
	public void initCtrlPane() {
		ctrl.setSpacing(10);
		//ctrl.setAlignment(Pos.TOP_CENTER);
		ArrayList<Button> btns = new ArrayList<Button>();
		Button easy = new Button ("Easy");
		Button inter = new Button("Intermediate");
		Button hard = new Button("Hard");
		btns.add(easy);
		btns.add(inter);
		btns.add(hard);
		easy.setOnAction((ActionEvent e) -> {
			mode = "easy";
			numRemBombs = 10;
			numMines = 10;
			worldSize = 9;
			reset();
		});
		inter.setOnAction((ActionEvent e) -> {
			mode = "inter";
			numRemBombs = 40;
			numMines = 40;
			worldSize = 16;
			reset();
		});
		hard.setOnAction((ActionEvent e) -> {
			mode = "hard";
			numRemBombs = 99;
			worldSize = 30;
			numMines = 99;
			worldPane.getChildren().clear();
			reset();
		});
		for(int i = 0; i<btns.size(); i++) {
			btns.get(i).setPrefWidth(ctrlPaneWidth);
			ctrl.getChildren().add(btns.get(i));
		}
		
		Text instructions = new Text();
		instructions.setText("Objective: reveal everything but the mines! \n \n "
				+ "Removing a flag: left click on the flag you wish to remove. \n \n"
				+ "Resetting the game: click on the smiley face at the top or click on any of the difficulty buttons.");
		instructions.setWrappingWidth(ctrlPaneWidth);
		
		Text obj = new Text("Objective: ");
		Text obj2 = new Text("Reveal everything but the mines!");
		Text flg = new Text("Removing a flag: ");
		Text flg2 = new Text("Left click on the flag you wish to remove.");
		Text rst = new Text("Resetting the game: ");
		Text rst2 = new Text("Click on the smiley face at the top or click on any of the difficulty buttons.");
		obj.setFont(Font.font("Courier", FontWeight.BOLD, 12));
		flg.setFont(Font.font("Courier", FontWeight.BOLD, 12));
		rst.setFont(Font.font("Courier", FontWeight.BOLD, 12));
		obj2.setWrappingWidth(ctrlPaneWidth);
		flg2.setWrappingWidth(ctrlPaneWidth);
		rst2.setWrappingWidth(ctrlPaneWidth);
		ctrl.getChildren().add(obj);
		ctrl.getChildren().add(obj2);
		ctrl.getChildren().add(flg);
		ctrl.getChildren().add(flg2);
		ctrl.getChildren().add(rst);
		ctrl.getChildren().add(rst2);
	}
	public void initImages() {
		Image zero = new Image("0.png");
	    Image one = new Image("1.png");
	    Image two = new Image("2.png");
	    Image three = new Image("3.png");
	    Image four = new Image("4.png");
	    Image five = new Image("5.png");
	    Image six = new Image("6.png");
	    Image seven = new Image("7.png");
	    Image eight = new Image("8.png");
	    numbers.add(zero);
	    numbers.add(one);
	    numbers.add(two);
	    numbers.add(three);
	    numbers.add(four);
	    numbers.add(five);
	    numbers.add(six);
	    numbers.add(seven);
	    numbers.add(eight);
	}
	public void initTop() {
		remainingNum = new Text(numRemBombs.toString());
		remainingNum.setFont(Font.font("Courier", FontWeight.BOLD, 25));
		remainingNum.setFill(Color.RED);
		remaining.getChildren().addAll(remainingRec, remainingNum);
		Image flag = new Image("flagged.png");
	    ImageView flagiv = new ImageView(flag);
	    flagiv.addEventHandler(MouseEvent.MOUSE_CLICKED,(MouseEvent m)->
		{ 
			if(state == 0) {
				state = 1;
				flagiv.setOpacity(0.5);
			} else {
				state = 0;
				flagiv.setOpacity(1.0);
			}
			System.out.println("curr state = " +  state);
		});
	    flagiv.setFitHeight(topHeight);
	    flagiv.setFitWidth(topHeight);
	    Image face = new Image("smiley.png");
	    ImageView faceiv = new ImageView(face);
	    faceiv.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent m) -> {
	    	if(mode.equals("easy")) {
	    		numRemBombs = 10;
				numMines = 10;
				worldSize = 9;
	    	} else if(mode.equals("inter")) {
	    		numMines = 40;
	    		numRemBombs = 40;
				worldSize = 16;
	    	} else {
	    		numMines = 99;
	    		numRemBombs = 99;
	    		worldSize = 30;
	    	}
	    	reset();
	    });
	    faceiv.setFitHeight(topHeight);
	    faceiv.setFitWidth(topHeight);
	    middle.getChildren().clear();
	    middle.getChildren().add(faceiv);
	    middle.getChildren().add(flagiv);
	    hbox.getChildren().clear();
	    hbox.getChildren().add(remaining);
	    hbox.getChildren().add(middle);
	    hbox.setAlignment(Pos.CENTER);
	    hbox.setSpacing(1.5*scale);
	}
	public void initBoard() {
		game.getChildren().remove(worldPane);
		game.getChildren().remove(status);
		world = new Cell[worldSize][worldSize];
		System.out.println("worldSize = " + worldSize);
		for(int i = 0; i<worldSize; i++) {
			for(int j = 0; j<worldSize; j++) {
				Cell ce = new Cell(scale, i*scale, j*scale, false, i, j);
				worldPane.add(ce, i, j);
				world[i][j] = ce;
			}
		}
		game.getChildren().add(worldPane);
		while(numMines > 0) { //place mines in random boxes in the world
			int i = (int)(Math.random()*worldSize);
			int j = (int)(Math.random()*worldSize);
			if(!world[i][j].getHasBomb()) { //if this cell doesn't already have a bomb
				world[i][j].setHasBomb(true);
				world[i][j].placeBomb(scale);
				numMines--;
			}
		}
		game.getChildren().add(status);
	}
	public void assignNeighbors() {
		for ( int i=0; i<worldSize; i++ ){
	    	for ( int j=0; j<worldSize; j++ ){
		    	Cell ce = world[i][j];
		    	ce.addNeighbor( getCell(i-1,j-1) );
		    	ce.addNeighbor( getCell(i  ,j-1) );
		    	ce.addNeighbor( getCell(i+1,j-1) );
		    	ce.addNeighbor( getCell(i-1,j  ) );
		    	ce.addNeighbor( getCell(i+1,j  ) );
		    	ce.addNeighbor( getCell(i-1,j+1) );
		    	ce.addNeighbor( getCell(i  ,j+1) );
		    	ce.addNeighbor( getCell(i+1,j+1) );
		    	if(!ce.getHasBomb()) { //if current cell does not have a bomb
		    		int num = ce.countSurrBombs();
		    		ce.placeNum(numbers.get(num));
		    		ce.setNum(num);
		    	}
	    	}
		}
	}
	public Cell getCell(int i, int j) {
		return world[wrap(i)][wrap(j)];
	}

	// returns int 0<=ret<worldSize.  Wraps as needed.
    public int wrap( int ij ){
    	if ( ij<0 ) { ij = 0; } 
    	if ( ij>=worldSize ) { ij = worldSize-1; }
    	return ij;
    }
    public void coverEverything() {
    	for(int i = 0; i<worldSize; i++) {
    		for(int j = 0; j<worldSize; j++) {
    			world[i][j].cover(scale);
    		}
    	}
    }
    public void uncoverEverything() {
    	for(int i = 0; i<worldSize; i++) {
    		for(int j = 0; j<worldSize; j++) {
    			world[i][j].removeCover();
    		}
    	}
    }
    public class Cell extends StackPane
    {
	    ArrayList<Cell> neighbors;
	    boolean hasBomb;
	    ImageView facingDowniv;
	    int num; //number displayed
	    double scale; //used to size everything according to different difficult levels
	    ImageView flagiv;
	    int col;
	    int row;
	    Rectangle r;
	    boolean covered;
	    public Cell( double wh, double x, double y, boolean hasBomb_, int i, int j )
	    {
    	   scale = wh;
    	   r = new Rectangle(wh, wh);
    	   Color pieceBg = new Color(185.0/255, 185.0/255, 185.0/255, 1.0);
    	   r.setFill(pieceBg);
    	   super.getChildren().add(r);
    	   hasBomb = hasBomb_;
    	   col = i; 
    	   row = j;
    	   neighbors = new ArrayList<Cell>();
    	   covered = true;
    	   addEventHandler(MouseEvent.MOUSE_CLICKED,(MouseEvent m)->
    		{ 
    			System.out.println("col = " + col + ", row = " + row);
    			if(hasBomb && state == 0) { //if you click on one that has a bomb
    				r.setFill(Color.RED);
    				uncoverEverything();
    				inGame = false;
    				status.setText("You Lost!");
    			}
    			if(state == 0) { //uncovering state
    				super.getChildren().remove(facingDowniv);
    				numUncovered++;
    				System.out.println("numUncovered = " + numUncovered);
    				if(numUncovered == numNeeded) {
    					status.setText("You Win!");
    				}
    				covered = false;
    			} else if(covered){ //flagging state
    				Image flag = new Image("flagged.png");
    				System.out.println("target coordinates = (" + (m.getSceneX()-200) + ", " + (m.getSceneY()-topHeight) + ")");
    				int cellI = (int)((m.getSceneX()-200)/scale);
    				int cellJ = (int)((m.getSceneY()-topHeight)/scale);
    				Cell ce = world[cellI][cellJ];
    				System.out.println("target cell = (" + cellI + ", " + cellJ + ")");
    				Flag f = new Flag(flag, ce);
    				flags.add(f);
    				gameStack.getChildren().add(f);
    				numRemBombs--;
    				remainingNum.setText(numRemBombs.toString());
    			}
    			if(num == 0) {
    				System.out.println("blank");
    				//keepUncovering(); //uncover squares until we find one with a number
    			}
    			
    		});
	    }
	    public double getX() {
	    	return getLayoutX();
	    }
	    public double getY() {
	    	return getLayoutY();
	    }
	    public void removeCover() {
	    	super.getChildren().remove(facingDowniv);
	    }
	    public void addNeighbor( Cell c )
	    {
    	   if(!neighbors.contains(c) && c != this) {
    		   neighbors.add(c);
    	   }
	    }
	
	    boolean getHasBomb() {
    	   return hasBomb;
	    }
	
	    void setHasBomb(boolean b) {
    	   hasBomb = b;
	    }
	
	    void placeBomb(double wh) {
    	   Image bomb = new Image("bomb.png");
    	   ImageView bombiv = new ImageView(bomb);
    	   bombiv.setFitHeight(wh);
    	   bombiv.setFitWidth(wh);
    	   super.getChildren().add(bombiv);
	    }
	
	    int countSurrBombs() {
    	   int count = 0;
    	   for(int i = 0; i<neighbors.size(); i++) {
    		   if(neighbors.get(i).getHasBomb()) {
    			   count++;
    		   }
    	   }
    	   return count;
	    }
	
	    void placeNum(Image img) {
    	   ImageView iv = new ImageView(img);
    	   iv.setFitHeight(scale);
    	   iv.setFitWidth(scale);
    	   super.getChildren().add(iv);
	    }
	    void setNum(int n) {
	    	num = n;
	    }
	    void cover(double wh) {
    	   Image facingDown = new Image("facingDown.png");
    	   facingDowniv = new ImageView(facingDown);
    	   facingDowniv.setFitHeight(wh);
    	   facingDowniv.setFitWidth(wh);
    	   super.getChildren().add(facingDowniv);
	    }
	
	    void keepUncovering() {
	    	System.out.println("keeyUncovering()");
	    	ArrayList<Cell> q = new ArrayList<Cell>();
	    	q.add(this);
	    	while(!q.isEmpty()) {
	    		Cell ce = q.get(0);
	    		System.out.println("current cell: (" + ce.col + ", " + ce.row + ")");
	    		for(int i = 0; i<ce.neighbors.size(); i++) {
	    			ce.neighbors.get(i).removeCover();
	    			if(ce.neighbors.get(i).num == 0 && !q.contains(ce.neighbors.get(i))) {
	    				q.add(ce.neighbors.get(i));
	    			}
	    		}
	    		q.remove(0);
	    		System.out.print("q after: ");
	    		for(int i = 0; i<q.size(); i++) {
	    			System.out.print("(" + q.get(i).col + ", " + q.get(i).row + ") ");
	    		}
	    		System.out.print('\n');
	    	}
	    }
    }
    
    public class Flag extends ImageView {
    	double startX;
    	double startY;
    	double endX; //location
    	double endY; //location
    	Cell target;
        public Flag(Image img, Cell ce) //constructor
        {
        	super(img); //construct ImageView
        	super.setFitHeight(scale);
        	super.setFitWidth(scale);
        	target = ce;
        	startX = 200;
        	startY = topHeight/2;
        	if(mode.equals("easy")) {
        		endX = target.getX()-ctrlPaneWidth-7;
        		endY = target.getY()-gameWidth/2+topHeight-5;
        	} else if(mode.equals("inter")) {
        		endX = target.getX()-ctrlPaneWidth-scale-2;
        		endY = target.getY()-gameWidth/2+topHeight-scale;
        	} else {
        		endX = target.getX()-ctrlPaneWidth-2.75*scale;
        		endY = target.getY()-gameWidth/2+topHeight-2.75*scale;
        	}
        	System.out.println("pathway: " + startX + ", " + startY + ", " + endX + ", " + endY);
        	Line path = new Line(startY, startY, endX, endY); //pathway for flag animation
        	PathTransition pt = new PathTransition(new Duration(1500), path, this);
        	gameStack.getChildren().add(path);
        	pt.play();
        	gameStack.getChildren().remove(path);
        	addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent m)-> {
        		gameStack.getChildren().remove(this);
        		numRemBombs++;
				remainingNum.setText(numRemBombs.toString());
        	});
        }
    }
}



