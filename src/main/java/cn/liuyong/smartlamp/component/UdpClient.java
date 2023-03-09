package cn.liuyong.smartlamp.component;

import cn.liuyong.smartlamp.event.events.ExceptionEvent;
import cn.liuyong.smartlamp.event.events.UdpPacketReceivedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

@Component
@Slf4j
public class UdpClient {

	@Autowired
	EventManager eventManager;

	private int portIn  = 41328;
	
	private DatagramSocket udpSocket;
	
	private ListenThread listenThread = new ListenThread();

	public UdpClient() {

	}
	
	public EventManager getEventManager() {
		return eventManager;
	}

	public void connect() {
		try {
			udpSocket = new DatagramSocket(portIn);
			udpSocket.setBroadcast(true);
			log.info("UDP客户端开启成功,开始接受广播消息!");
			listenThread.start();
		} catch (SocketException e) {
			eventManager.trigger(new ExceptionEvent(e));
			e.printStackTrace();
		}
	}
	
	public void disconnect() {
		listenThread.stopListening();
		udpSocket.disconnect();
	}

	private void processUdpPacket(DatagramPacket packet) {
		eventManager.trigger(new UdpPacketReceivedEvent(packet));
	}

	private class ListenThread extends Thread {
		
		private volatile boolean listen = true;
		
		@Override
		public void run() {
			byte[] buffer = new byte[2048];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

			while (listen) {
				try {
					udpSocket.receive(packet);
					processUdpPacket(packet);
				} catch (IOException e) {
					eventManager.trigger(new ExceptionEvent(e));
				}
			}
		}
		
		public void stopListening() {
			listen = false;
		}
		
	}
}
