package com.pd.game.magic64;

import com.pd.game.chess.Piece;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

/**
 * To select which piece, pawn want to convert.
 * It will display Bishop, Queen, Knight, Rook, and Pawn.
 *
 */
public class ChooserDialog extends Dialog implements android.view.View.OnClickListener{

	ImageButton btnQueen,btnPawn,btnRook,btnKnight,btnBishop;
	private boolean isWhite = false;
	private android.view.View.OnClickListener listener = null;
	private int selType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choose_piece);
		btnQueen = (ImageButton) findViewById(R.id.btnQueen);
		btnBishop = (ImageButton) findViewById(R.id.btnBishop);
		btnKnight = (ImageButton) findViewById(R.id.btnKnight);
		btnPawn = (ImageButton) findViewById(R.id.btnPawn);
		btnRook = (ImageButton) findViewById(R.id.btnRook);

		if(isWhite){
			btnBishop.setImageResource(R.drawable.white_bishop);
			btnKnight.setImageResource(R.drawable.white_knight);
			btnPawn.setImageResource(R.drawable.white_pawn);
			btnQueen.setImageResource(R.drawable.white_queen);
			btnRook.setImageResource(R.drawable.white_rook);
		}


		btnBishop.setOnClickListener(this);
		btnKnight.setOnClickListener(this);
		btnPawn.setOnClickListener(this);
		btnQueen.setOnClickListener(this);
		btnRook.setOnClickListener(this);
	}

	public ChooserDialog(Context context,boolean isWhite,android.view.View.OnClickListener listener) {
		super(context);
		this.isWhite = isWhite;
		this.listener = listener;
		setCancelable(false);
		setCanceledOnTouchOutside(false);
	}

	public ChooserDialog(Context context, boolean white) {
		this(context,white,null);
	}
	
	public int getSelectedType(){
		return selType;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnBishop:
			v.setTag(Piece.TYPE_BISHOP);
			selType = Piece.TYPE_BISHOP;
			break;
		case R.id.btnKnight:
			v.setTag(Piece.TYPE_KNIGHT);
			selType = Piece.TYPE_KNIGHT;
			break;
		case R.id.btnPawn:
			v.setTag(Piece.TYPE_PAWN);
			selType = Piece.TYPE_PAWN;
			break;
		case R.id.btnQueen:
			v.setTag(Piece.TYPE_QUEEN);
			selType = Piece.TYPE_QUEEN;
			break;
		case R.id.btnRook:
			v.setTag(Piece.TYPE_ROOK);
			selType = Piece.TYPE_ROOK;
			break;

		default:
			v.setTag(Piece.TYPE_QUEEN);
			break;
		}

		if(listener != null)
			listener.onClick(v);

		this.dismiss();
	}

}
