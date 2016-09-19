package com.pd.game.magic64;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pd.game.bt.BluetoothChatService;
import com.pd.game.bt.DeviceListActivity;
import com.pd.game.chess.Bishop;
import com.pd.game.chess.Empty;
import com.pd.game.chess.King;
import com.pd.game.chess.Knight;
import com.pd.game.chess.Pawn;
import com.pd.game.chess.Piece;
import com.pd.game.chess.Queen;
import com.pd.game.chess.Rook;

public class Game extends Activity{

	private static final String FILE_LAST_SAVED = "lastSaved";

	public static final String GAME_CONTINUE = "continueMagic64";
	public static final String GAME_OPEN = "openMagic64";

	private static final int STATUS_WHOS_TURN = 1500;
	private static final int STATUS_LAST_MOVE_BLACK = 1501;
	private static final int STATUS_LAST_MOVE_WHITE = 1502;

	private static final boolean D = false;

	private static final String BT_SEPARATOR = ":";


	private ChessBoardView chessView;

	private Piece[][] piece = new Piece[Piece.MAX][Piece.MAX];

	private Piece emptyPiece;

	private int id = 0;

	private static boolean lastBlackMoved = true;

	private boolean gameOver = false;

	private int whoWon;

	private ArrayList<Piece> checker2White = new ArrayList<Piece>();

	private ArrayList<Piece> checker2Black = new ArrayList<Piece>();

	private int[][] checksOnBlackKing = new int[Piece.MAX][Piece.MAX];
	private int[][] checksOnWhiteKing = new int[Piece.MAX][Piece.MAX];

	private boolean checkOnBlack = false;

	private boolean checkOnWhite = false;

	private String msgCheckBlack;

	private String msgCheckWhite;

	private static String filePath;// = "/data/data/"+this.getPackageName()+"/magic64data/";


	// status bar variables.
	private boolean isStatusBarOn = true;
	private View btnWhos = null;
	private TextView lblLastMoveWhite = null;
	private TextView lblLastMoveBlack = null;
	private ImageView imgLastMoveWhite = null;
	private ImageView imgLastMoveBlack = null;

	private int imgResourceLastMoveWhite;
	private int imgResourceLastMoveBlack;
	private String strLastMoveWhite;
	private String strLastMoveBlack;

	private String strBT_Move;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		super.onCreate(savedInstanceState);
		isStatusBarOn = Settings.getStatusBar(Game.this);

		// creating a DIR to save the game state
		filePath = "/data/data/"+this.getPackageName()+"/magic64data/";
		if(new File(filePath).mkdirs()){
			if(D)Log.i("DIR", "dir created :"+filePath);
		}
		else {
			if(D)Log.e("DIR", "failed mkdir :"+filePath);
		}

		emptyPiece = new Empty(R.drawable.ic_empty);
		setUpChessBoard();

		Intent intent = getIntent();
		GameState state = null;
		if(intent.getBooleanExtra(GAME_CONTINUE, false)){
			state = deserialize(FILE_LAST_SAVED);
			if(state != null)
			{	
				piece = state.getPiece();
				lastBlackMoved = state.getWhosTurn();
			}
		}
		else if(intent.getStringExtra(GAME_OPEN) != null){
			state = deserialize(intent.getStringExtra(GAME_OPEN));
			if(state != null)
			{	
				piece = state.getPiece();
				lastBlackMoved = state.getWhosTurn();
			}
		}
		else
			lastBlackMoved = true;


		chessView = new ChessBoardView(Game.this,piece);

		setContentView(R.layout.game);

		if(isStatusBarOn){
			LinearLayout v = (LinearLayout) findViewById(R.id.viewForChess);
			v.addView(chessView);// chessView;

			findStatusBarViews();

		}
		else{
			findViewById(R.id.statusBar).setVisibility(android.view.View.GONE);
		}

		onCreateChat(); // for BT-Chat


		if(isStatusBarOn ){
			updateStatusBar(STATUS_WHOS_TURN, null, -1);
			if(state != null){
				updateStatusBar(STATUS_LAST_MOVE_WHITE, state.getStrLastMoveWhite(), state.getImgResourceWhite());
				updateStatusBar(STATUS_LAST_MOVE_BLACK, state.getStrLastMoveBlack(), state.getImgResourceBlack());
			}
		}

		chessView.invalidate();
	}

	@Override
	protected void onResume() {
		super.onResume();
		onResumeChat();  // for BT-Chat
	}

	@Override
	protected void onStart() {
		super.onStart();
		onStartChat();  // for BT-Chat
	}

	@Override
	protected void onPause() {
		super.onPause();
		GameState gState = new GameState(piece, lastBlackMoved,
				imgResourceLastMoveWhite,strLastMoveWhite,
				imgResourceLastMoveBlack,strLastMoveBlack);

		serialize(gState, FILE_LAST_SAVED);

		onPauseChat();  // for BT-Chat
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		onDestroyChat();  // for BT-Chat
	}

	private void findStatusBarViews() {
		btnWhos = findViewById(R.id.btnWhosTurn);
		lblLastMoveWhite = (TextView)findViewById(R.id.lblLastMoveWhite);
		lblLastMoveBlack = (TextView)findViewById(R.id.lblLastMoveBlack);

		imgLastMoveWhite = (ImageView)findViewById(R.id.imgLastMoveWhite);
		imgLastMoveBlack = (ImageView)findViewById(R.id.imgLastMoveBlack);

	}

	private void setUpChessBoard() {

		for (int row = 0; row < Piece.MAX; row++) {
			for (int col = 0; col < Piece.MAX; col++) {
				piece[row][col] = emptyPiece;;

				if (col == 1 || col == 6) {
					boolean isWhite = col==1? true:false;
					if (isWhite) {
						piece[row][col] = new Pawn(++id,isWhite,R.drawable.white_pawn);
					}
					else
						piece[row][col] = new Pawn(++id,isWhite,R.drawable.black_pawn);

					piece[row][col].setXY(row,col);

				}
				else{
					piece[row][col] = emptyPiece;;
					//piece[row][col].setXY(row,col);
				}

			}	
		}

		piece[0][0] = new Rook(++id, true, R.drawable.white_rook);
		piece[0][0].setXY(0, 0);

		piece[1][0] = new Knight(++id, true, R.drawable.white_knight);
		piece[1][0].setXY(1, 0);

		piece[2][0] = new Bishop(++id, true, R.drawable.white_bishop);
		piece[2][0].setXY(2, 0);

		piece[3][0] = new Queen(++id, true, R.drawable.white_queen);
		piece[3][0].setXY(3, 0);
		piece[4][0] = new King(++id, true, R.drawable.white_king);
		piece[4][0].setXY(4, 0);

		piece[5][0] = new Bishop(++id, true, R.drawable.white_bishop);
		piece[5][0].setXY(5, 0);
		piece[6][0] = new Knight(++id, true, R.drawable.white_knight);
		piece[6][0].setXY(6, 0);
		piece[7][0] = new Rook(++id, true, R.drawable.white_rook);
		piece[7][0].setXY(7, 0);



		piece[0][7] = new Rook(++id, false, R.drawable.black_rook);
		piece[0][7].setXY(0, 7);
		piece[1][7] = new Knight(++id, false, R.drawable.black_knight);
		piece[1][7].setXY(1, 7);
		piece[2][7] = new Bishop(++id, false, R.drawable.black_bishop);
		piece[2][7].setXY(2, 7);

		piece[3][7] = new Queen(++id, false, R.drawable.black_queen);
		piece[3][7].setXY(3, 7);
		piece[4][7] = new King(++id, false, R.drawable.black_king);
		piece[4][7].setXY(4, 7);

		piece[5][7] = new Bishop(++id, false, R.drawable.black_bishop);
		piece[5][7].setXY(5, 7);
		piece[6][7] = new Knight(++id, false, R.drawable.black_knight);
		piece[6][7].setXY(6, 7);
		piece[7][7] = new Rook(++id, false, R.drawable.black_rook);
		piece[7][7].setXY(7, 7);


	}

	/**
	 * @return <b>piece[][]</b>
	 * current state of chess game.
	 */
	Piece[][] getCurrentState() {
		return piece;
	}

	/**
	 * Get all possible check on both kings.
	 * NOTE:- <i>User can use checkOnBlack/checkOnWhite boolean variables as well as ChecksOnBlackKing/ChecksOnWhiteKing ArryList variables to verify checks.</i>
	 */
	private void getCheckPositions(){

		checker2Black.clear();
		checker2White.clear();

		checksOnBlackKing = new int[Piece.MAX][Piece.MAX];
		checksOnWhiteKing = new int[Piece.MAX][Piece.MAX];

		for (int row = 0; row < Piece.MAX; row++) {
			for (int col = 0; col < Piece.MAX; col++) {
				aboutToMakeAMove(piece[row][col], row,col, true);
			}	
		}


		if(checker2Black != null && !checker2Black.isEmpty()){
			msgCheckBlack = "BLACK KING\nCHECK from White "+checker2Black.get(0).getName();
			//Toast.makeText(Game.this, msgCheck, Toast.LENGTH_SHORT).show();
			checkOnBlack = true;

		}
		else{
			checkOnBlack = false;
		}

		if(checker2White != null && !checker2White.isEmpty()){
			msgCheckWhite = "WHITE KING\nCHECK from Black "+checker2White.get(0).getName();
			//Toast.makeText(Game.this, msgCheck, Toast.LENGTH_SHORT).show();
			checkOnWhite = true;
		}
		else{
			checkOnWhite = false;
		}
	}

	/**
	 * It will update the possible moves of the curPiece.
	 * @param curPiece
	 * @param cx
	 * @param cy
	 */
	public void aboutToMakeAMove(Piece curPiece,final int cx,final int cy) {
		aboutToMakeAMove(curPiece, cx, cy, false);
	}

	/**
	 * It will update the possible moves of the curPiece. as well as if <i>isUpdateCheckPosition</i> is <b>true</b> then updates the check positions too.
	 * @param curPiece
	 * @param cx
	 * @param cy
	 * @param isUpdateCheckPositions
	 */
	private void aboutToMakeAMove(Piece curPiece,final int cx,final int cy, boolean isUpdateCheckPositions) {
		if (gameOver) {
			won();
			return;
		}

		if(isUpdateCheckPositions == false){
			// Restrict the user to see moves of opponent or user can only see his moves not opponents move.
			// Now who's turn, Checking for black/white move.
			if(lastBlackMoved  != curPiece.isWhite()){		
//				String player = curPiece.isWhite()?"Black":"White";
//				Toast.makeText(Game.this, player+" turn.", Toast.LENGTH_SHORT).show();
				int[][] possibleMoves = new int[Piece.MAX][Piece.MAX]; // To get the possible moves first initialising with emptyPiece.
				for (int row = 0; row < Piece.MAX; row++) {
					for (int col = 0; col < Piece.MAX; col++) {
						possibleMoves[row][col] = Piece.MOVE_EMPTY;
					}
				}
				curPiece.setPossibleMoves(possibleMoves);
				piece[cx][cy] = curPiece;
				return;
			}
		}

		if(curPiece.getId() != -1 && curPiece.getAlive()){ // Not an Empty piece.

			int[][] moves = curPiece.getMoves();
			boolean isWhite = curPiece.isWhite();
			boolean isMultiStep = curPiece.isMultiStep();
			int pieceType = curPiece.getType();

			int[][] possibleMoves = new int[Piece.MAX][Piece.MAX]; // To get the possible moves first initialising with emptyPiece.
			for (int row = 0; row < Piece.MAX; row++) {
				for (int col = 0; col < Piece.MAX; col++) {
					possibleMoves[row][col] = Piece.MOVE_EMPTY;
				}
			}
			//----------
			for (int[] move : moves) {
				int x,y;
				x = cx; y = cy;
				int moveType;
				if(isMultiStep){

					for (int count = 0; count < Piece.MAX; count++) {
						//							Log.i("MOVES","--------------------------------count: "+count);
						moveType = calculateMove(pieceType,isWhite, x, y, move,possibleMoves);

						if(isUpdateCheckPositions ){
							if(moveType == Piece.MOVE_CHECK){
								updateCheckPosition(curPiece,cx,cy);								
							}
						}

						if (moveType != Piece.MOVE_NORMAL) {
							break; // no need to continue this move.
						}
						if(isWhite){
							x+=move[0];
							y+=move[1];
						}
						else{
							x-=move[0];
							y-=move[1];
						}
					}
				}
				else{
					if(pieceType == Piece.TYPE_PAWN){
						// Pawn is a special piece, first time only it can move {0,2}.
						if(move[0] == 0 && move[1] ==2){ // Pawn 2-step move
							if( !((Pawn) curPiece).hadFirstMove()){
								if(D)Log.i("MOVES","Pawn ID: "+curPiece.getId()+", can have 2-step move 	X:Y("+x +","+y+")");
								moveType =  calculateMove(pieceType,isWhite, x, y, move,possibleMoves);

								if(isUpdateCheckPositions ){
									if(moveType == Piece.MOVE_CHECK){
										updateCheckPosition(curPiece,cx,cy);								
									}
								}

							}
						}
						else {
							if(D)Log.i("MOVES","Pawn ID: "+curPiece.getId()+", can move 	X:Y("+x +","+y+")");
							moveType =  calculateMove(pieceType,isWhite, x, y, move,possibleMoves);

							if(isUpdateCheckPositions ){
								if(moveType == Piece.MOVE_CHECK){
									updateCheckPosition(curPiece,cx,cy);								
								}
							}

						}

					}
					else
					{
						moveType =  calculateMove(pieceType,isWhite, x, y, move,possibleMoves);

						if(isUpdateCheckPositions ){
							if(moveType == Piece.MOVE_CHECK){
								updateCheckPosition(curPiece,cx,cy);								
							}
						}

					}
				}

			}
			curPiece.setPossibleMoves(possibleMoves);
			piece[cx][cy] = curPiece;
			//----------

		}


	}

	/**
	 * It will update the check positions for <i>curPiece</i>.
	 * @param curPiece
	 * @param cx
	 * @param cy
	 */
	private void updateCheckPosition(Piece curPiece, int cx, int cy) {
		if(curPiece.isWhite()){
			checker2Black.add(curPiece);
			checksOnBlackKing[cx][cy] = Piece.MOVE_CHECK;
		}
		else{
			checker2White.add(curPiece);
			checksOnWhiteKing[cx][cy] = Piece.MOVE_CHECK;
		}
	}

	/**
	 * According to piece type it gets the piece possible moves and calculates the current possible moves on the board.
	 * @param pieceType
	 * @param isWhite
	 * @param x
	 * @param y
	 * @param move
	 * @param possibleMoves2
	 * @return currentMove
	 */
	private int calculateMove(int pieceType, boolean isWhite, int x, int y, int[] move, int[][] possibleMoves2) {
		String color;

		if(isWhite){
			color = "White: ";
			x+=move[0];
			y+=move[1];
		}
		else{
			color = "Black: ";
			x-=move[0];
			y-=move[1];
		}

		if( (x>=0 && x<8) && (y>=0 && y<8)){
			Piece targetPiece = piece[x][y];
			if(targetPiece.getAlive()){ 
				if(targetPiece.getType() == Piece.TYPE_EMPTY){
					if(pieceType == Piece.TYPE_PAWN){
						// Pawn is a special piece, the killer moves of pawn are {-1,1},{1,1}.
						if((move[0] == -1 || move[0] == 1) && move[1] ==1){
							possibleMoves2[x][y]=Piece.MOVE_CANT; // Here Pawn can't move.
							return Piece.MOVE_CANT;
						}else{
							if(D)Log.i("MOVES",color +" Normal valid move. 	X:Y("+x +","+y+")");
							possibleMoves2[x][y]=Piece.MOVE_NORMAL;
							return Piece.MOVE_NORMAL;
						}
					}
					//					else if(pieceType == Piece.TYPE_KING){
					//						Log.i("MOVES",color +" Can get CHECK #### X:Y("+x +","+y+")");
					//						possibleMoves2[x][y]=Piece.MOVE_MAY_GET_CHECK;
					//						return Piece.MOVE_MAY_GET_CHECK;
					//					}
					else{ // Except Pawn
						if(D)Log.i("MOVES",color +" Normal valid move. 	X:Y("+x +","+y+")");
						possibleMoves2[x][y]=Piece.MOVE_NORMAL;
						return Piece.MOVE_NORMAL;
					}
				}
				else if(targetPiece.isWhite() == isWhite){ //--else if, both are White/Black
					if(D)Log.i("MOVES",color +" Cant move. 	X:Y("+x +","+y+")");
					possibleMoves2[x][y]=Piece.MOVE_CANT;
					return Piece.MOVE_CANT;
				}
				else if(targetPiece.isWhite() != isWhite){ //--else if, both are different; hence can kill
					int move_Check_Killer;
					if(piece[x][y].getType() == Piece.TYPE_KING){ // checking for CHECK
						move_Check_Killer = Piece.MOVE_CHECK;
					}
					else{
						move_Check_Killer = Piece.MOVE_KILLER;
					}


					if(pieceType == Piece.TYPE_PAWN){
						// Pawn is a special piece, the killer moves of pawn are {-1,1},{1,1}.
						if((move[0] == -1 || move[0] == 1) && move[1] ==1){
							if(D)Log.i("MOVES",color +" Killer valid move. 	X:Y("+x +","+y+")");
							possibleMoves2[x][y]=move_Check_Killer;
							return move_Check_Killer;
						}
					}
					else{
						if(D)Log.i("MOVES",color +" Killer valid move. 	X:Y("+x +","+y+")");
						possibleMoves2[x][y]=move_Check_Killer;
						return move_Check_Killer;
					}
				}
			}else{ // NORMAL MOVES... 

				//This piece got killed before, hence considering as Empty.
				if(D)Log.i("MOVES",color +" xXx DEAD PIECE HERE, Normal valid move. 	X:Y("+x +","+y+")");
				if(pieceType == Piece.TYPE_PAWN){
					// Pawn is a special piece, the killer moves of pawn are {-1,1},{1,1}.
					if((move[0] == -1 || move[0] == 1) && move[1] ==1){
						possibleMoves2[x][y]=Piece.MOVE_CANT; // Here Pawn can't move.
						return Piece.MOVE_CANT;
					}else{
						if(D)Log.i("MOVES",color +" Normal valid move. 	X:Y("+x +","+y+")");
						possibleMoves2[x][y]=Piece.MOVE_NORMAL;
						return Piece.MOVE_NORMAL;
					}
				}
				else{
					possibleMoves2[x][y]=Piece.MOVE_NORMAL;
					return Piece.MOVE_NORMAL;
				}
			}

		}//--else invalid move, i.e out of chess board.

		return Piece.MOVE_EMPTY;
	}

	/**
	 * Swaps the <i>curPiece</i> from <b>current</b> position to <b>selX2 selY2</b> position
	 * @param curPiece
	 * @param selX2
	 * @param selY2
	 * @param isAlive
	 */
	private void swape(Piece curPiece, int selX2, int selY2, boolean isAlive) {
		int curX,curY;
		curX = curPiece.getX();
		curY = curPiece.getY();

		Piece targetPiece = piece[selX2][selY2];

		if(curPiece.getType() == Piece.TYPE_PAWN){
			// Pawn is a special piece, first time only it can move {0,2}.
			// If first move is done by pawn than setFirstMove(true);
			if(!((Pawn) curPiece).hadFirstMove()){
				((Pawn) curPiece).setFirstMove(true);
				if(D)Log.i("MOVES","Pawn ID: "+curPiece.getId()+", Made its first move on X:Y("+curX +","+curY+")");
			}
		}
		if(targetPiece.getType() != Piece.TYPE_EMPTY && isAlive == false){ // Make it dead
			if(targetPiece.getType() == Piece.TYPE_KING){
				if(targetPiece.isWhite()){
					whoWon = Piece.BLACK; 
				}
				else{
					whoWon = Piece.WHITE; 
				}
				won();
			}
			targetPiece.setAlive(false);
			int[][] possibleMoves = new int[Piece.MAX][Piece.MAX]; // Initialising with emptyPiece, b'coz it is dead now.
			for (int row = 0; row < Piece.MAX; row++) {
				for (int col = 0; col < Piece.MAX; col++) {
					possibleMoves[row][col] = Piece.MOVE_EMPTY;
				}
			}
			targetPiece.setPossibleMoves(possibleMoves);

		}
		Piece temp = piece[selX2][selY2];

		piece[selX2][selY2] = curPiece;
		curPiece.setXY(selX2, selY2);

		piece[curX][curY] = temp;

	}

	/**
	 * Set gameOver to true, and display the msg who won.
	 */
	private void won() {
		gameOver = true;
		String player = whoWon==Piece.BLACK?"Black won.":"White won.";
		Toast.makeText(Game.this, player, Toast.LENGTH_LONG).show();
	}

	/**
	 * Makes a move if its valid.
	 * @param curPiece
	 * @param sx
	 * @param sy
	 */
	public void makeMove(Piece curPiece, int sx, int sy) {
		if (gameOver) {
			won();
			return;
		}
		boolean invalidMove = false;

		if((curPiece.getId() != -1)&&(curPiece.getAlive() == true)){
			int lastX = curPiece.getX(),lastY = curPiece.getY();
			// Now who's turn, Checking for black/white move.
			if(lastBlackMoved  == curPiece.isWhite()){				

				int move = curPiece.getPossibleMoves()[sx][sy];

				if(move  == Piece.MOVE_NORMAL){
					lastBlackMoved = !lastBlackMoved; 
					swape(curPiece, sx, sy,true);

					// Check for new Check positions or no Check.
					getCheckPositions();
					if((checkOnWhite && !lastBlackMoved) || (checkOnBlack && lastBlackMoved)){
						lastBlackMoved = !lastBlackMoved;					
						swape(curPiece,lastX,lastY,true); // undo last move
						if(curPiece.getType() == Piece.TYPE_PAWN)
						{
							((Pawn)curPiece).setFirstMove(false);
						}
					}

					// if pawn reaches opponents home, than it will convert into queen.
					pawnToOtherPiece(curPiece);

				}
				else if(move  == Piece.MOVE_KILLER || move == Piece.MOVE_CHECK){
					lastBlackMoved = !lastBlackMoved; 
					swape(curPiece, sx, sy,false);

					// Check for new Check positions or no Check.
					getCheckPositions();
					if((checkOnWhite == true && !lastBlackMoved) || (checkOnBlack == true && lastBlackMoved)){
						lastBlackMoved = !lastBlackMoved;					
						swape(curPiece,lastX,lastY,true); // undo last move
						if(curPiece.getType() == Piece.TYPE_PAWN)
						{
							((Pawn)curPiece).setFirstMove(false);
						}

						piece[sx][sy].setAlive(true); // make the piece alive which got killed, above b'coz we still have a check.
					}

					// if pawn reaches opponents home, than it will convert into queen/bishop/knight/rook/pawn.
					pawnToOtherPiece(curPiece);
				}
				else if(move  == Piece.MOVE_EMPTY || move  == Piece.MOVE_CANT){
					// invalid move
					invalidMove = true;
//					Toast.makeText(Game.this, "Invalid move.", Toast.LENGTH_SHORT).show();
				}

				//Showing check message
				if(checkOnBlack && !checkOnWhite) 
					Toast.makeText(Game.this, msgCheckBlack, Toast.LENGTH_SHORT).show();
				else if (checkOnWhite && !checkOnBlack) 
					Toast.makeText(Game.this, msgCheckWhite, Toast.LENGTH_SHORT).show();

				// Updating status bar.
				updateStatusBar(STATUS_WHOS_TURN,null,-1);
				if (! invalidMove) { // THIS MOVE IS VALID MOVE THEN
					sendBTMove(curPiece.getId(),sx,sy);

					String lastMove = getLastMove(lastX,lastY,sx,sy);
					int resId = curPiece.getImageResource();
					if(curPiece.isWhite()){
						updateStatusBar(STATUS_LAST_MOVE_WHITE,lastMove,resId);
						this.imgResourceLastMoveWhite = resId;
						this.strLastMoveWhite = lastMove;
					}
					else{
						updateStatusBar(STATUS_LAST_MOVE_BLACK,lastMove,resId);
						this.imgResourceLastMoveBlack = resId;
						this.strLastMoveBlack = lastMove;
					}
				}

			}
			else{
//				String player = curPiece.isWhite()?"Black":"White";
//				Toast.makeText(Game.this, player+" turn.", Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void sendBTMove(int id, int sx, int sy) {
		strBT_Move = Game.BT_MOVE +
				id + Game.BT_SEPARATOR+
				sx + Game.BT_SEPARATOR+
				sy + Game.BT_SEPARATOR;
		sendMessage(strBT_Move);		
	}

	/**
	 * Last move in string form, according to chess board border.
	 * @param x
	 * @param y
	 * @param sx
	 * @param sy
	 * @return String form of last move.
	 */
	private String getLastMove(int x, int y, int sx, int sy) {
		String last = "";
		last = ChessBoardView.BORDER_TEXT[8+x] + ChessBoardView.BORDER_TEXT[y] +
				" : " +
				ChessBoardView.BORDER_TEXT[8+sx] + ChessBoardView.BORDER_TEXT[sy];
		return last;
	}

	/**
	 * Updates status bar.
	 * @param key - used to determine which part of bar needs to update.
	 * @param lastMove
	 * @param resId
	 */
	private void updateStatusBar(int key, String lastMove, int resId) { //Updating statusBar 
		if(isStatusBarOn == false){
			return;
		}

		switch (key) {
		case STATUS_WHOS_TURN:
			if(btnWhos != null){ 
				btnWhos.setBackgroundResource(lastBlackMoved ? R.drawable.white_king : R.drawable.black_king);
			}	
			break;

		case STATUS_LAST_MOVE_WHITE:
			if(imgLastMoveWhite != null && resId != -1){ 
				imgLastMoveWhite.setImageResource(resId);
				this.imgResourceLastMoveWhite = resId;
			}
			if(lblLastMoveWhite != null && lastMove != null){ 
				lblLastMoveWhite.setText(lastMove);
				strLastMoveWhite = lastMove;
			}
			break;

		case STATUS_LAST_MOVE_BLACK:
			if(imgLastMoveBlack != null && resId != -1){ 
				imgLastMoveBlack.setImageResource(resId);
				imgResourceLastMoveBlack = resId;
			}
			if(lblLastMoveBlack != null && lastMove != null){ 
				lblLastMoveBlack.setText(lastMove);
				strLastMoveBlack = lastMove;
			}	
			break;

		default:
			break;
		}

	}

	/**
	 * When 'Pawn' reaches opponents home it can convert in to Bishop/Queen/Knight/Rook/Pawn
	 * @param curPiece
	 */
	private void pawnToOtherPiece(final Piece curPiece) {
		if(curPiece.getType() == Piece.TYPE_PAWN){
			final boolean isWhite = curPiece.isWhite();

			ChooserDialog dialog = new ChooserDialog(Game.this,isWhite,new OnClickListener() {
				@Override
				public void onClick(View v) {
					int selType;
					selType = (Integer) v.getTag();
					createNewPiece(curPiece,selType);
					chessView.updatePiece(); //refreshing the screen.
				}
			});

			if(isWhite){
				if(curPiece.getY() == Piece.MAX-1){
					dialog.show();
				}
			}
			else{ //black pawn
				if(curPiece.getY() == 0){
					dialog.show();
				}
			}
		}
	}

	/**
	 * Creates a new Piece according to selType.
	 * @param curPiece
	 * @param selType
	 */
	private void createNewPiece(Piece curPiece, int selType) {
		final int row = curPiece.getX();
		final int col = curPiece.getY();
		final boolean isWhite = curPiece.isWhite();

		curPiece.setAlive(false);

		switch (selType) {
		case Piece.TYPE_BISHOP:
			piece[row][col] = new Bishop(++id, isWhite, isWhite ? R.drawable.white_bishop : 
				R.drawable.black_bishop);
			piece[row][col].setXY(row,col);
			break;
		case Piece.TYPE_KNIGHT:
			piece[row][col] = new Knight(++id, isWhite, isWhite ? R.drawable.white_knight : 
				R.drawable.black_knight);
			piece[row][col].setXY(row,col);
			break;
		case Piece.TYPE_PAWN:
			piece[row][col] = new Pawn(++id, isWhite,  isWhite ? R.drawable.white_pawn : 
				R.drawable.black_pawn);
			piece[row][col].setXY(row,col);
			break;
		case Piece.TYPE_ROOK:
			piece[row][col] = new Rook(++id, isWhite,  isWhite ? R.drawable.white_rook : 
				R.drawable.black_rook);
			piece[row][col].setXY(row,col);
			break;
		case Piece.TYPE_QUEEN:
			piece[row][col] = new Queen(++id, isWhite,  isWhite ? R.drawable.white_queen : 
				R.drawable.black_queen);
			piece[row][col].setXY(row,col);
			break;

		default:
			break;
		}

	}


	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		piece = (Piece[][]) savedInstanceState.getSerializable(Piece.PIECE);
		chessView.updatePiece();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(Piece.PIECE, piece);
		super.onSaveInstanceState(outState);
	}


	/**
	 * To save the GameState for later use.
	 * @param state
	 * @param fileName
	 * @return <b>true</b> if serialization success; otherwise <b>false</b> 
	 */
	private static boolean serialize(GameState state,String fileName) {
		FileOutputStream fos;
		boolean result = false;
		try {
			fos = new FileOutputStream(filePath+fileName);
			ObjectOutputStream oos = new ObjectOutputStream(fos); 

			oos.writeObject(state); 
			oos.flush(); 
			oos.close();
			result = true;

		} catch (FileNotFoundException e) {
			result = false;
			e.printStackTrace();
		} catch (IOException e) {
			result = false;
			e.printStackTrace();
		}

		return result; 
	}

	/**
	 * Deserialization of given file.
	 * @param fileName - to deserialize.
	 * @return GameState of the deserialized file.
	 */
	private static GameState deserialize(String fileName){
		FileInputStream fis;
		GameState state = null;
		try {
			fis = new FileInputStream(filePath+fileName);
			ObjectInputStream ois = new ObjectInputStream(fis); 

			state = (GameState) ois.readObject(); 
			ois.close(); 

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (OptionalDataException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return state; 
	}

	/**
	 * It shows a dialog box for providing a name to save the current game state.
	 * And save the game state.
	 */
	private void saveGame() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);  
		alert.setTitle("Save Game");  
		alert.setIcon(R.drawable.save32);
		alert.setMessage("To play it later.\nGive a name to save.");  

		// Set an EditText view to get user input   
		final EditText inputName = new EditText(this);  
		alert.setView(inputName);  

		alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {  
			public void onClick(DialogInterface dialog, int whichButton) {  
				String gameName = inputName.getText().toString();
				GameState gState = new GameState(piece, lastBlackMoved,
						imgResourceLastMoveWhite,strLastMoveWhite,
						imgResourceLastMoveBlack,strLastMoveBlack);
				if(gameName.isEmpty())
				{
					Toast.makeText(Game.this, "Empty field!!\nGame did not save.", Toast.LENGTH_SHORT).show();
					return;
				}
				gameName = DateFormat.format("yyyy_MM_dd", new Date()) +"_"+ gameName;
				if(serialize(gState, gameName)){ //save the game
					Toast.makeText(Game.this, "Game saved.", Toast.LENGTH_SHORT).show();
				}
				else{
					Toast.makeText(Game.this, "ERROR:\nGame did not save.", Toast.LENGTH_SHORT).show();
				}

			}  
		}); 

		alert.show();

	}

	/*
	 * For blue-tooth chat
	 */
	/*
	 * For blue-tooth chat
	 */
	// Debugging
	private static final String TAG = "BluetoothChat";

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;


	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
	private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
	private static final int REQUEST_ENABLE_BT = 3;

	protected static final String BT_MOVE = "Magic64_Move:";

	// Layout Views
	private TextView mTitle;
	private ListView mConversationView;
	private EditText mOutEditText;
	private Button mSendButton;

	// Name of the connected device
	private String mConnectedDeviceName = null;
	// Array adapter for the conversation thread
	private ArrayAdapter<String> mConversationArrayAdapter;
	// String buffer for outgoing messages
	private StringBuffer mOutStringBuffer;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private BluetoothChatService mChatService = null;


	public void onCreateChat() {
		if(D) Log.e(TAG, "+++ ON CREATE +++");

		// Set up the window layout
		//        this.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		////        setContentView(R.layout.main_chat);
		//        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

		// Set up the custom title
		mTitle = (TextView) findViewById(R.id.title_left_text);
		mTitle.setText(R.string.app_name);
		mTitle = (TextView) findViewById(R.id.title_right_text);

		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
			//            finish();
			return;
		}
	}

	public void onStartChat() {
		if(D) Log.e(TAG, "++ ON START ++");

		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		} else {
			if (mChatService == null) setupChat();
		}
	}

	public synchronized void onResumeChat() {
		if(D) Log.e(TAG, "+ ON RESUME +");

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
		if (mChatService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't started already
			if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
				// Start the Bluetooth chat services
				mChatService.start();
			}
		}
	}

	private void setupChat() {
		Log.d(TAG, "setupChat()");

		// Initialize the array adapter for the conversation thread
		mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
		mConversationView = (ListView) findViewById(R.id.in);
		mConversationView.setAdapter(mConversationArrayAdapter);

		// Initialize the compose field with a listener for the return key
		mOutEditText = (EditText) findViewById(R.id.edit_text_out);
		mOutEditText.setOnEditorActionListener(mWriteListener);

		// Initialize the send button with a listener that for click events
		mSendButton = (Button) findViewById(R.id.button_send);
		mSendButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Send a message using content of the edit text widget
				TextView view = (TextView) findViewById(R.id.edit_text_out);
				String message = view.getText().toString();
				sendMessage(message);
			}
		});

		// Initialize the BluetoothChatService to perform bluetooth connections
		mChatService = new BluetoothChatService(this, mHandler);

		// Initialize the buffer for outgoing messages
		mOutStringBuffer = new StringBuffer("");
	}

	public synchronized void onPauseChat() {
		if(D) Log.e(TAG, "- ON PAUSE -");
	}

	public void onDestroyChat() {
		// Stop the Bluetooth chat services
		if (mChatService != null) mChatService.stop();
		if(D) Log.e(TAG, "--- ON DESTROY ---");
	}

	private void ensureDiscoverable() {
		if(D) Log.d(TAG, "ensure discoverable");
		if (mBluetoothAdapter.getScanMode() !=
				BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	/**
	 * Sends a message.
	 * @param message  A string of text to send.
	 */
	private void sendMessage(String message) {
		// Check that we're actually connected before trying anything
		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
			return;
		}

		// Check that there's actually something to send
		if (message.length() > 0) {
			// Get the message bytes and tell the BluetoothChatService to write
			byte[] send = message.getBytes();
			mChatService.write(send);

			// Reset out string buffer to zero and clear the edit text field
			mOutStringBuffer.setLength(0);
			mOutEditText.setText(mOutStringBuffer);
		}
	}

	// The action listener for the EditText widget, to listen for the return key
	private TextView.OnEditorActionListener mWriteListener =
			new TextView.OnEditorActionListener() {
		public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
			// If the action is a key-up event on the return key, send the message
			if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
				String message = view.getText().toString();
				sendMessage(message);
			}
			if(D) Log.i(TAG, "END onEditorAction");
			return true;
		}
	};

	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothChatService.STATE_CONNECTED:
					mTitle.setText(R.string.title_connected_to);
					mTitle.append(mConnectedDeviceName);
					mConversationArrayAdapter.clear();
					break;
				case BluetoothChatService.STATE_CONNECTING:
					mTitle.setText(R.string.title_connecting);
					break;
				case BluetoothChatService.STATE_LISTEN:
				case BluetoothChatService.STATE_NONE:
					mTitle.setText(R.string.title_not_connected);
					break;
				}
				break;
			case MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				String writeMessage = new String(writeBuf);
				if( ! writeMessage.contains(Game.BT_MOVE) ){
					mConversationArrayAdapter.add("Me:  " + writeMessage);
				}
				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				// construct a string from the valid bytes in the buffer
				String readMessage = new String(readBuf, 0, msg.arg1);
				if(readMessage.contains(Game.BT_MOVE)){
					makeMove(readMessage);
				}
				else{
					mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
				}
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(), "Connected to "
						+ mConnectedDeviceName, Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
						Toast.LENGTH_SHORT).show();
				break;
			}
		}

		
	};

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(D) Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE_SECURE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				connectDevice(data, true);
			}
			break;
		case REQUEST_CONNECT_DEVICE_INSECURE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				connectDevice(data, false);
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				setupChat();
			} else {
				// User did not enable Bluetooth or an error occured
				Log.d(TAG, "BT not enabled");
				Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	private void connectDevice(Intent data, boolean secure) {
		// Get the device MAC address
		String address = data.getExtras()
				.getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
		// Get the BLuetoothDevice object
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		// Attempt to connect to the device
		mChatService.connect(device, secure);
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent serverIntent = null;
		switch (item.getItemId()) {
		case R.id.secure_connect_scan:
			// Launch the DeviceListActivity to see devices and do scan
			serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
			return true;
		case R.id.insecure_connect_scan:
			// Launch the DeviceListActivity to see devices and do scan
			serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
			return true;
		case R.id.discoverable:
			// Ensure this device is discoverable by others
			ensureDiscoverable();
			return true;
		case R.id.gameSave:
			saveGame();
			return true;
		case R.id.gameOpen:
			startActivity(new Intent(Game.this,OpenGame.class));
			finish();
			return true;
		}
		return false;
	}

	private void makeMove(String moveString) {
		int id, sx, sy;
		String[] args = moveString.split(Game.BT_SEPARATOR);
		
		try {
			id = Integer.parseInt(args[1]);
			sx = Integer.parseInt(args[2]);
			sy = Integer.parseInt(args[3]);
		} catch (Exception e) {
			Toast.makeText(Game.this,"error:5551" , Toast.LENGTH_SHORT).show();
			return;
		}
		
		Piece curPiece = getPiece(id);
		aboutToMakeAMove(curPiece, curPiece.getX(), curPiece.getY());
		makeMove(curPiece, sx, sy);
		chessView.updatePiece();
	}

	private Piece getPiece(int id) {
		for (int row = 0; row < Piece.MAX; row++) {
			for (int col = 0; col < Piece.MAX; col++) {
				if(piece[row][col].getId() == id)
				{
					return piece[row][col];
				}
			}
		}
		return null;
	}
}
//-------------------------------------------------------------------------------------

/**
 * 	To save the game current state, in to a file.
 *	This class has getter methods to get the saved game state.
 */
class GameState implements Serializable{
	private static final long serialVersionUID = 1201L;
	private Piece[][] sPiece;
	private boolean sWhosTurn;
	private int sImgResourceLastMoveWhite;
	private int sImgResourceLastMoveBlack;
	private String sStrLastMoveWhite;
	private String sStrLastMoveBlack;

	GameState(Piece[][] _piece,boolean _whosTurn, int imgResourceLastMoveWhite, String strLastMoveWhite, int imgResourceLastMoveBlack, String strLastMoveBlack){
		this.sPiece = _piece;
		this.sWhosTurn = _whosTurn;

		this.sImgResourceLastMoveWhite = imgResourceLastMoveWhite;
		this.sStrLastMoveWhite = strLastMoveWhite;

		this.sImgResourceLastMoveBlack = imgResourceLastMoveBlack;
		this.sStrLastMoveBlack = strLastMoveBlack;
	}

	Piece[][] getPiece(){
		return sPiece;
	}

	boolean getWhosTurn(){
		return sWhosTurn;
	}

	int getImgResourceWhite(){
		return sImgResourceLastMoveWhite;
	}

	String getStrLastMoveWhite(){
		return sStrLastMoveWhite;
	}

	int getImgResourceBlack(){
		return sImgResourceLastMoveBlack;
	}

	String getStrLastMoveBlack(){
		return sStrLastMoveBlack;
	}

}
