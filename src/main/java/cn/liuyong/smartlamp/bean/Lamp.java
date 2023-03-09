package cn.liuyong.smartlamp.bean;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class Lamp {

	private int deviceId;

	private int red;

	private int green;

	private int blue;

	private int intensity;
	
	public Lamp(int deviceId, int red, int green, int blue, int intensity) {
		this.deviceId = deviceId;
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.intensity = intensity;
	}
	
	public Lamp(Lamp lamp) {
		this(lamp.getId(), lamp.getRed(), lamp.getGreen(), lamp.getBlue(), lamp.getIntensity());
	}
	
	public int getId() {
		return deviceId;
	}
	
	public int getAlias() {
		return -1 * deviceId;
	}
	
	public int getIntensity() {
		return intensity;
	}
	
	public void setIntensity(int intensity) {
		this.intensity = intensity;
	}
	
	public int getRed() {
		return red;
	}
	
	public void setRed(int red) {
		this.red = red;
	}
	
	public int getGreen() {
		return green;
	}
	
	public void setGreen(int green) {
		this.green = green;
	}
	
	public int getBlue() {
		return blue;
	}
	
	public void setBlue(int blue) {
		this.blue = blue;
	}
	
	public void setRGB(int red, int green, int blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}
	
	public void setRGBI(int red, int green, int blue, int intensity) {
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.intensity = intensity;
	}
	
	public String toString() {
		return String.format("[%d, r=%d, g=%d, b=%d, i=%d]", deviceId, red, green, blue, intensity);
	}
	
	public byte[] getBytes() {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		
		byte[] id = ByteBuffer.allocate(4).putInt(deviceId).array();
		
		for (int i = 0; i < id.length; i++) {
			buffer.write(id[id.length - i - 1]);
		}

		buffer.write((byte) intensity);
		buffer.write((byte) red);
		buffer.write((byte) green);
		buffer.write((byte) blue);
		
		return buffer.toByteArray();
	}
	
	public boolean equals(Object object) {
		if (! (object instanceof Lamp)) {
			return false;
		}
		
		Lamp other = (Lamp) object;
		
		return other.getId() == this.getId() &&
				other.getRed() == this.getRed() &&
				other.getGreen() == this.getGreen() &&
				other.getBlue() == this.getBlue() &&
				other.getIntensity() == this.getIntensity();
	}
}
