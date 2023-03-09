package cn.liuyong.smartlamp.component;


import cn.liuyong.smartlamp.bean.Lamp;
import cn.liuyong.smartlamp.event.events.ExceptionEvent;
import cn.liuyong.smartlamp.event.events.GatewayConnectedEvent;
import cn.liuyong.smartlamp.event.events.LampStateChangedEvent;
import cn.liuyong.smartlamp.event.events.UdpPacketReceivedEvent;
import cn.liuyong.smartlamp.event.listeners.UdpPackageReceivedListener;
import cn.liuyong.smartlamp.requests.AbstractLedoRequest;
import cn.liuyong.smartlamp.requests.GetLampsRequest;
import cn.liuyong.smartlamp.requests.UpdateLampsRequest;
import cn.liuyong.smartlamp.util.Utility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.time.LocalTime;

@Component
@Slf4j
public class Client implements UdpPackageReceivedListener {

	@Autowired
	EventManager eventManager;

	@Autowired
	UdpClient udpClient;

	private int portOut = 41330;

	private volatile InetAddress gateway;

	private volatile int gatewayId;

	private boolean connected = false;

	private byte removedDevices = 0x00;

	private byte addedDevices = 0x00;

	private LocalTime lastCommunication = null;

	private long timeOut = 200;
	
	public Client() {
		log.info("client初始化完毕");
	}
	
	public EventManager getEventManager() {
		return eventManager;
	}

	public void connect() {
		udpClient.getEventManager().addUdpPacketReceivedListener(this);
		udpClient.connect();
	}
	
	public boolean isConnected() {
		return connected;
	}
	
	public void disconnect() {
		udpClient.disconnect();
		connected = false;
	}

	private void rateLimitRequests() {
		if (lastCommunication == null) {
			return;
		}
		
		LocalTime now = LocalTime.now();
		if (lastCommunication.isAfter(now)) {
			return;
		}
		
		Duration duration = Duration.between(lastCommunication, now);
		long sleep = timeOut - duration.toMillis();
			
		if (sleep > 0) {
			Utility.sleep(sleep);
		}
	}
	
	private synchronized void sendRequest(AbstractLedoRequest request) {
		log.info("开始向网关发送请求！");
		rateLimitRequests();
		try {
			Socket tcpSocket = new Socket();
			
			tcpSocket.connect(new InetSocketAddress(gateway, portOut), 5000);
			tcpSocket.setSoTimeout(5000);
			
			tcpSocket.getOutputStream().write(request.getBytes());
			
			byte[] header = new byte[10];
			
			if (tcpSocket.getInputStream().read(header, 0, 10) == 10 && header[9] > 0) {
				byte[] bytes = new byte[header[9]];
				tcpSocket.getInputStream().read(bytes, 0, header[9]);
				
				request.setResponse(bytes);
			}
			
			lastCommunication = LocalTime.now();
			
			tcpSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
			eventManager.trigger(new ExceptionEvent(e));
		}
	}
	
	public Lamp[] getLamps() {
		if (!connected) {
			log.info("网关未连接，获取灯泡列表失败！");
			return null;
		}
		GetLampsRequest request = new GetLampsRequest(gatewayId);
		sendRequest(request);
		byte[] data = request.getResponse();
		
		if (data == null) {
			log.info("获取灯泡列表失败！");
			return null;
		}
			
		Lamp[] lamps = new Lamp[data.length / 8];
		
		for (int i = 0; i < lamps.length; i++) {
			int offset = (i * 8);
			
			int deviceId = Utility.int32FromBytes(data, offset);

			int red = Utility.intFromByte(data[offset + 6]);
			int green = Utility.intFromByte(data[offset + 5]);
			int blue = Utility.intFromByte(data[offset + 4]);
			int intensity = Utility.intFromByte(data[offset + 7]);
			
			lamps[i] = new Lamp(deviceId, red, green, blue, intensity);
		}
		
		return lamps;
	}

	public void updateLamps(Lamp[] lamps) {
		UpdateLampsRequest request = new UpdateLampsRequest(lamps);
		sendRequest(request);
	}
	
	public LocalTime getLastCommunication() {
		return lastCommunication;
	}

	public void onUdpPackageReceived(UdpPacketReceivedEvent event) {
		DatagramPacket packet = event.getPacket();
		gateway = packet.getAddress();
		boolean removed = removedDevices != packet.getData()[21];
		boolean added = addedDevices != packet.getData()[22];
		
		if (connected && (removed || added)) {
			eventManager.trigger(new LampStateChangedEvent());
		}
		
		if (!connected) {
			connected = true;
			eventManager.trigger(new GatewayConnectedEvent());
		}
		removedDevices = packet.getData()[21];
		addedDevices = packet.getData()[22];
		gatewayId = Utility.int32FromBytes(packet.getData(), 2);
	}
	
}
