package Blockecho;

import java.security.PublicKey;

public class TransactionOutput {
	public String id;
	public PublicKey reciepient; //new owner of these messages.
	public String msg; 
	public String parentTransactionId; //the id of the transaction this output was created in
	
	//Constructor
	public TransactionOutput(PublicKey reciepient, String msg, String parentTransactionId) {
		this.reciepient = reciepient;
		this.msg = msg;
		this.parentTransactionId = parentTransactionId;
		this.id = StringUtil.applySha256(StringUtil.getStringFromKey(reciepient)+msg+parentTransactionId);
	}
	
	
	public boolean isMine(PublicKey publicKey) {
		return (publicKey == reciepient);
	}
	
}

