cd target

echo ==============================================================================
echo Copying blockchain client to network_resources
echo ==============================================================================

cp blockchain-java-sdk-0.0.1-SNAPSHOT-jar-with-dependencies.jar blockchain-client.jar

cp blockchain-client.jar ../../network_resources

cd ../../network_resources
