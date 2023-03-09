package cn.liuyong.smartlamp.component;


import cn.liuyong.smartlamp.event.Callable;
import cn.liuyong.smartlamp.event.EventHandler;
import cn.liuyong.smartlamp.event.events.*;
import cn.liuyong.smartlamp.event.listeners.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EventManager {

	private EventHandler<LampStateChangedListener> lampStateChanged = new EventHandler<LampStateChangedListener>();

	private EventHandler<GatewayConnectedListener> gatewayConnected = new EventHandler<GatewayConnectedListener>();

	private EventHandler<UdpPackageReceivedListener> udpPackageReceived = new EventHandler<UdpPackageReceivedListener>();

	private EventHandler<ExceptionListener> exception = new EventHandler<ExceptionListener>();
	
	private EventManager() {
		log.info("事件管理器加载完毕!");
	}


	public void addLampStateChangedListener(LampStateChangedListener listener) {
		lampStateChanged.addListener(listener);
	}

	public void addGatewayConnectedListener(GatewayConnectedListener listener) {
		gatewayConnected.addListener(listener);
	}
	
	public void addUdpPacketReceivedListener(UdpPackageReceivedListener listener) {
		udpPackageReceived.addListener(listener);
	}

	public void addExceptionListener(ExceptionListener listener) {
		exception.addListener(listener);
	}

	
	public void trigger(LampStateChangedEvent event) {
		lampStateChanged.trigger(event, listener -> listener.onLampStateChanged(event));
	}
	
	public void trigger(GatewayConnectedEvent event) {
		gatewayConnected.trigger(event, listener -> listener.onGatewayConnected(event));
	}
	
	public void trigger(UdpPacketReceivedEvent event) {
		udpPackageReceived.trigger(event, listener -> listener.onUdpPackageReceived(event));
	}
	
	public void trigger(ExceptionEvent event) {
		exception.trigger(event, listener -> listener.onException(event));
	}

}
