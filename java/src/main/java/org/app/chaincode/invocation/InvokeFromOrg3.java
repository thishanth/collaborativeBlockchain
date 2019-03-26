/****************************************************** 
 *  Copyright 2018 IBM Corporation 
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0 
 *  Unless required by applicable law or agreed to in writing, software 
 *  distributed under the License is distributed on an "AS IS" BASIS, 
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *  See the License for the specific language governing permissions and 
 *  limitations under the License.
 */ 
package org.app.chaincode.invocation;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.app.client.CAClient;
import org.app.client.ChannelClient;
import org.app.client.FabricClient;
import org.app.config.Config;
import org.app.user.UserContext;
import org.app.util.Util;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.ChaincodeResponse.Status;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.EventHub;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;

import java.util.concurrent.TimeUnit;
/**
 * 
 * @author Balaji Kadambi
 *
 */

public class InvokeFromOrg3 implements Runnable {

	private Thread thread;
	private String threadName;
	private TransactionStatistics transactions;
	private Date startTime;

	private static final byte[] EXPECTED_EVENT_DATA = "!".getBytes(UTF_8);
	private static final String EXPECTED_EVENT_NAME = "event";

	public InvokeFromOrg3(String threadName, Date s)
	{
		this.threadName=threadName;
		transactions=new TransactionStatistics();
		System.out.println("Creating thread "+this.threadName);
		 startTime = s;
	}

	public void start(){
		System.out.println("Starting thread "+this.threadName);
		if(thread==null){
			thread=new Thread(this,threadName);
			thread.start();
		}
	}

	public void run() {
		System.out.println("Running thread "+this.threadName);

		//State variables
		long transactionStartTime = System.nanoTime();

		try {
            Util.cleanUp();
			String caUrl = Config.CA_ORG3_URL;
			CAClient caClient = new CAClient(caUrl, null);
			// Enroll Admin to ORG3MSP
			UserContext adminUserContext = new UserContext();
			adminUserContext.setName(Config.ADMIN);
			adminUserContext.setAffiliation(Config.ORG3);
			adminUserContext.setMspId(Config.ORG3_MSP);
			caClient.setAdminUserContext(adminUserContext);
			adminUserContext = caClient.enrollAdminUser(Config.ADMIN, Config.ADMIN_PASSWORD);
			
			FabricClient fabClient = new FabricClient(adminUserContext);
			
			ChannelClient channelClient = fabClient.createChannelClient(Config.CHANNEL_NAME);
			Channel channel = channelClient.getChannel();
			Peer peer = fabClient.getInstance().newPeer(Config.ORG3_PEER_0, Config.ORG3_PEER_0_URL);
			EventHub eventHub = fabClient.getInstance().newEventHub("eventhub01", "grpc://localhost:9053");
			Orderer orderer = fabClient.getInstance().newOrderer(Config.ORDERER_NAME, Config.ORDERER_URL);
			channel.addPeer(peer);
			channel.addEventHub(eventHub);
			channel.addOrderer(orderer);
			channel.initialize();


		/*
		*	Configuration properties
		*
		*/
		final int MAX_ROW=20;
		final int MAX_COLUMN=20;
		final int RUN_TIME= 1 ; //in minutes


		final String[] colors={"Red","Blue","Green","Yellow"};
		final String[] operations={"createTile","createTile"};
		final String[] points={"2","4"};
		

		Random rand= new Random();
		final long NANOSEC_PER_SEC = 1000l*1000*1000;

			int randomRow=0;
			int randomColumn=0;
			int randomColor=0;
			int randomPoint=0;
			int randomOperation=0;
		
			String lockId = null;

			boolean isLockObtained=false;

		//Run loop for 0.5 mins(30 seconds)		
			Boolean stillHasTimeToRun = true;
			while (stillHasTimeToRun){
			if (new Date(System.currentTimeMillis()).after(startTime)){stillHasTimeToRun = false; return;}
		 
			if(!isLockObtained)
			{

  			lockId=UUID.randomUUID().toString();

  			randomRow=rand.nextInt(MAX_ROW);
			randomColumn=rand.nextInt(MAX_COLUMN);
			randomColor= rand.nextInt(colors.length);
			randomPoint=rand.nextInt(points.length);
			randomOperation=rand.nextInt(operations.length);
			}

		try{
			
			TransactionProposalRequest request = fabClient.getInstance().newTransactionProposalRequest();
			ChaincodeID ccid = ChaincodeID.newBuilder().setName(Config.CHAINCODE_1_NAME).build();
			request.setChaincodeID(ccid);
					
			String[] arguments = null;
			
			if(isLockObtained)
			{
				//Set operation
				request.setFcn(operations[randomOperation]);
				
				if(operations[randomOperation].equalsIgnoreCase("createTile"))
					arguments=new String[]{String.valueOf(randomRow),String.valueOf(randomColumn),colors[randomColor],points[randomPoint],lockId};
				else if(operations[randomOperation].equalsIgnoreCase("deleteTile"))
					arguments=new String[]{String.valueOf(randomRow),String.valueOf(randomColumn),lockId};
				isLockObtained=false;
			}
			else {
				request.setFcn("lockTile");
				arguments =new String[]{String.valueOf(randomRow),String.valueOf(randomColumn),lockId};
				isLockObtained=true;
			}
			request.setArgs(arguments);
			request.setProposalWaitTime(1000);

			Map<String, byte[]> tm2 = new HashMap<>();
			tm2.put("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8)); 																								
			tm2.put("method", "TransactionProposalRequest".getBytes(UTF_8)); 
			tm2.put("result", ":)".getBytes(UTF_8));
			tm2.put(EXPECTED_EVENT_NAME, EXPECTED_EVENT_DATA); 
			request.setTransientMap(tm2);
			transactionStartTime = System.nanoTime();
			Collection<ProposalResponse> responses = channelClient.sendTransactionProposal(request);
	    	long transactionEndTime = System.nanoTime();
			for (ProposalResponse res: responses) {
				Status status = res.getStatus();
				Logger.getLogger(InvokeChaincode.class.getName()).log(Level.INFO,"Invoked chaincode on "+Config.CHAINCODE_1_NAME + ". Status - " + status);
				System.out.println("Trasaction status : "+status);
			}
	    Thread.sleep(2000);
	    long durationInNano = (transactionEndTime - transactionStartTime);  //Total execution time in nano seconds
	    //Same duration in millis
	    long durationInMillis = TimeUnit.NANOSECONDS.toMillis(durationInNano);  //Total execution time in nano seconds
	    
			FinalStats.syncAll(1, durationInMillis);
		
		}catch(Exception ex)
		{
			//ex.printStackTrace();
			System.out.println("Transaction failed !!!");
			isLockObtained=false;

		}

		}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}

