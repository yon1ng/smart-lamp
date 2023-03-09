package cn.liuyong.smartlamp.requests;

import java.nio.ByteBuffer;

public class GetLampsRequest extends AbstractLedoRequest {

	private byte[] bytes;

	public GetLampsRequest(int gatewayId) {

		byte[] gatewayIdArray = ByteBuffer.allocate(4).putInt(gatewayId)
				.array();

		bytes = new byte[] {
			(byte) 0xf3,
			(byte) 0xd4,
			gatewayIdArray[0], 
			gatewayIdArray[1], 
			gatewayIdArray[2],
			gatewayIdArray[3], 
			0x00, 
			0x00, 
			0x1d, 
			0x05, 
			0x00, 
			0x00, 
			0x00,
			0x43, 
			0x00 
		};
	}

	public byte[] getBytes() {
		return bytes;
	}
}
