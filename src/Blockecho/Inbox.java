package Blockecho;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Inbox {
	
	public PrivateKey privateKey;
	public PublicKey publicKey;
	
	public HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();
	
	public Inbox() {
		generateKeyPair();
	}
		
	public void generateKeyPair() {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
			// Initialize the key generator and generate a KeyPair
			keyGen.initialize(ecSpec, random); //256 
	        KeyPair keyPair = keyGen.generateKeyPair();
	        // Set the public and private keys from the keyPair
	        privateKey = keyPair.getPrivate();
	        publicKey = keyPair.getPublic();
	        
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public String getMessage() {
		String finalmsg = "";	
        for (Map.Entry<String, TransactionOutput> item: Messagechain.UTXOs.entrySet()){
        	TransactionOutput UTXO = item.getValue();
            if(UTXO.isMine(publicKey)) { //if message belongs to me
            	UTXOs.put(UTXO.id,UTXO); 
            	finalmsg = UTXO.msg ; 
            }
        }  
		return finalmsg;
	}
	
	public Transaction sendmessages(PublicKey _recipient,String msg ) {
		
		ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
		
		String finalmsg = "";
		for (Map.Entry<String, TransactionOutput> item: UTXOs.entrySet()){
			TransactionOutput UTXO = item.getValue();
			finalmsg = UTXO.msg;
			inputs.add(new TransactionInput(UTXO.id));
		
		}
		
		Transaction newTransaction = new Transaction(publicKey, _recipient , msg, inputs);
		newTransaction.generateSignature(privateKey);
		
		for(TransactionInput input: inputs){
			UTXOs.remove(input.transactionOutputId);
		}
		
		return newTransaction;
	}
	
}
