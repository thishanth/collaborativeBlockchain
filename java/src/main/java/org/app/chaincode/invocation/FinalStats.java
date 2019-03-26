package org.app.chaincode.invocation;

public class FinalStats
{
	private static long totalTransaction = 0;
	private static long totalLatency = 0;
	public static synchronized void syncAll(long val, long val2)
	{
		totalTransaction += val;
		totalLatency += val2;
		System.out.println("**************************************");
		System.out.println ("Transaction (tps) : "+(totalTransaction/(Executer.minsToRun * 60)));
		System.out.println ("Latency (sec) : "+(totalLatency/1000));
		System.out.println("**************************************");

	}
}