package cn.liuyong.smartlamp.event.events;

public abstract class AbstractEvent {
	private boolean stopped = false;
	
	public void stopPropagation() {
		stopped = true;
	}
	
	public boolean isStopped() {
		return stopped;
	}
}
