package network.lobby;

import fr.slaynash.communication.handlers.PacketHandler;
import network.general.*;

import java.util.List;

public class ClientHandler extends PacketHandler {

	GameClient gameClient;

	public ClientHandler() {
	}

	@Override
	public void onConnection() {

	}

	@Override
	public void onDisconnectedByLocal(String s) {
		onDisconnect(s);
	}

	@Override
	public void onDisconnectedByRemote(String s) {
		onDisconnect(s);
	}

	private void onDisconnect(String s) {
		System.out.println(s);
		this.gameClient.lobby.clearPlayers();
	}

	@Override
	public void onPacketReceived(byte[] bytes) {
		byte[] data = new byte[bytes.length - 3];
		System.arraycopy(bytes, 3, data, 0, data.length);
		decodePacket(data);
	}

	@Override
	public void onReliablePacketReceived(byte[] bytes) {
		byte[] data = new byte[bytes.length - 3];
		System.arraycopy(bytes, 3, data, 0, data.length);
		decodePacket(data);
	}

	private void decodePacket(byte[] bytes) {
		if (this.gameClient == null) {
			this.gameClient = (GameClient) this.rudp;
		}
		for (int i = 0; i < bytes.length; i++) {
			System.out.printf("%2x ", bytes[i]);
			if (i % 16 == 7) {
				System.out.print("| ");
			}
			if (i % 16 == 15) {
				System.out.println();
			}
		}
		System.out.println();
		if (bytes[0] == MessageConstants.SERVER) {
			switch (bytes[1]) {
				case MessageConstants.MESSAGE_SERVER_LOBBY_STATE -> {
					ServerLobbyStateMessage msg = new ServerLobbyStateMessage(bytes);
					this.gameClient.lobby.setLobbySettings(msg.settings);
					for (Player player : msg.players) {
						this.gameClient.lobby.addPlayer(player.getName());
						Player currentPlayer = this.gameClient.lobby.getPlayer(player.getName());
						currentPlayer.setReady(player.isReady());
						currentPlayer.setSpectator(player.isSpectator());
					}
					for (OnLobbyUpdate callback : this.gameClient.lobbyUpdateCallbacks) {
						callback.onLobbyUpdate(msg.players);
					}
				}
				case MessageConstants.MESSAGE_SERVER_LOBBY_UPDATE_PLAYER -> {
					ServerLobbyPlayerUpdateMessage msg = new ServerLobbyPlayerUpdateMessage(bytes);
					if (this.gameClient.lobby.getPlayer(msg.username) == null) {
						this.gameClient.lobby.addPlayer(msg.username);
					}
					else if (msg.isDisconnected) {
						this.gameClient.lobby.removePlayer(msg.username);
					}
					else {
						Player player = this.gameClient.lobby.getPlayer(msg.username);
						player.setSpectator(msg.isSpectating);
						player.setReady(msg.isReady);
					}
					List<Player> players = this.gameClient.lobby.getPlayerList();
					for (OnLobbyUpdate callback : this.gameClient.lobbyUpdateCallbacks) {
						callback.onLobbyUpdate(players);
					}
				}
				case MessageConstants.MESSAGE_SERVER_COUNTDOWN -> {
					ServerCountdownMessage msg = new ServerCountdownMessage(bytes);
					if (msg.state == ServerCountdownMessage.TELL_EVERYONE_TO_PREPARE) {
						for (Player player : this.gameClient.lobby.players.values()) {
							player.setReady(false);
						}
						this.gameClient.lobby.changeState(GameState.IN_GAME);
						for (OnPrepareGame callback : this.gameClient.prepareCallbacks) {
							callback.onPrepareGame();
						}
						ClientConfirmStartMessage prepMsg = new ClientConfirmStartMessage();
						this.rudp.sendReliablePacket(prepMsg.serialize());
					}
					if (msg.state == ServerCountdownMessage.START) {
						this.gameClient.lobby.setStarted(true);
						for (OnStartGame callback : this.gameClient.lobby.startGameCallbacks) {
							callback.onStartGame();
						}
					}
				}
				case MessageConstants.MESSAGE_SERVER_GARBAGE -> {
					ServerGarbageMessage msg = new ServerGarbageMessage(bytes);
					for (OnGarbageReceived callback : this.gameClient.garbageReceivedCallbacks) {
						callback.onGarbageReceived(msg.garbage);
					}
				}
				case MessageConstants.MESSAGE_SERVER_GAME_END -> {
					ServerGameEndMessage msg = new ServerGameEndMessage(bytes);
					this.gameClient.lobby.changeState(GameState.LOBBY, msg.winningPlayer);
					for (OnGameFinish callback : this.gameClient.finishCallbacks) {
						callback.onGameFinish(msg.winningPlayer);
					}
				}
				case MessageConstants.MESSAGE_SERVER_BOARD -> {
					ServerBoardMessage msg = new ServerBoardMessage(bytes);
					this.gameClient.updatePlayer(msg.username, msg.isToppedOut, msg.board, msg.queue, msg.hold);
				}
			}
		}
	}

	@Override
	public void onRemoteStatsReturned(int i, int i1, int i2, int i3) {

	}
}