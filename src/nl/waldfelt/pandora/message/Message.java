package nl.waldfelt.pandora.message;

import java.io.Serializable;

public class Message implements Serializable{
	private static final long serialVersionUID = 780897132559638316L;
	
	String mCommand;
	Object mData;
	
	public Message(String pCommand, Object pData) {
		mCommand = pCommand;
		mData = pData;
	}
	
	public String getCommand() {
		return mCommand;
	}
	
	public Object getData() {
		return mData;
	}
}
