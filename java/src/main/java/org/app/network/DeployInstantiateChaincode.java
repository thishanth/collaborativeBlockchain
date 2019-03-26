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
package org.app.network;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.app.client.ChannelClient;
import org.app.client.FabricClient;
import org.app.config.Config;
import org.app.user.UserContext;
import org.app.util.Util;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.TransactionRequest.Type;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

/**
 * 
 * @author Balaji Kadambi
 *
 */

public class DeployInstantiateChaincode {

	public static void main(String[] args) {
		try {
			CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
			
			UserContext org1Admin = new UserContext();
			File pkFolder1 = new File(Config.ORG1_USR_ADMIN_PK);
			File[] pkFiles1 = pkFolder1.listFiles();
			File certFolder = new File(Config.ORG1_USR_ADMIN_CERT);
			File[] certFiles = certFolder.listFiles();
			Enrollment enrollOrg1Admin = Util.getEnrollment(Config.ORG1_USR_ADMIN_PK, pkFiles1[0].getName(),
					Config.ORG1_USR_ADMIN_CERT, certFiles[0].getName());
			org1Admin.setEnrollment(enrollOrg1Admin);
			org1Admin.setMspId("Org1MSP");
			org1Admin.setName("admin");

			UserContext org2Admin = new UserContext();
			File pkFolder2 = new File(Config.ORG2_USR_ADMIN_PK);
			File[] pkFiles2 = pkFolder2.listFiles();
			File certFolder2 = new File(Config.ORG2_USR_ADMIN_CERT);
			File[] certFiles2 = certFolder2.listFiles();
			Enrollment enrollOrg2Admin = Util.getEnrollment(Config.ORG2_USR_ADMIN_PK, pkFiles2[0].getName(),
					Config.ORG2_USR_ADMIN_CERT, certFiles2[0].getName());
			org2Admin.setEnrollment(enrollOrg2Admin);
			org2Admin.setMspId(Config.ORG2_MSP);
			org2Admin.setName(Config.ADMIN);
			
			UserContext org3Admin = new UserContext();
			File pkFolder3 = new File(Config.ORG3_USR_ADMIN_PK);
			File[] pkFiles3 = pkFolder3.listFiles();
			File certFolder3 = new File(Config.ORG3_USR_ADMIN_CERT);
			File[] certFiles3 = certFolder3.listFiles();
			Enrollment enrollOrg3Admin = Util.getEnrollment(Config.ORG3_USR_ADMIN_PK, pkFiles3[0].getName(),
					Config.ORG3_USR_ADMIN_CERT, certFiles3[0].getName());
			org3Admin.setEnrollment(enrollOrg3Admin);
			org3Admin.setMspId(Config.ORG3_MSP);
			org3Admin.setName(Config.ADMIN);

			UserContext org4Admin = new UserContext();
			File pkFolder4 = new File(Config.ORG4_USR_ADMIN_PK);
			File[] pkFiles4 = pkFolder4.listFiles();
			File certFolder4 = new File(Config.ORG4_USR_ADMIN_CERT);
			File[] certFiles4 = certFolder4.listFiles();
			Enrollment enrollOrg4Admin = Util.getEnrollment(Config.ORG4_USR_ADMIN_PK, pkFiles4[0].getName(),
					Config.ORG4_USR_ADMIN_CERT, certFiles4[0].getName());
			org4Admin.setEnrollment(enrollOrg4Admin);
			org4Admin.setMspId(Config.ORG4_MSP);
			org4Admin.setName(Config.ADMIN);
			

			FabricClient fabClient = new FabricClient(org1Admin);

			Channel mychannel = fabClient.getInstance().newChannel(Config.CHANNEL_NAME);
			Orderer orderer = fabClient.getInstance().newOrderer(Config.ORDERER_NAME, Config.ORDERER_URL);
			Peer peer0_org1 = fabClient.getInstance().newPeer(Config.ORG1_PEER_0, Config.ORG1_PEER_0_URL);
			Peer peer1_org1 = fabClient.getInstance().newPeer(Config.ORG1_PEER_1, Config.ORG1_PEER_1_URL);
			Peer peer0_org2 = fabClient.getInstance().newPeer(Config.ORG2_PEER_0, Config.ORG2_PEER_0_URL);
			Peer peer1_org2 = fabClient.getInstance().newPeer(Config.ORG2_PEER_1, Config.ORG2_PEER_1_URL);

			Peer peer0_org3 = fabClient.getInstance().newPeer(Config.ORG3_PEER_0, Config.ORG3_PEER_0_URL);
			Peer peer1_org3 = fabClient.getInstance().newPeer(Config.ORG3_PEER_1, Config.ORG3_PEER_1_URL);
			Peer peer0_org4 = fabClient.getInstance().newPeer(Config.ORG4_PEER_0, Config.ORG4_PEER_0_URL);
			Peer peer1_org4 = fabClient.getInstance().newPeer(Config.ORG4_PEER_1, Config.ORG4_PEER_1_URL);

			mychannel.addOrderer(orderer);
			mychannel.addPeer(peer0_org1);
			mychannel.addPeer(peer1_org1);
			mychannel.addPeer(peer0_org2);
			mychannel.addPeer(peer1_org2);
			mychannel.addPeer(peer0_org3);
			mychannel.addPeer(peer1_org3);
			mychannel.addPeer(peer0_org4);
			mychannel.addPeer(peer1_org4);
			mychannel.initialize();

			List<Peer> org1Peers = new ArrayList<Peer>();
			org1Peers.add(peer0_org1);
			org1Peers.add(peer1_org1);
			
			List<Peer> org2Peers = new ArrayList<Peer>();
			org2Peers.add(peer0_org2);
			org2Peers.add(peer1_org2);
			
			List<Peer> org3Peers = new ArrayList<Peer>();
			org3Peers.add(peer0_org3);
			org3Peers.add(peer1_org3);
			
			List<Peer> org4Peers = new ArrayList<Peer>();
			org4Peers.add(peer0_org4);
			org4Peers.add(peer1_org4);
			

			Collection<ProposalResponse> response = fabClient.deployChainCode(Config.CHAINCODE_1_NAME,
					Config.CHAINCODE_1_PATH, Config.CHAINCODE_ROOT_DIR, Type.GO_LANG.toString(),
					Config.CHAINCODE_1_VERSION, org1Peers);
			
			
			for (ProposalResponse res : response) {
				Logger.getLogger(DeployInstantiateChaincode.class.getName()).log(Level.INFO,
						Config.CHAINCODE_1_NAME + "- Chain code deployment " + res.getStatus());
			}

			fabClient.getInstance().setUserContext(org2Admin);
			
			response = fabClient.deployChainCode(Config.CHAINCODE_1_NAME,
					Config.CHAINCODE_1_PATH, Config.CHAINCODE_ROOT_DIR, Type.GO_LANG.toString(),
					Config.CHAINCODE_1_VERSION, org2Peers);
			
			
			for (ProposalResponse res : response) {
				Logger.getLogger(DeployInstantiateChaincode.class.getName()).log(Level.INFO,
						Config.CHAINCODE_1_NAME + "- Chain code deployment " + res.getStatus());
			}
			

			fabClient.getInstance().setUserContext(org3Admin);
			
			response = fabClient.deployChainCode(Config.CHAINCODE_1_NAME,
					Config.CHAINCODE_1_PATH, Config.CHAINCODE_ROOT_DIR, Type.GO_LANG.toString(),
					Config.CHAINCODE_1_VERSION, org3Peers);
			
			
			for (ProposalResponse res : response) {
				Logger.getLogger(DeployInstantiateChaincode.class.getName()).log(Level.INFO,
						Config.CHAINCODE_1_NAME + "- Chain code deployment " + res.getStatus());
			}

			fabClient.getInstance().setUserContext(org4Admin);
			
			response = fabClient.deployChainCode(Config.CHAINCODE_1_NAME,
					Config.CHAINCODE_1_PATH, Config.CHAINCODE_ROOT_DIR, Type.GO_LANG.toString(),
					Config.CHAINCODE_1_VERSION, org4Peers);
			
			
			for (ProposalResponse res : response) {
				Logger.getLogger(DeployInstantiateChaincode.class.getName()).log(Level.INFO,
						Config.CHAINCODE_1_NAME + "- Chain code deployment " + res.getStatus());
			}

			//Switching to Org1Admin
			fabClient.getInstance().setUserContext(org1Admin);

			//TODO : Remove. Not required
			Thread.sleep(2000);

			ChannelClient channelClient = new ChannelClient(mychannel.getName(), mychannel, fabClient);

			String[] arguments = { "" };
			response = channelClient.instantiateChainCode(Config.CHAINCODE_1_NAME, Config.CHAINCODE_1_VERSION,
					Config.CHAINCODE_1_PATH, Type.GO_LANG.toString(), "init", arguments, null);

			for (ProposalResponse res : response) {
				Logger.getLogger(DeployInstantiateChaincode.class.getName()).log(Level.INFO,
						Config.CHAINCODE_1_NAME + "- Chain code instantiation " + res.getStatus());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
