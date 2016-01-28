package nl.waldfelt.pandora.message;


public class ServerMessage extends Message{
	
	private static final long serialVersionUID = -8769213668659417220L;

	public ServerMessage(String pCommand, Object pData) {
		super(pCommand, pData);
	}
}
