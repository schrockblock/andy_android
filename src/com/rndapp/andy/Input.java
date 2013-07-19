package com.rndapp.andy;

import java.io.Serializable;
import java.util.Vector;

public class Input implements Serializable{
	public String string;
	public Vector<Integer> startPos;
	public Vector<Integer> endPos;
	public Vector<Integer> startGroove;
	public Vector<Integer> endGroove;
	
	public Input(){
		super();
		startPos = new Vector<Integer>();
		endPos = new Vector<Integer>();
		startGroove = new Vector<Integer>();
		endGroove = new Vector<Integer>();
	}

}
