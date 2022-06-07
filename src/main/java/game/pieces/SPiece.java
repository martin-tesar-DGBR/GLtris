package game.pieces;

import game.pieces.util.*;

public class SPiece extends Piece {
	private static final int[][][] KICK_CW = {
		{{0, 0}, {-1, 0}, {-1, 1}, {0, -2}, {-1, -2}},
		{{0, 0}, {1, 0}, {1, -1}, {0, 2}, {1, 2}},
		{{0, 0}, {1, 0}, {1, 1}, {0, -2}, {1, -2}},
		{{0, 0}, {-1, 0}, {-1, -1}, {0, 2}, {-1, 2}}
	};

	private static final int[][][] KICK_CCW = {
		{{0, 0}, {1, 0}, {1, 1}, {0, -2}, {1, -2}},
		{{0, 0}, {1, 0}, {1, -1}, {0, 2}, {1, 2}},
		{{0, 0}, {-1, 0}, {-1, 1}, {0, -2}, {-1, -2}},
		{{0, 0}, {-1, 0}, {-1, -1}, {0, 2}, {-1, 2}}
	};

	private static final int[][][] KICK_HALF = {
		{{0, 0}},
		{{0, 0}},
		{{0, 0}},
		{{0, 0}}
	};

	private static final boolean[][] TILE_MAP_E = {
		{false, false, false},
		{true, true, false},
		{false, true, true}
	};

	private static final boolean[][] TILE_MAP_R = {
		{false, false, true},
		{false, true, true},
		{false, true, false}
	};

	private static final boolean[][] TILE_MAP_R2 = {
		{true, true, false},
		{false, true, true},
		{false, false, false}
	};

	private static final boolean[][] TILE_MAP_R3 = {
		{false, true, false},
		{true, true, false},
		{true, false, false}
	};

	public SPiece(int bottomLeftX, int bottomLeftY) {
		super(bottomLeftX, bottomLeftY, TILE_MAP_E, Orientation.E, PieceName.S);
	}

	private SPiece(SPiece piece) {
		super(piece.bottomLeftX, piece.bottomLeftY, piece.tileMap, piece.orientation, PieceName.S);
	}

	@Override
	public boolean[][] getTileMapE() {
		return TILE_MAP_E;
	}

	@Override
	public boolean[][] getTileMapR() {
		return TILE_MAP_R;
	}

	@Override
	public boolean[][] getTileMapR2() {
		return TILE_MAP_R2;
	}

	@Override
	public boolean[][] getTileMapR3() {
		return TILE_MAP_R3;
	}

	@Override
	public int[][][] getKickTableCW() {
		return KICK_CW;
	}

	@Override
	public int[][][] getKickTableCCW() {
		return KICK_CCW;
	}

	@Override
	public int[][][] getKickTableHALF() {
		return KICK_HALF;
	}

	@Override
	public Piece copy() {
		return new SPiece(this);
	}

	public static boolean[][] getTileMapSpawn() {
		return TILE_MAP_E;
	}
}
