set -ev

# Shut down the Docker containers that might be currently running.
docker-compose -f docker-compose.yml stop


# Shut down the Docker containers for the system tests.
docker-compose -f docker-compose.yml kill && docker-compose -f docker-compose.yml down
if [ "$(docker ps -aq)" ]; then
	docker rm -f $(docker ps -aq)
fi

# remove chaincode docker images
if [ "$(docker images dev-* -q)" ]; then
	docker rmi $(docker images dev-* -q)
fi

# Your system is now clean

#Start from here
echo -e "\nStopping the previous network (if any)"
docker-compose -f docker-compose.yml down

# If need to re-generate the artifacts, uncomment the following lines and run
#
# cryptogen generate --config=./crypto-config.yaml
# mkdir config
# configtxgen -profile TwoOrgsOrdererGenesis -outputBlock ./config/genesis.block
# configtxgen -profile TwoOrgsChannel -outputCreateChannelTx ./config/channel.tx -channelID mychannel
# configtxgen -profile TwoOrgsChannel -outputAnchorPeersUpdate ./config/Org1MSPanchors.tx -channelID $CHANNEL_NAME -asOrg Org1MSP
# configtxgen -profile TwoOrgsChannel -outputAnchorPeersUpdate ./config/Org2MSPanchors.tx -channelID $CHANNEL_NAME -asOrg Org2MSP
#
# Create and Start the Docker containers for the network
echo -e "\nSetting up the Hyperledger Fabric 1.1 network"
docker-compose -f docker-compose.yml up -d
sleep 10
echo -e "\nNetwork setup completed!!\n"