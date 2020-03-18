package annotations;

import com.google.common.base.Enums;

public enum UaTypes {
	Boolean(1), SByte2(2), Byte(3), Int16(4), UInt16(5), Int32(6), UInt32(7), Int64(8), UInt64(9), Float(10),
	Double(11), String(12);

	private final int key;

	UaTypes(int key) {
		this.key = key;
	}

	public int getKey() {
		return key;
	}
	
	public static UaTypes getIfPresent(String name) {
		return Enums.getIfPresent(UaTypes.class, name).orNull(); 
	}
}
