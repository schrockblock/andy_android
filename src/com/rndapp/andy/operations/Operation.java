package com.rndapp.andy.operations;

import java.io.Serializable;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Operation extends BroadcastReceiver implements Runnable, Serializable {
	public Operation next;
	public Context context;
	public String[] strings;
	
	public Operation(){
		super();
	}
	
	public Operation(Operation next, Context ctxt, String[] strs){
		super();
		
		this.next = next;
		context = ctxt;
		strings = strs;
	}
	
	public void run(){
		
	}
	
	@Override
	public boolean equals(Object o){
		if (o==this){
			return true;
		}
		if (!(o instanceof Operation)){
			return false;
		}
		if (!strings.equals(((Operation)o).strings) || !next.equals(((Operation) o).next)){
			return false;
		}
		return true;
	}

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		
	}
}
