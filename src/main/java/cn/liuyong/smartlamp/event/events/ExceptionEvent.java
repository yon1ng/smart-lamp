package cn.liuyong.smartlamp.event.events;

public class ExceptionEvent extends AbstractEvent {
	private Exception exception;
	
	public ExceptionEvent(Exception exception) {
		this.exception = exception;
	}
	
	public Exception getException() {
		return exception;
	}
}
