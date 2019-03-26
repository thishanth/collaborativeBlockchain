package org.app.chaincode.invocation;

import java.util.concurrent.atomic.AtomicInteger;

public class TransactionStatistics{

	public static AtomicInteger totalTransactions = new AtomicInteger();
	public static AtomicInteger successfulTransactions = new AtomicInteger();
	public static AtomicInteger failedTransactions = new AtomicInteger();
}