package com.pd.game.chess;


public class Bishop implements Piece{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int[][] MOVES = {{1,-1},{1,1},{-1,1},{-1,-1}};
	private int image;
	int[][] possibleMoves;
	private int priority = 2;
	private final int id;
	private int x,y;
	private boolean isAlive;
	private boolean isWhite;
	
	// Constructor
	public Bishop(int id, boolean isWhite, int image){
		this.id = id;
		this.isWhite = isWhite;
		this.image = image;
		this.isAlive = true;
		
		// There is no shortcut... remember it...
		possibleMoves = new int[Piece.MAX][Piece.MAX]; // To get the possible moves first initialising with emptyPiece.
		for (int row = 0; row < Piece.MAX; row++) {
			for (int col = 0; col < Piece.MAX; col++) {
				possibleMoves[row][col] = Piece.MOVE_EMPTY;
			}
		}
	}

	public int getImageResource(){
		return image;
	}
	public void setImageResource(int image) {
		this.image = image;
	}


	@Override
	public boolean getAlive() {
		return isAlive;
	}

	@Override
	public void setAlive(boolean isAlive) {
		this.isAlive = isAlive;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public int[][] getPossibleMoves() {
		return this.possibleMoves;
	}

	@Override
	public void setPossibleMoves(int[][] possibleMoves) {
		this.possibleMoves = possibleMoves;		
	}

	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public void setSetPriority(int priority) {
		this.priority = priority;
	}

	@Override
	public String getName() {
		return Piece.BISHOP;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public void setX(int x) {
		this.x = x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public void setY(int y) {
		this.y = y;
	}

	@Override
	public void setXY(int x, int y) {
		setX(x);
		setY(y);
	}

	@Override
	public int[][] getMoves() {
		return MOVES;
	}

	@Override
	public boolean isWhite() {
		return isWhite;
	}

	@Override
	public int getType() {
		return Piece.TYPE_BISHOP;
	}

	@Override
	public boolean isMultiStep() {
		return true;
	}

}
