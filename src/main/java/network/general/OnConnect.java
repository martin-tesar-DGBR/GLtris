package network.general;

import java.net.Socket;

public interface OnConnect {
	void onConnect(Socket receivedFrom, SenderThread returnAddress);
}