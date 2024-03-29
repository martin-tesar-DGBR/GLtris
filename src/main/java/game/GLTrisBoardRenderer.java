package game;

import game.callbacks.BoardUpdateCallback;
import game.pieces.*;
import game.pieces.util.*;
import menu.component.Component;
import render.manager.ResourceManager;
import render.texture.TextureAtlas;
import render.texture.TextureNineSlice;
import util.Constants;
import util.Utils;

import java.util.ArrayList;
import java.util.List;

public class GLTrisBoardRenderer extends Component {
	private float tileSize;

	private float xOffsetHeld;
	private float yOffsetHeld;
	private float heldPieceBoundSize;

	private float xOffsetBoard;
	private float yOffsetBoard;

	private float xOffsetQueue;
	private float yOffsetQueue;
	private float queuePieceBoundSizeX;
	private float queuePieceBoundSizeY;

	private float xOffsetGarbage;
	private float yOffsetGarbage;
	private float garbageMargin;
	private float garbageBoundSize;

	private TextureNineSlice backgroundTexture;
	private TextureAtlas tileTexture;

	private GLTrisRender game;
	private BoardUpdateCallback boardUpdate;

	private int boardHeight;
	private int boardWidth;
	private String[] currentQueue;
	private int[] garbageQueue;
	private PieceFactory pieces;

	public GLTrisBoardRenderer(double xPos, double yPos, float tileSize, boolean isActive, GLTrisRender game, PieceFactory pieces) {
		super(xPos, yPos,
			xPos + tileSize * 5.0f + (game.getBoardWidth() + 1) * tileSize + tileSize * 5.0f, (game.getBoardHeight()) * tileSize,
			"Game", isActive);
		this.pieces = pieces;
		boardHeight = game.getBoardHeight();
		boardWidth = game.getBoardWidth();
		this.tileSize = tileSize;

		updateBoardElements();

		backgroundTexture = ResourceManager.getTextureNineSliceByName("images/game_background.png");
		tileTexture = ResourceManager.getAtlasByName("images/default_skin.png");

		this.game = game;
		boardUpdate = () -> {
			currentQueue = this.game.getPieceQueue();
			garbageQueue = this.game.getGarbageQueue();
		};
		game.registerOnBoardUpdate(boardUpdate);
	}

	public float[] generateBackgroundVertices() {
		//TODO: this needs to be refactored ASAP if literally anything in the UI needs to change
		float[] vertices;
		if (garbageQueue == null) {
			vertices = new float[
				boardHeight * boardWidth * Constants.BLOCK_ATTRIBUTES_PER_VERTEX * Constants.BLOCK_ELEMENTS_PER_QUAD +
					9 * Constants.BLOCK_ATTRIBUTES_PER_VERTEX * Constants.BLOCK_ELEMENTS_PER_QUAD +
					9 * Constants.BLOCK_ATTRIBUTES_PER_VERTEX * Constants.BLOCK_ELEMENTS_PER_QUAD];
		}
		else {
			vertices = new float[
				boardHeight * boardWidth * Constants.BLOCK_ATTRIBUTES_PER_VERTEX * Constants.BLOCK_ELEMENTS_PER_QUAD +
					9 * Constants.BLOCK_ATTRIBUTES_PER_VERTEX * Constants.BLOCK_ELEMENTS_PER_QUAD +
					9 * Constants.BLOCK_ATTRIBUTES_PER_VERTEX * Constants.BLOCK_ELEMENTS_PER_QUAD +
					garbageQueue.length * Constants.BLOCK_ATTRIBUTES_PER_VERTEX * Constants.BLOCK_ELEMENTS_PER_QUAD];
		}

		float[] uvsBoard = backgroundTexture.getElementUVs(0, 3, 1, 1);

		int index = 0;
		//board
		for (int i = 0; i < boardHeight; i++) {
			for (int j = 0; j < boardWidth; j++){
				float p0x = (float) xPos + xOffsetBoard + j * tileSize;
				float p0y = (float) yPos + yOffsetBoard + i * tileSize;
				float p1x = p0x + tileSize;
				float p1y = p0y + tileSize;

				Utils.addBlockVertices(vertices, index,
					p0x, p0y, uvsBoard[0], uvsBoard[1],
					p1x, p1y, uvsBoard[2], uvsBoard[3]);

				index += Constants.BLOCK_ATTRIBUTES_PER_VERTEX * Constants.BLOCK_ELEMENTS_PER_QUAD;
			}
		}

		//queue
		float[] uvsQueue = backgroundTexture.getElementUVsNineSlice(1, 3, 2, 2);
		Utils.addBlockVerticesNineSlice(vertices, index,
			(float) xPos + xOffsetQueue, (float) yPos + yOffsetQueue - (game.getNumPreviews() - 1) * queuePieceBoundSizeY,
			(float) xPos + xOffsetQueue + queuePieceBoundSizeX, (float) yPos + yOffsetQueue + queuePieceBoundSizeY + tileSize,
			tileSize, uvsQueue);
		index += 9 * Constants.BLOCK_ATTRIBUTES_PER_VERTEX * Constants.BLOCK_ELEMENTS_PER_QUAD;

		//hold piece
		Utils.addBlockVerticesNineSlice(vertices, index,
			(float) xPos + xOffsetHeld, (float) yPos + yOffsetHeld,
			(float) xPos + xOffsetHeld + heldPieceBoundSize, (float) yPos + yOffsetHeld + heldPieceBoundSize,
			tileSize, uvsQueue);
		index += 9 * Constants.BLOCK_ATTRIBUTES_PER_VERTEX * Constants.BLOCK_ELEMENTS_PER_QUAD;

		if (garbageQueue != null) {
			//garbage queue
			int increment = 0;
			float[] uvsGarbage = backgroundTexture.getElementUVs(0, 2, 1, 1);
			for (int garbageAmount : garbageQueue) {
				Utils.addBlockVertices(vertices, index,
					(float) xPos + xOffsetGarbage + garbageMargin,
					(float) yPos + yOffsetGarbage + increment * garbageBoundSize + garbageMargin,
					uvsGarbage[0], uvsGarbage[1],
					(float) xPos + xOffsetGarbage + garbageBoundSize - garbageMargin,
					(float) yPos + yOffsetGarbage + (increment + garbageAmount) * garbageBoundSize - garbageMargin,
					uvsGarbage[2], uvsGarbage[3]);
				increment += garbageAmount;
				index += Constants.BLOCK_ATTRIBUTES_PER_VERTEX * Constants.BLOCK_ELEMENTS_PER_QUAD;
			}
		}
		return vertices;
	}

	public float[] generateTileVertices() {
		TileState[][] board = game.getBoard();

		List<Float> vertices = new ArrayList<>();

		int px = 0;
		int py = 0;
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[i].length; j++) {
				if (board[i][j] != TileState.EMPTY) {
					float p0x = (float) (xPos + j * tileSize + xOffsetBoard);
					float p0y = (float) (yPos + i * tileSize + yOffsetBoard);
					float p1x = p0x + tileSize;
					float p1y = p0y + tileSize;

					switch(board[i][j]) {
						case GARBAGE -> {
							px = 0; py = 0;
						}
						case I -> {
							px = 1; py = 0;
						}
						case O -> {
							px = 2; py = 0;
						}
						case L -> {
							px = 3; py = 0;
						}
						case J -> {
							px = 0; py = 1;
						}
						case S -> {
							px = 1; py = 1;
						}
						case Z -> {
							px = 2; py = 1;
						}
						case T -> {
							px = 3; py = 1;
						}
						default -> {
							px = 0; py = 0;
						}
					}

					float[] uvs = tileTexture.getElementUVs(px, py, 1, 1);
					float p0u = uvs[0];
					float p0v = uvs[1];
					float p1u = uvs[2];
					float p1v = uvs[3];

					Utils.addBlockVertices(vertices,
						p0x, p0y, p0u, p0v,
						p1x, p1y, p1u, p1v);
				}
			}
		}

		int pieceX = game.getPieceX();
		int pieceY = game.getPieceY();
		boolean[][] tileMap = game.getTileMap();
		PieceColour pieceColour = game.getPieceColour();
		if (tileMap != null) {
			for (int i = 0; i < tileMap.length; i++) {
				for (int j = 0; j < tileMap[i].length; j++) {
					if (tileMap[i][j]) {
						float p0x = (float) xPos + (pieceX + j) * tileSize + xOffsetBoard;
						float p0y = (float) yPos + (pieceY + i) * tileSize + yOffsetBoard;
						float p1x = p0x + tileSize;
						float p1y = p0y + tileSize;

						switch(pieceColour) {
							case I -> {
								px = 1; py = 0;
							}
							case O -> {
								px = 2; py = 0;
							}
							case L -> {
								px = 3; py = 0;
							}
							case J -> {
								px = 0; py = 1;
							}
							case S -> {
								px = 1; py = 1;
							}
							case Z -> {
								px = 2; py = 1;
							}
							case T -> {
								px = 3; py = 1;
							}
							default -> {
								px = 0; py = 0;
							}
						}

						float[] uvs = tileTexture.getElementUVs(px, py, 1, 1);

						Utils.addBlockVertices(vertices,
							p0x, p0y, uvs[0], uvs[1],
							p1x, p1y, uvs[2], uvs[3]);
					}
				}
			}

			boolean moveDown = true;
			while (moveDown && tileMap.length > 0) {
				for (int i = 0; i < tileMap.length; i++) {
					for (int j = 0; j < tileMap[i].length; j++) {
						if (tileMap[i][j] &&
							!(pieceX + j >= 0 && pieceX + j < board[i].length &&
							pieceY - 1 + i >= 0 && pieceY - 1 + i < board.length &&
							board[pieceY - 1 + i][pieceX + j] == TileState.EMPTY)) {
							moveDown = false;
							break;
						}
					}
				}
				if (moveDown) {
					pieceY--;
				}
			}
			switch(pieceColour) {
				case I -> {
					px = 1; py = 2;
				}
				case O -> {
					px = 2; py = 2;
				}
				case L -> {
					px = 3; py = 2;
				}
				case J -> {
					px = 0; py = 3;
				}
				case S -> {
					px = 1; py = 3;
				}
				case Z -> {
					px = 2; py = 3;
				}
				case T -> {
					px = 3; py = 3;
				}
				default -> {
					px = 0; py = 2;
				}
			}

			float[] uvs = tileTexture.getElementUVs(px, py, 1, 1);

			for (int i = 0; i < tileMap.length; i++) {
				for (int j = 0; j < tileMap[i].length; j++) {
					if (tileMap[i][j]) {
						float p0x = (float) xPos + (pieceX + j) * tileSize + xOffsetBoard;
						float p0y = (float) yPos + (pieceY + i) * tileSize + yOffsetBoard;
						float p1x = p0x + tileSize;
						float p1y = p0y + tileSize;

						Utils.addBlockVertices(vertices,
							p0x, p0y, uvs[0], uvs[1],
							p1x, p1y, uvs[2], uvs[3]);
					}
				}
			}
		}


		String heldPiece = game.getHeldPiece();
		PieceBuilder pieceBuilder = pieces.getBuilder(heldPiece);
		tileMap = pieceBuilder == null ? new boolean[0][0] : pieceBuilder.getTileMapE();
		PieceColour heldPieceColour = pieceBuilder == null ? null : pieceBuilder.getPieceColour();

		if (heldPieceColour != null) {
			switch(heldPieceColour) {
				case I -> {
					px = 1; py = 0;
				}
				case O -> {
					px = 2; py = 0;
				}
				case L -> {
					px = 3; py = 0;
				}
				case J -> {
					px = 0; py = 1;
				}
				case S -> {
					px = 1; py = 1;
				}
				case Z -> {
					px = 2; py = 1;
				}
				case T -> {
					px = 3; py = 1;
				}
				default -> {
					px = 0; py = 0;
				}
			}

			float[] uvs = tileTexture.getElementUVs(px, py, 1, 1);

			for (int i = 0; i < tileMap.length; i++) {
				for (int j = 0; j < tileMap[i].length; j++) {
					if (tileMap[i][j]) {
						float p0x = (float) xPos + j * tileSize + (xOffsetHeld + 0.5f * heldPieceBoundSize - tileMap[0].length * 0.5f * tileSize);
						float p0y = (float) yPos + i * tileSize + (yOffsetHeld + 0.5f * heldPieceBoundSize - tileMap.length * 0.5f * tileSize);
						float p1x = p0x + tileSize;
						float p1y = p0y + tileSize;

						Utils.addBlockVertices(vertices,
							p0x, p0y, uvs[0], uvs[1],
							p1x, p1y, uvs[2], uvs[3]);
					}
				}
			}
		}

		//queue
		for (int index = 0; index < (Math.min(game.getNumPreviews(), currentQueue.length)); index++) {
			pieceBuilder = pieces.getBuilder(currentQueue[index]);
			tileMap = pieceBuilder.getTileMapE();
			heldPieceColour = pieceBuilder.getPieceColour();
			switch (heldPieceColour) {
				case I -> {
					px = 1; py = 0;
				}
				case O -> {
					px = 2; py = 0;
				}
				case L -> {
					px = 3; py = 0;
				}
				case J -> {
					px = 0; py = 1;
				}
				case S -> {
					px = 1; py = 1;
				}
				case Z -> {
					px = 2; py = 1;
				}
				case T -> {
					px = 3; py = 1;
				}
			}

			float[] uvs = tileTexture.getElementUVs(px, py, 1, 1);

			for (int i = 0; i < tileMap.length; i++) {
				for (int j = 0; j < tileMap[i].length; j++) {
					if (tileMap[i][j]) {
						float p0x = (float) xPos + j * tileSize + (xOffsetQueue + 0.5f * queuePieceBoundSizeX) - tileMap[0].length * 0.5f * tileSize;
						float p0y = (float) yPos + i * tileSize + (yOffsetQueue + 0.5f * queuePieceBoundSizeY - tileMap.length * 0.5f * tileSize) - index * queuePieceBoundSizeY;
						float p1x = p0x + tileSize;
						float p1y = p0y + tileSize;

						Utils.addBlockVertices(vertices,
							p0x, p0y, uvs[0], uvs[1],
							p1x, p1y, uvs[2], uvs[3]);
					}
				}
			}
		}

		float[] ret = new float[vertices.size()];
		for (int i = 0; i < vertices.size(); i++) {
			ret[i] = vertices.get(i);
		}

		return ret;
	}

	//use specific methods to generate vertices for each part
	@Override
	public float[] generateVertices() {
		return new float[0];
	}

	@Override
	public void destroy() {
		game.unregisterOnBoardUpdate(boardUpdate);
	}

	@Override
	public void onClick(double mouseX, double mouseY, int button, int action, int mods) {

	}

	@Override
	public void onHover(double mouseX, double mouseY, boolean isInFrame) {

	}

	@Override
	public void onScroll(double mouseX, double mouseY, double xOffset, double yOffset) {

	}

	public void setTileSize(float tileSize) {
		this.tileSize = tileSize;
		updateBoardElements();
	}

	private void updateBoardElements() {
		heldPieceBoundSize = tileSize * 5.0f;
		xOffsetHeld = (float) 0.0;
		yOffsetHeld = (float) (0.0 + boardHeight * tileSize - heldPieceBoundSize);

		xOffsetGarbage = (float) (0.0 + heldPieceBoundSize + 0.5 * tileSize);
		yOffsetGarbage = (float) 0.0;
		garbageMargin = (tileSize * 0.0625f);
		garbageBoundSize = tileSize;

		xOffsetBoard = (float) (0.0 + heldPieceBoundSize + 2.0 * tileSize);
		yOffsetBoard = (float) 0.0;

		queuePieceBoundSizeX = tileSize * 5.0f;
		queuePieceBoundSizeY = tileSize * 3.5f;
		xOffsetQueue = (float) (0.0 + xOffsetBoard + (boardWidth + 1) * tileSize);
		yOffsetQueue = (float) (0.0 + boardHeight * tileSize - queuePieceBoundSizeY - tileSize);
	}
}
