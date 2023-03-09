package cn.liuyong.smartlamp.event.listeners;


import cn.liuyong.smartlamp.event.events.ExceptionEvent;

public interface ExceptionListener extends EventListener {
	public void onException(ExceptionEvent event);
}
