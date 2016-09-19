package com.pd.game.chess;


public class Empty implements Piece{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//	private Context context;
	private int image;

	// Constructor
	public Empty(int image){
		this.image = image;
	}

	public int getImageResource(){
		return image;
	}
	public void setImageResource(int image) {
		this.image = image;
	}


	@Override
	public boolean getAlive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setAlive(boolean isAlive) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getId() {
		return -1;
	}

	@Override
	public int[][] getPossibleMoves() {
		return null;
	}

	@Override
	public void setPossibleMoves(int[][] possibleMoves) {
		// do nothing...
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public void setSetPriority(int id) {
		// do nothing...
	}

	@Override
	public String getName() {
		return Piece.EMPTY;
	}

	@Override
	public int getX() {
		return -1;
	}

	@Override
	public void setX(int x) {
		// do nothing...
	}

	@Override
	public int getY() {
		return -1;
	}

	@Override
	public void setY(int y) {
		// do nothing...
	}

	@Override
	public void setXY(int x, int y) {
		// do nothing...
	}

	@Override
	public int[][] getMoves() {
		// do nothing...
		return null;
	}

	@Override
	public boolean isWhite() {
		return false;
	}
	@Override
	public int getType() {
		return Piece.TYPE_EMPTY;
	}

	@Override
	public boolean isMultiStep() {
		return false;
	}

}
