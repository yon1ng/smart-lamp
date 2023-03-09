package cn.liuyong.smartlamp.component;


import cn.liuyong.smartlamp.bean.Collection;
import cn.liuyong.smartlamp.bean.Lamp;
import cn.liuyong.smartlamp.event.events.GatewayConnectedEvent;
import cn.liuyong.smartlamp.event.events.LampStateChangedEvent;
import cn.liuyong.smartlamp.event.listeners.GatewayConnectedListener;
import cn.liuyong.smartlamp.event.listeners.LampStateChangedListener;
import cn.liuyong.smartlamp.util.Utility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalTime;

@Component
@Slf4j
public class Controller implements GatewayConnectedListener, LampStateChangedListener {

	@Autowired
	private Client client;
	
	private boolean connected = false;

	@PostConstruct
	private void init(){
		log.info("开始加载控制器！");
		client.getEventManager().addGatewayConnectedListener(this);
		client.getEventManager().addLampStateChangedListener(this);
		client.connect();
	}
	
	public Controller() {
		log.info("初始化控制器！");
	}
	
	public void onLampStateChanged(LampStateChangedEvent event) {
		log.info("灯泡状态发生改变!");
	}

	public void onGatewayConnected(GatewayConnectedEvent event) {
		log.info("网关连接成功!");
		connected = true;
	}
	
	public EventManager getEventManager() {
		return client.getEventManager();
	}

	public Collection getLamps() {
		return new Collection(client.getLamps());
	}

	public void updateLamps(Collection lamps) {
		updateLamps(lamps, true);
	}

	public void updateLamps(Collection lamps, boolean confirm) {
		client.updateLamps(lamps.getRaw());
		
		if (confirm) {
			Utility.sleep(5000);
			if  (! getLamps().equals(lamps)) {
				System.out.println(LocalTime.now() + " Retry...");
				client.updateLamps(lamps.getRaw());
			}
		}
	}

	public boolean areAllLightsOff() {
		Collection lamps = getLamps();
		
		for (Lamp lamp : lamps) {
			if (lamp.getIntensity() != 0) {
				return false;
			}
		}
		
		return true;
	}
	
	public boolean isConnected() {
		return connected;
	}
	
	public LocalTime getLastCommunication() {
		return client.getLastCommunication();
	}
}
