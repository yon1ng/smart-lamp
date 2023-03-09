package cn.liuyong.smartlamp.event.listeners;


import cn.liuyong.smartlamp.event.events.LampStateChangedEvent;

public interface LampStateChangedListener extends EventListener {

	void onLampStateChanged(LampStateChangedEvent event);
}
