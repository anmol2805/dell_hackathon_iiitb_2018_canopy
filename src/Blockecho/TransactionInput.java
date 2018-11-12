package Blockecho;


public class TransactionInput {
		public String transactionOutputId; //Reference to TransactionOutputs -> transactionId
		public TransactionOutput UTXO; //Contains the transaction output
		
		public TransactionInput(String transactionOutputId) {
			this.transactionOutputId = transactionOutputId;
		}
}

