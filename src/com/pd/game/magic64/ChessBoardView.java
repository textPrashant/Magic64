package com.pd.game.magic64;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.pd.game.chess.Piece;


public class ChessBoardView extends View {

	private static final String TAG = "ChessBoardView";
	private final Game game;

	private static final String SELX = "selX" ;
	private static final String SELY = "selY" ;
	private static final String VIEW_STATE = "viewState" ;
	private static final int ID = 64;
	private static int Desity = 160;
	private static float Border = 15f;


	private float width; 	// width of one tile
	private float height; 	// height of one tile
	private int selX; 		// X index of selection
	private int selY; 		// Y index of selection
	private final Rect selRect = new Rect();
	private int curX;
	private int curY;

	private Piece[][] piece;
	private Piece curPiece;
	private boolean moveNow;


	int whichColor[] = { getResources().getColor(R.color.view_moveNormal),
			getResources().getColor(R.color.view_moveKiller),
			getResources().getColor(R.color.view_moveCheck), };
	private static int COL_NORMAL = 0;
	private static int COL_KILLER = 1;
	private static int COL_CHECK = 2;
	
	public static final String[] BORDER_TEXT = {"1","2","3","4","5","6","7","8","A","B","C","D","E","F","G","H"};
	
	private static final boolean D = false;
	
	private Resources resource;

	public ChessBoardView(Context context, Piece[][] piece) {
		super(context);
		setId(ID);
		resource = getResources();
		this.game = (Game) context;
		this.piece = piece;
		Desity = Settings.getDensity(getContext());

		setFocusable(true);
		setFocusableInTouchMode(true);

	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		width = w / 8f;
		height = h / 8f;
		width -= Border/8f;
		height -= Border/8f;
		getRect(selX, selY, selRect); 
		if(D)Log.d("MOVES", "onSizeChanged: width " + width + Border+ ", height " + height+ Border);
		if(D)Log.d("MOVES", "onSizeChanged: w:" + w+ ", h:" + h);
		super.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		drawChessBoard(canvas);
	}

	private void drawChessBoard(Canvas canvas) {

		// Draw the chess board boarder
		if (Settings.getBoarder(getContext()) ) {
			Border = 15f;
			getRect(selX, selY, selRect); 

			Paint borderPaint = new Paint();
			borderPaint.setColor(resource.getColor(R.color.view_background));
			for (int row = 0; row < Piece.MAX; row++) {
				canvas.drawText(BORDER_TEXT[row],3, (int)(row * height)+height/2 +Border, borderPaint);
			}
			
			for (int col = 0; col < Piece.MAX ; col++) {
//				canvas.drawText(BORDER_TEXT [8+col], (int)(col * height), 6, border);
				canvas.drawText(BORDER_TEXT[8+col],(int)(col * width)+width/2 +Border, 12, borderPaint);
			}
			
		}
		else{
			Border = 0f;
			width = this.getWidth() / 8f;
			height = this.getHeight() / 8f;
			getRect(selX, selY, selRect); 
		}


		// Draw the chess board, alternative Black and white boxes, and pieces
		Paint black = new Paint();
		black.setColor(resource.getColor(R.color.view_black));
		Paint white = new Paint();
		white.setColor(resource.getColor(R.color.view_white));
		boolean isWhite = true;
		for (int row = 0; row < Piece.MAX; row++) {
			for (int col = 0; col < Piece.MAX ; col++) {
				if (isWhite) {
					canvas.drawRect((int)(row * width)+ Border, (int)(col * height)+ Border,
							(int)(row * width + width)+ Border, (int)(col * height + height)+ Border, white);

				}
				else{
					canvas.drawRect((int)(row * width)+ Border, (int)(col * height)+ Border,
							(int)(row * width + width)+ Border, (int)(col * height + height)+ Border, black);
				}
				if (piece[row][col].getAlive() == true) {
					Bitmap img =BitmapFactory.decodeResource(resource, piece[row][col].getImageResource());
					img.setDensity(Desity);
					canvas.drawBitmap(img, width*row+ Border, height*col+ Border, null);
				}
				isWhite = !isWhite;
			}
			isWhite = !isWhite;
		}


		// Draw the selection...
		if(D)Log.d(TAG, "selRect=" + selRect);
		Paint selected = new Paint();
		selected.setColor(moveNow?resource.getColor(R.color.view_makeMove):resource.getColor(R.color.view_selected));
		canvas.drawRect(selRect, selected);

		if (piece[selX][selY].getAlive() == true) { //--draw only if it is alive.
			Bitmap img = BitmapFactory.decodeResource(resource, piece[selX][selY].getImageResource());
			img.setDensity(Desity);

			canvas.drawBitmap(img, width*selX+ Border, height*selY+ Border, null);
//			canvas.drawBitmap(piece[selX][selY].getImage(), width*selX+ BORDER, height*selY+ BORDER, null);
		}
		curPiece = piece[selX][selY];

		if(D)Log.i("CHECK","ID:"+ curPiece.getId()+", isWhite:"+ curPiece.isWhite() +", "+curPiece.getName()+ ", X: "+selX + ", Y: "+selY);

		if (Settings.getHints(getContext()) && moveNow) {

			// Draw the hints...
			Paint hint = new Paint();

			Rect r = new Rect();
			int[][] currentPossibleMoves = curPiece.getPossibleMoves();
			if (currentPossibleMoves != null) {
				for (int row = 0; row < Piece.MAX; row++) {
					for (int col = 0; col < Piece.MAX; col++) {
						int thisMove = currentPossibleMoves[row][col]; 
						if(thisMove == Piece.MOVE_NORMAL) // Selected Piece's possible normal moves
						{
							if(D)Log.i("CHECK","PM >> X: "+selX + ", Y: "+selY);

							getRect(row, col, r);
							r.inset(3, 3);
							hint.setColor(whichColor[COL_NORMAL ]);
							hint.setStyle(Style.FILL);
							canvas.drawRect(r, hint);

							if (piece[row][col].getAlive() == true) { //--draw only if it is alive.
								Bitmap img = BitmapFactory.decodeResource(resource, piece[row][col].getImageResource());
								img.setDensity(Desity);
								canvas.drawBitmap(img, width*row+ Border, height*col+ Border, null);
							}

							Paint transp = new Paint();
							transp.setAlpha(25);
							Bitmap img = BitmapFactory.decodeResource(resource, curPiece.getImageResource());
							img.setDensity(Desity);
							canvas.drawBitmap(img , width*row+ Border, height*col+ Border, transp );
						}
						else if(thisMove == Piece.MOVE_KILLER || thisMove == Piece.MOVE_CHECK ) // Selected Piece's possible/Check killer moves
						{
							if(D)Log.i("CHECK","PM >> X: "+selX + ", Y: "+selY);

							getRect(row, col, r);
							r.inset(3, 3);
							hint.setColor(thisMove==Piece.MOVE_KILLER ? whichColor[COL_KILLER] :  whichColor[COL_CHECK]);
							hint.setStyle(Style.FILL);
							canvas.drawRect(r, hint);
							if (piece[row][col].getAlive() == true) { //--draw only if it is alive.
								canvas.drawBitmap(BitmapFactory.decodeResource(resource, piece[row][col].getImageResource()), width*row+ Border, height*col+ Border, new Paint());
							}
						}
					}	
				}						
			}

		}

	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable p = super.onSaveInstanceState();
		if(D)Log.d(TAG, "onSaveInstanceState" );
		Bundle bundle = new Bundle();
		bundle.putInt(SELX, selX);
		bundle.putInt(SELY, selY);
		bundle.putParcelable(VIEW_STATE, p);
		return bundle;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if(D)Log.d(TAG, "onRestoreInstanceState" );
		Bundle bundle = (Bundle) state;
		select(bundle.getInt(SELX), bundle.getInt(SELY));
		super.onRestoreInstanceState(bundle.getParcelable(VIEW_STATE));
		return;
	}

	private void select(int x, int y) {
		
		final int tx = Math.min(Math.max(x, 0), Piece.MAX-1);
		final int ty = Math.min(Math.max(y, 0), Piece.MAX-1);

		if(tx == selX && ty == selY){
			selX = curX;
			selY = curY;

			moveNow = true;
			game.aboutToMakeAMove(curPiece,selX,selY);
			invalidate();
			piece = game.getCurrentState();

			return;
		}

		//		invalidate(selRect);
		selX = Math.min(Math.max(x, 0), Piece.MAX-1);
		selY = Math.min(Math.max(y, 0), Piece.MAX-1);


		if(moveNow) 
		{
			moveNow = false;
			makeMove(selX,selY);
		}

		getRect(selX, selY, selRect);
		curX = selX;
		curY = selY;
		//		invalidate(selRect);

		invalidate(); // may change hint ... 

		//		//tempLogging...
		//		String selmoves="";
		//		int[][] mmm = curPiece.getPossibleMoves();
		//		for (int row = 0; row < Piece.MAX; row++) {
		//			for (int col = 0; col < Piece.MAX; col++) {
		//				int m = mmm[row][col];
		//				if(m == Piece.MOVE_CHECK){
		//					selmoves += " check" + "(" +row + ","+col+")";
		//				}
		//				else if(m == Piece.MOVE_KILLER){
		//					selmoves += " kill" + "(" +row + ","+col+")";
		//				}
		//				else if(m == Piece.MOVE_NORMAL){
		//					selmoves += " normal" + "(" +row + ","+col+")";
		//				}
		//				else if(m == Piece.MOVE_MAY_GET_CHECK){
		//					selmoves += " mayGetCheck" + "(" +row + ","+col+")";
		//				}
		//			}
		//			selmoves += "\n";
		//		}
		//		Log.i("SELMOVES",selmoves);

	}

	private void makeMove(int selX2, int selY2) {
		game.makeMove(curPiece,selX2,selY2); 
		piece = game.getCurrentState();
	}

	private void getRect(int x, int y, Rect rect) {
		rect.set((int) ((x * width)+ Border), (int) ((y * height )+ Border), (int) ((x
				* width + width )+ Border), (int) ((y * height + height )+ Border));
		rect.inset(3, 3); // border
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {


		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if(D)Log.i("MOUSE","before X: "+event.getX()+ ",Y:"+event.getY());
			
			
			int x = (int)( (event.getX()-Border) /(width));
			int y = (int)( (event.getY()-Border) /(height));
			if(D)Log.i("MOUSE","after  X: "+x+ ",Y:"+y);
			if(D)Log.i("MOUSE","width: "+width+ ",height:"+height);
			
			width += Border/8f;
			height += Border/8f;
			select(x,y);
			width -= Border/8f;
			height -= Border/8f;

			break;
		default:
			break;
		}
		return super.onTouchEvent(event);
	}

	public void updatePiece() {
		piece = null;
		piece = game.getCurrentState();
		invalidate();
	}
}
