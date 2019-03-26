cd target

echo ==============================================================================
echo Copying blockchain client to network_resources
echo ==============================================================================

cp blockchain-java-sdk-0.0.1-SNAPSHOT-jar-with-dependencies.jar blockchain-client.jar

cp blockchain-client.jar ../../network_resources

cd ../../network_resources

echo `pwd`


echo ===============================================================================
echo Creating Channel....
echo ===============================================================================

java -cp blockchain-client.jar org.app.network.CreateChannel

echo ================================================================================
echo Deploying chaincode in progress
echo ================================================================================

java -cp blockchain-client.jar org.app.network.DeployInstantiateChaincode

echo ================================================================================
echo Registering and enrolling users
echo ================================================================================

java -cp blockchain-client.jar org.app.user.RegisterEnrollUser

#echo ================================================================================
#echo Invoking Chain code
#echo ================================================================================
#java -cp blockchain-client.jar org.app.chaincode.invocation.InvokeChaincode

#echo ================================================================================
#echo Querying chain code
#echo ================================================================================
#java -cp blockchain-client.jar org.app.chaincode.invocation.QueryChaincode
