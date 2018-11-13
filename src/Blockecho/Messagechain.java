package Blockecho;

import java.security.Security;
import java.util.ArrayList;
//import java.util.Base64;
import java.util.HashMap;


import javafx.application.Application;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Messagechain extends Application {
	
	public static ArrayList<Block> blockchain = new ArrayList<Block>();
	public static HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();
	
	public static int difficulty =3;
	public static float minimumTransaction = 0.1f;
	public static Inbox sender;
	public static Inbox receiver;
	public static Transaction genesisTransaction;
	private static String hash;
	Button button;
	Stage window;
	Scene scene;
	ListView<String> listview,listview2;
	public static void main(String[] args) {	
		
		//add our blocks to the blockchain ArrayList:
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); //Setup Bouncey castle as a Security Provider
		
		//Create inboxes:
		sender = new Inbox();
		receiver = new Inbox();		
		Inbox message = new Inbox();
		//create genesis transaction, which sends message
		genesisTransaction = new Transaction(message.publicKey, sender.publicKey, "Hello Dell", null);
		genesisTransaction.generateSignature(message.privateKey);	 //manually sign the genesis transaction	
		genesisTransaction.transactionId = "0"; //manually set the transaction id
		genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.reciepient, genesisTransaction.msg, genesisTransaction.transactionId)); //manually add the Transactions Output
		UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0)); //its important to store our first transaction in the UTXOs list.
		
		System.out.println("Creating and Mining Genesis block... ");
		Block genesis = new Block("0");
		genesis.addTransaction(genesisTransaction);
		addBlock(genesis);
		hash = genesis.hash;
		launch(args);

		
		
	}
	
	public static Boolean isChainValid() {
		Block currentBlock; 
		Block previousBlock;
		String hashTarget = new String(new char[difficulty]).replace('\0', '0');
		HashMap<String,TransactionOutput> tempUTXOs = new HashMap<String,TransactionOutput>(); //a temporary working list of unspent transactions at a given block state.
		tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));
		
		//loop through blockchain to check hashes:
		for(int i=1; i < blockchain.size(); i++) {
			
			currentBlock = blockchain.get(i);
			previousBlock = blockchain.get(i-1);
			//compare registered hash and calculated hash:
			if(!currentBlock.hash.equals(currentBlock.calculateHash()) ){
				System.out.println("#Current Hashes not equal");
				return false;
			}
			//compare previous hash and registered previous hash
			if(!previousBlock.hash.equals(currentBlock.previousHash) ) {
				System.out.println("#Previous Hashes not equal");
				return false;
			}
			//check if hash is solved
			if(!currentBlock.hash.substring( 0, difficulty).equals(hashTarget)) {
				System.out.println("#This block hasn't been mined");
				return false;
			}
			
			//loop thru blockchains transactions:
			TransactionOutput tempOutput;
			for(int t=0; t <currentBlock.transactions.size(); t++) {
				Transaction currentTransaction = currentBlock.transactions.get(t);
				
				if(!currentTransaction.verifySignature()) {
					System.out.println("#Signature on Transaction(" + t + ") is Invalid");
					return false; 
				}
			
				for(TransactionInput input: currentTransaction.inputs) {	
					tempOutput = tempUTXOs.get(input.transactionOutputId);
					
					if(tempOutput == null) {
						System.out.println("#Referenced input on Transaction(" + t + ") is Missing");
						return false;
					}
					
					if(input.UTXO.msg != tempOutput.msg) {
						System.out.println("#Referenced input Transaction(" + t + ") value is Invalid");
						return false;
					}
					
					tempUTXOs.remove(input.transactionOutputId);
				}
				
				for(TransactionOutput output: currentTransaction.outputs) {
					tempUTXOs.put(output.id, output);
				}
				
				if( currentTransaction.outputs.get(0).reciepient != currentTransaction.reciepient) {
					System.out.println("#Transaction(" + t + ") output reciepient is not who it should be");
					return false;
				}

				
			}
			
		}
		System.out.println("Blockchain is valid");
		return true;
	}
	
	public static void addBlock(Block newBlock) {
		newBlock.mineBlock(difficulty);
		blockchain.add(newBlock);
	}

	@Override
	public void start(Stage primarystage) throws Exception {
		// TODO Auto-generated method stub
		window = primarystage;
		window.setTitle("message sender");
		button = new Button();
		listview = new ListView<>();
		TextField messageinput = new TextField();
		Text textview = new Text();
		textview.setText("Sent Messages");
		Text textview2 = new Text();
		textview2.setText("Received Messages");
		button.setText("Send");
		button.setOnAction(e-> sendtypedmessage(messageinput.getText()));
		
		
		VBox layout = new VBox(10);
	    layout.setPadding(new Insets(20, 20, 20, 20));
	    layout.getChildren().addAll(messageinput, button,textview,listview);
	    scene = new Scene(layout, 300, 250);
	    window.setScene(scene);
	    window.show();
	    Stage secondStage = new Stage();
	    Stage window2 = secondStage;
	    window2.setTitle("message receiver");
	    listview2 = new ListView<>();
	    VBox layout2 = new VBox(10);
	    layout2.setPadding(new Insets(20, 20, 20, 20));
	    layout2.getChildren().addAll(textview2,listview2);
	    Scene scene2 = new Scene(layout2, 300, 250);
	    window2.setScene(scene2);
	    window2.show();
        		
	    
	}

	

	private Object sendtypedmessage(String text) {
		// TODO Auto-generated method stub
		
		Block block = new Block(hash);
		block.addTransaction(sender.sendmessages(receiver.publicKey, text));
		addBlock(block);
		System.out.println("receiver:" + receiver.getMessage());
		listview2.getItems().add(receiver.getMessage());
		hash = block.hash;
		Block block2 = new Block(hash);
		block2.addTransaction(receiver.sendmessages(sender.publicKey, "message delivered"));
		addBlock(block2);
		System.out.println("sender:" + sender.getMessage());
		hash = block2.hash;
		listview.getItems().add(text);
		
		isChainValid();
		Alertbox.display();
		
		return null;
	}

	
	
	
}