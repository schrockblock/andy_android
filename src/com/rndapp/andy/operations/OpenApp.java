package com.rndapp.andy.operations;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

public class OpenApp extends Operation {
	
	public OpenApp(){
		super();
		String[] strs = {"Name of App"};
		this.strings = strs;
	}
	
	public OpenApp(Context ctxt){
		super();
		context = ctxt;
		String[] strs = {"Name of App"};
		this.strings = strs;
	}
	
	public OpenApp(Operation next, Context ctxt, String[] strs){
		super();
		
		this.next = next;
		context = ctxt;
		this.strings = strs;
	}
	
	@Override
	public void run(){
		String name = this.strings[0].toLowerCase();
		PackageManager pm = context.getPackageManager();
		List<ApplicationInfo> l = pm.getInstalledApplications(PackageManager.GET_META_DATA);
		String canonicalName = "";
		double mostSimilar = Double.MIN_VALUE;
		for (ApplicationInfo ai : l){
			String n = ((String)pm.getApplicationLabel(ai)).toLowerCase();
			if (n.contains(name) || name.contains(n)){
				double sim = similarity((String)pm.getApplicationLabel(ai), this.strings[0]);
				if (sim >= mostSimilar){
					canonicalName = ai.processName;
					mostSimilar = sim;
				}
			}
		}

		//do whatever you want with canonicalName, e.g. launch the app,
		Intent app = context.getPackageManager().getLaunchIntentForPackage(canonicalName);
		if (app != null){
			context.startActivity(app);
		}
	}
	
	private double similarity(String ref, String comp){
		double sim = 0;
		if (ref.equals(comp)){
			return Double.MAX_VALUE;
		}
		sim -= Math.abs(ref.length() - comp.length());
		for (int m = 1; m<comp.length(); m++){
			for (int n = 0; n<comp.length()-m; n++){
				String test = comp.substring(n, n+m);
				for (int i = 0; i<ref.length()-m; i++){
					if (test.equals(ref.substring(i, i+m))){
						sim += m;
						if (i==n){
							sim++;
						}
					}
				}
			}
		}
		return sim;
	}
}
