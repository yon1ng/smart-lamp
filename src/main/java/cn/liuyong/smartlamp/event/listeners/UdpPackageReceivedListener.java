package cn.liuyong.smartlamp.event.listeners;


import cn.liuyong.smartlamp.event.events.UdpPacketReceivedEvent;


public interface UdpPackageReceivedListener extends EventListener {

	void onUdpPackageReceived(UdpPacketReceivedEvent event);

}
