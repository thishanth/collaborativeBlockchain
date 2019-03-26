package org.app.chaincode.invocation;
import java.util.Date;
import java.util.concurrent.*;

public class Executer{

	public static int minsToRun = 3;
	public static void main(String args[])
	{
		int totalThreads=Integer.parseInt(args[0]);
		Date endTime = new Date(System.currentTimeMillis()+minsToRun*60*1000);
		
		for(int i=0;i<totalThreads;i++)
		{
			try{
			InvokeFromOrg1 org1Client=new InvokeFromOrg1("Org1-Client"+i, endTime);
			InvokeFromOrg2 org2Client=new InvokeFromOrg2("Org2-Client"+i, endTime);
			InvokeFromOrg3 org3Client=new InvokeFromOrg3("Org3-Client"+i, endTime);
			InvokeFromOrg4 org4Client=new InvokeFromOrg4("Org4-Client"+i, endTime);
			Thread.sleep(1000);
			org1Client.start();
			org2Client.start();
			org3Client.start();
			org4Client.start();

		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
		}
	}
}