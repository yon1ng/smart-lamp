package cn.liuyong.smartlamp.event.listeners;


import cn.liuyong.smartlamp.event.events.GatewayConnectedEvent;

public interface GatewayConnectedListener extends EventListener {

	void onGatewayConnected(GatewayConnectedEvent event);

}
