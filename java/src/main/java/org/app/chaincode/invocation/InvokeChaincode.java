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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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
import java.util.UUID;
/**
 * 
 * @author Balaji Kadambi
 *
 */

public class InvokeChaincode {

	private static final byte[] EXPECTED_EVENT_DATA = "!".getBytes(UTF_8);
	private static final String EXPECTED_EVENT_NAME = "event";

	public static void main(String args[]) {
		try {
            Util.cleanUp();
			String caUrl = Config.CA_ORG3_URL;
			CAClient caClient = new CAClient(caUrl, null);
			// Enroll Admin to Org1MSP
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


		//Obtain lock or release lock
		//if(args[0].equals("lockTile") || args[0].equals("unlockTile"))
		//{
			final String lockId = UUID.randomUUID().toString();

			TransactionProposalRequest request2 = fabClient.getInstance().newTransactionProposalRequest();
			ChaincodeID ccid2 = ChaincodeID.newBuilder().setName(Config.CHAINCODE_1_NAME).build();
			request2.setChaincodeID(ccid2);
			request2.setFcn("lockTile");
			String[] arguments2 = {args[1],args[2],lockId};
			request2.setArgs(arguments2);
			request2.setProposalWaitTime(1000);

			Map<String, byte[]> tm3 = new HashMap<>();
			tm3.put("HyperLedgerFabric", "TransactionProposalrequest2:JavaSDK".getBytes(UTF_8)); 																								
			tm3.put("method", "TransactionProposalrequest2".getBytes(UTF_8)); 
			tm3.put("result", ":)".getBytes(UTF_8));
			tm3.put(EXPECTED_EVENT_NAME, EXPECTED_EVENT_DATA); 
			request2.setTransientMap(tm3);
			Collection<ProposalResponse> responses2 = channelClient.sendTransactionProposal(request2);
			for (ProposalResponse res: responses2) {
				Status status2 = res.getStatus();
				Logger.getLogger(InvokeChaincode.class.getName()).log(Level.INFO,"Invoked createTile on "+Config.CHAINCODE_1_NAME + ". Status - " + status2);
			} 

			Thread.sleep(20000);
		//}else
		//{
			
			TransactionProposalRequest request = fabClient.getInstance().newTransactionProposalRequest();
			ChaincodeID ccid = ChaincodeID.newBuilder().setName(Config.CHAINCODE_1_NAME).build();
			request.setChaincodeID(ccid);
			request.setFcn(args[0]);
			//String[] arguments = {"1", "1", "1", "Green", "2", "false" };
			//String[] arguments = {"0", "0", "Blue","2", "false" };
	
			String[] arguments = null;
			if(args[0].equalsIgnoreCase("createTile"))
				arguments=new String[]{args[1],args[2],args[3],args[4], lockId};
			else if(args[0].equalsIgnoreCase("deleteTile"))
				arguments=new String[]{args[1],args[2],lockId};
		//	else if(args[0].equalsIgnoreCase("lockTile"))
		//		arguments=new String[]{args[1],args[2],args[3]};

			request.setArgs(arguments);
			request.setProposalWaitTime(1000);

			Map<String, byte[]> tm2 = new HashMap<>();
			tm2.put("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8)); 																								
			tm2.put("method", "TransactionProposalRequest".getBytes(UTF_8)); 
			tm2.put("result", ":)".getBytes(UTF_8));
			tm2.put(EXPECTED_EVENT_NAME, EXPECTED_EVENT_DATA); 
			request.setTransientMap(tm2);
			Collection<ProposalResponse> responses = channelClient.sendTransactionProposal(request);
			for (ProposalResponse res: responses) {
				Status status = res.getStatus();
				Logger.getLogger(InvokeChaincode.class.getName()).log(Level.INFO,"Invoked createTile on "+Config.CHAINCODE_1_NAME + ". Status - " + status);
			}

		//}
			/**Thread.sleep(3000);


			TransactionProposalRequest request2 = fabClient.getInstance().newTransactionProposalRequest();
			ChaincodeID ccid2 = ChaincodeID.newBuilder().setName(Config.CHAINCODE_1_NAME).build();
			request2.setChaincodeID(ccid2);
			request2.setFcn("createTile");
			//String[] arguments2 = {"1", "1", "1", "Green", "2", "false" };
			String[] arguments2 = {"0", "0", "Green","4", "false" };
			request2.setArgs(arguments2);
			request2.setProposalWaitTime(1000);

			Map<String, byte[]> tm3 = new HashMap<>();
			tm3.put("HyperLedgerFabric", "TransactionProposalrequest2:JavaSDK".getBytes(UTF_8)); 																								
			tm3.put("method", "TransactionProposalrequest2".getBytes(UTF_8)); 
			tm3.put("result", ":)".getBytes(UTF_8));
			tm3.put(EXPECTED_EVENT_NAME, EXPECTED_EVENT_DATA); 
			request2.setTransientMap(tm3);
			Collection<ProposalResponse> responses2 = channelClient.sendTransactionProposal(request2);
			for (ProposalResponse res: responses2) {
				Status status2 = res.getStatus();
				Logger.getLogger(InvokeChaincode.class.getName()).log(Level.INFO,"Invoked createTile on "+Config.CHAINCODE_1_NAME + ". Status - " + status2);
			} 
			**/
					
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

