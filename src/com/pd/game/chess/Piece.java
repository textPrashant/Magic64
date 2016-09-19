package com.pd.game.chess;

import java.io.Serializable;


public interface Piece extends Serializable{
	final static int MAX = 8;
	final static String EMPTY = "Empty";
	final static String PAWN = "Pawn";
	final static String KING = "King";
	final static String QUEEN = "Queen";
	final static String ROOK = "Rook";
	final static String KNIGHT = "Knight";
	final static String BISHOP = "Bishop";

	static final String PIECE = "Piece";
	
	final static int BLACK 	= 99999;
	final static int WHITE 	= 11111;
	
	final static int CHECK_TO_WHIET  	= 30001;
	final static int CHECK_TO_BLACK  	= 30002;
	final static int CHECK_TO_NOBUDDY  	= 30003;

 
	final static int TYPE_EMPTY 	= 10000;
	final static int TYPE_PAWN 		= 10001;
	final static int TYPE_BISHOP 	= 10002;
	final static int TYPE_KNIGHT 	= 10003;
	final static int TYPE_ROOK 		= 10004;
	final static int TYPE_QUEEN 	= 10005;
	final static int TYPE_KING 		= 10006;
	
	final static int MOVE_EMPTY 	= 20000; // for out of chess board move!!
	final static int MOVE_NORMAL 	= 20001;
	final static int MOVE_KILLER 	= 20002;
	final static int MOVE_CANT 		= 20003;
	final static int MOVE_DEAD 		= 20004;
	final static int MOVE_CHECK		= 20005;
	static final int MOVE_MAY_GET_CHECK = 20006;
	
	boolean getAlive();
	void setAlive(boolean isAlive);

	int getId();
	String getName();
	int getType();
	
	int getPriority();
	void setSetPriority(int priority);
	
	int getX();
	void setX(int x);
	
	int getY();
	void setY(int y);
	
	void setXY(int x,int y);
	

	int getImageResource();
	void setImageResource(int imageResource);

	int[][] getMoves();
	
	int[][] getPossibleMoves();
	void setPossibleMoves(int[][] possibleMoves);
	
	boolean isWhite();
	boolean isMultiStep();

}
