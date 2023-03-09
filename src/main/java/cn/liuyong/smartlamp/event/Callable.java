package cn.liuyong.smartlamp.event;


import cn.liuyong.smartlamp.event.listeners.EventListener;

public interface Callable<L extends EventListener> {

	void call(L listener);

}
