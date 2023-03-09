package cn.liuyong.smartlamp.bean;

import java.util.Iterator;

public class Collection implements Iterable<Lamp>, Iterator<Lamp> {

	private Lamp[] lamps;
	
	private int current = 0;
	
	public Collection(Lamp[] lamps) {
		this.lamps = lamps;
	}
	
	public void reset() {
		current = 0;
	}
	
	public boolean isEmpty() {
		return lamps.length == 0;
	}
	
	public Lamp[] getRaw() {
		return lamps;
	}
	
	public Lamp at(int i) {
		return lamps[i];
	}
	
	public int getLength() {
		return lamps.length;
	}
	
	public Collection copy() {
		Lamp[] copy = new Lamp[lamps.length];
		
		for (int i = 0; i < lamps.length; i++) {
			copy[i] = new Lamp(lamps[i]);
		}
		
		return new Collection(copy);
	}

	@Override
	public Iterator<Lamp> iterator() {
		Collection itarable =  new Collection(lamps);
		
		itarable.reset();
		
		return itarable;
	}

	@Override
	public boolean hasNext() {
		if(lamps == null){
			return false;
		}
		return current < lamps.length;
	}

	@Override
	public Lamp next() {
		return lamps[current++];
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Lamp lamp : lamps) {
			sb.append(lamp);
			sb.append(", ");
		}
		return sb.toString();
	}
	
	public boolean equals(Object object) {
		if (! (object instanceof Collection)) {
			return false;
		}
		
		Collection other = (Collection) object;
		
		for (int i = 0; i < lamps.length; i++) {
			if (!lamps[i].equals(other.at(i))) {
				return false;
			}
		}
		
		return true;
	}
}
