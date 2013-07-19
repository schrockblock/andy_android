package com.rndapp.andy;

import java.util.HashMap;

import com.rndapp.andy.operations.OpenApp;
import com.rndapp.andy.operations.Operation;

import android.content.Context;

public class Operations {
	Context context;
	
	public Operations(Context ctxt){
		super();
		this.context = ctxt;
	}
	
	public HashMap<String, Operation> operations(){
		HashMap<String, Operation> map = new HashMap<String, Operation>();
		
		final String open = "open an app";
		OpenApp runOpen = new OpenApp(context);
		
		map.put(open, runOpen);
		
		return map;
	}

}
