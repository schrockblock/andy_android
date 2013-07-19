package com.rndapp.andy;

import java.io.Serializable;

import com.rndapp.andy.operations.Operation;

public class Association implements Serializable{
	public static final int NOT_IN_COMMAND = Integer.MAX_VALUE - 1;
	public static final int AT_END_OF_STRING = Integer.MAX_VALUE;
	public static final int AT_BEGINNING_OF_STRING = Integer.MIN_VALUE;
	public Operation op;
	public String string;
	public Input[] inputs;
	public int grove;
	
	@Override
	public boolean equals(Object o){
		if (o==this){
			return true;
		}
		if (!(o instanceof Association)){
			return false;
		}
		if (!string.equals(((Association)o).string) || !op.equals(((Association) o).op) 
				|| inputs.length != ((Association) o).inputs.length
				/*|| !relPos.equals(((Association) o).relPos)
				|| !relEndPos.equals(((Association) o).relEndPos)*/){
			return false;
		}
		return true;
	}
}
