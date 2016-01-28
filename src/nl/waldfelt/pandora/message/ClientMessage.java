package nl.waldfelt.pandora.message;


public class ClientMessage extends Message{
	
	private static final long serialVersionUID = 1320793556284303786L;

	public ClientMessage(String pCommand, Object pData) {
		super(pCommand, pData);
	}
} 
