package Blockecho;

import java.security.*;
import java.util.ArrayList;

public class Transaction {
	
	public String transactionId; //Contains a hash of transaction*
	public PublicKey sender; //Senders address/public key.
	public PublicKey reciepient; //Recipients address/public key.
	public String msg;
	public byte[] signature; //This is to prevent anybody else from sending messages from our inbox.
	
	public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
	public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();
	
	private static int sequence = 0; //A rough count of how many transactions have been generated 
	
	// Constructor: 
	public Transaction(PublicKey from, PublicKey to, String msg,  ArrayList<TransactionInput> inputs) {
		this.sender = from;
		this.reciepient = to;
		this.msg = msg;
		this.inputs = inputs;
	}
	
	public boolean processTransaction() {
		
		if(verifySignature() == false) {
			System.out.println("#Transaction Signature failed to verify");
			return false;
		}
				
		//Gathers transaction inputs
		for(TransactionInput i : inputs) {
			i.UTXO = Messagechain.UTXOs.get(i.transactionOutputId);
		}


		
		//Generate transaction outputs:

		transactionId = calulateHash();
		outputs.add(new TransactionOutput( this.reciepient, msg,transactionId)); //send value to recipient
		
				
		
		for(TransactionOutput o : outputs) {
			Messagechain.UTXOs.put(o.id , o);
		}
		
		
		for(TransactionInput i : inputs) {
			if(i.UTXO == null) continue; //if Transaction can't be found skip it 
			Messagechain.UTXOs.remove(i.UTXO.id);
		}
		
		return true;
	}

	
	public void generateSignature(PrivateKey privateKey) {
		String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(reciepient) + msg	;
		signature = StringUtil.applyECDSASig(privateKey,data);		
	}
	
	public boolean verifySignature() {
		String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(reciepient) + msg	;
		return StringUtil.verifyECDSASig(sender, data, signature);
	}
	

	private String calulateHash() {
		sequence++; //increase the sequence to avoid 2 identical transactions having the same hash
		return StringUtil.applySha256(
				StringUtil.getStringFromKey(sender) +
				StringUtil.getStringFromKey(reciepient) +
				msg + sequence
				);
	}
}