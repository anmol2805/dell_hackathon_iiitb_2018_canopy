package Blockecho;

import java.security.PublicKey;

public class TransactionOutput {
	public String id;
	public PublicKey reciepient; //new owner of these messages.
	public float value; 
	public String parentTransactionId; //the id of the transaction this output was created in
	
	//Constructor
	public TransactionOutput(PublicKey reciepient, float value, String parentTransactionId) {
		this.reciepient = reciepient;
		this.value = value;
		this.parentTransactionId = parentTransactionId;
		this.id = StringUtil.applySha256(StringUtil.getStringFromKey(reciepient)+Float.toString(value)+parentTransactionId);
	}
	
	
	public boolean isMine(PublicKey publicKey) {
		return (publicKey == reciepient);
	}
	
}
