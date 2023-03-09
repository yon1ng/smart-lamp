package cn.liuyong.smartlamp.requests;

import lombok.Data;

@Data
public abstract class AbstractLedoRequest implements LedoRequest {

	private byte[] response;

}
