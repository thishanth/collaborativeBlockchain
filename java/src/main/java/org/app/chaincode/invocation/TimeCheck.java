package org.app.chaincode.invocation;

import java.util.Date;

public class TimeCheck
{
	public static void main(String[] args)
	{
		Date now = new Date(System.currentTimeMillis());
		Date after = new Date(System.currentTimeMillis()+1*60*1000);

		System.out.println("hello"+ now);
		System.out.println("hello"+ after);

		if(now.after(after)){
			System.out.println("Date1 is after Date2");
		}

		if(now.before(after)){
			System.out.println("Date1 is before Date2");
		}

		if(now.equals(after)){
			System.out.println("Date1 is equal Date2");
		}
	}
}