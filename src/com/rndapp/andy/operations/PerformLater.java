package com.rndapp.andy.operations;

import android.content.Context;
import android.content.Intent;

public class PerformLater extends Operation {
	
	public PerformLater(){
		super();
		String[] strs = {"Hour","Minute","Month","Day","Year"};
		this.strings = strs;
	}
	
	public void run(){
		
	}

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		next.context = this.context;
		//strings
		next.run();
	}
}
