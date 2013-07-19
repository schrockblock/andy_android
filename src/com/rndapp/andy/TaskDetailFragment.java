package com.rndapp.andy;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.rndapp.andy.dummy.DummyContent;
import com.rndapp.andy.operations.OpenApp;
import com.rndapp.andy.operations.SendText;
import com.rndapp.andy.operations.Operation;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

//TODO: do that again, same as, speak, alarmmanager -> run op
public class TaskDetailFragment extends Fragment implements OnClickListener{
	HashMap<String, Vector<Association>> associations;
	HashMap<String, Operation> history;

    public static final String ARG_ITEM_ID = "item_id";

    DummyContent.DummyItem mItem;

    public TaskDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_task_detail, container, false);
        if (mItem != null) {
            ((TextView) rootView.findViewById(R.id.task_detail)).setText(mItem.content);
            Button b = (Button)rootView.findViewById(R.id.speak);
            b.setOnClickListener(this);
        }
        
//        try {
//			FileInputStream fin = getActivity().openFileInput("associations");
//			ObjectInputStream ois = new ObjectInputStream(fin);
//			associations = (HashMap<String, Vector<Association>>) ois.readObject();
//			ois.close();
//		} catch (StreamCorruptedException e) {
//			e.printStackTrace();
//		} catch (OptionalDataException e) {
//			e.printStackTrace();
//		} catch (FileNotFoundException e) {
			associations = new HashMap<String, Vector<Association>>();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		}
        
        seedAssoc();
        
        return rootView;
    }
    
    @Override
    public void onPause(){
    	super.onPause();
    	
    	try {
			FileOutputStream fout = getActivity().openFileOutput("associations", Context.MODE_PRIVATE);
		    ObjectOutputStream oos = new ObjectOutputStream(fout);
		    oos.writeObject(associations);
		    oos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

	@Override
	public void onClick(View v) {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
	    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
	    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say command");
	    startActivityForResult(intent, 1);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
	    if (requestCode == 1 && resultCode == getActivity().RESULT_OK){
	        //grab best result
	        ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
	        
	        Log.d("Speech matches", matches.get(0));
	        runCommand(matches.get(0));
	    }
	    super.onActivityResult(requestCode, resultCode, data);
	}
	
	private void associate(String cmd, String[] inputs, Operation op){
    	for (int m = 2; m<cmd.length(); m++){
    		for (int n = 0; n<cmd.length()-m; n++){
    			String s = cmd.substring(n, n+m); //this is the associating string
    			
    			//now, for each s, we need to associate it with the op, and it's relative
    			//position w.r.t. the inputs.
    			
    			//first, make an empty association
    			Association a = new Association();
    			a.op = op;
    			a.string = s;
    			
    			//now, we build the relative positions of the inputs
    			a.inputs = new Input[inputs.length];
    			for (int i = 0; i<inputs.length; i++){
					Input in = new Input();
    				if (cmd.contains(inputs[i])){
    					if (cmd.indexOf(inputs[i]) == 0)
        					in.startPos.add(Association.AT_BEGINNING_OF_STRING);
    					else
        					in.startPos.add(cmd.indexOf(inputs[i]) - n);
    					
    					if (cmd.indexOf(inputs[i]) == cmd.length() - 1)
        					in.endPos.add(Association.AT_END_OF_STRING);
    					else
        					in.endPos.add(cmd.indexOf(inputs[i]) + inputs[i].length() - n);
    					
    					in.startGroove.add(1);
    					in.endGroove.add(1);
    				}else{
    					in.string = inputs[0];
    					in.startPos.add(Association.NOT_IN_COMMAND);
    					in.endPos.add(Association.NOT_IN_COMMAND);
    					in.startGroove.add(1);
    					in.endGroove.add(1);
    				}
    				a.inputs[i] = in;
    			}
    			
    			//finally, add the association to our hashmap
    			Vector<Association> v = associations.get(s);
    			if (v!=null && !v.contains(a)){
    				v.add(a);
    				associations.put(s, v);
    			}else if (v == null){
    				v = new Vector<Association>();
    				v.add(a);
    				associations.put(s, v);
    			}else if (v!=null && v.contains(a)){
    				Association as = v.get(v.indexOf(a));
    				as.grove++;
    				v.remove(as);
    				v.add(as);
    			}
    		}
    	}
    }

    private void runCommand(String cmd){
		HashMap<Operation, Integer> hm = new HashMap<Operation, Integer>();
		HashMap<Operation, Vector<Association>> opMap = new HashMap<Operation, Vector<Association>>();
		//int[] paramPos = new int[cmd.length()];
		//int[] endParamPos = new int[cmd.length()];
		//for each associating string,
    	for (int m = 2; m<cmd.length(); m++){
    		for (int n = 0; n<cmd.length()-m; n++){
    			String s = cmd.substring(n, n+m); //this is the associating string
    			
    			//get the associations...
    			Vector<Association> v = associations.get(s);
    			if (v != null && v.size() != 0){
    				for (int i = 0; i<v.size(); i++){
        				Association a = v.get(i);
        				if (a != null){
        					//...and their operations...
        					Operation op = a.op;
        					Integer z = hm.get(op);
        					
        					//and increment the count for that op
        					if (z == null){
        						hm.put(op, m);
        					}else{
        						hm.put(op, z+m);
        					}
        					
        					Vector<Association> opvec = opMap.get(op);
        					if (opvec == null){
        						opvec = new Vector<Association>();
        					}
        					opvec.add(a);
        					opMap.put(op, opvec);
        					
        					//finally, vote on the parameter location
//        					for (int k = 0; k<a.inputs.length; k++){
//        						for (int j = 0; j<a.inputs[k].startPos.size() && j<a.inputs[k].endPos.size();j++){
//            						if (a.inputs[k].startPos.get(j) == Association.AT_BEGINNING_OF_STRING){
//                						paramPos[0]+=a.inputs[k].startGroove.get(j);
//                					}else if (n+a.inputs[k].startPos.get(j)<cmd.length() 
//                    							&& n+a.inputs[k].startPos.get(j)>-1 
//                    							&& a.inputs[k].startPos.get(j)>=0){
//                    						paramPos[n+a.inputs[k].startPos.get(j)]+=a.inputs[k].startGroove.get(j);
//                					}
//                					if (a.inputs[0].endPos.get(j) == Association.AT_END_OF_STRING){
//                						endParamPos[cmd.length()-1]+=a.inputs[k].endGroove.get(j);
//                					}else if (n+a.inputs[k].endPos.get(j)<cmd.length() 
//                							&& n+a.inputs[k].endPos.get(j)>-1 
//                							&& a.inputs[k].endPos.get(j)<=0 ){
//                						endParamPos[n+a.inputs[k].endPos.get(j)]+=a.inputs[k].endGroove.get(j);
//                					}
//            					}
//        					}
        				}
    				}
    			}
    		}
    	}
    	
    	//now, find the op with the most votes
    	int mostVotes = 0;
    	Operation topOp = null;
    	for (Map.Entry<Operation, Integer> entry : hm.entrySet()){
    		if (entry.getValue() >= mostVotes){
    			mostVotes = entry.getValue();
    			topOp = entry.getKey();
    		}
    	}
    	
    	//great! now we need to find the parameter(s) (for now just one) to give to the op
    	if (topOp != null && (topOp.strings != null && topOp.strings.length > 0)){
    		int[][] paramsPos = new int[topOp.strings.length][cmd.length()];
    		int[][] endsPos = new int[topOp.strings.length][cmd.length()];
    		
    		Vector<Association> opvec = opMap.get(topOp);
    		for (int i = 0; i<opvec.size(); i++){
    			Association a = opvec.get(i);
				for (int k = 0; k<a.inputs.length; k++){
					for (int j = 0; j<a.inputs[k].startPos.size() && j<a.inputs[k].endPos.size();j++){
						int n = 0;
						while(cmd.indexOf(a.string, n) != -1){
							n = cmd.indexOf(a.string, n);
							Log.d("n", ""+n);
							if (a.inputs[k].startPos.get(j) == Association.AT_BEGINNING_OF_STRING){
	    						paramsPos[k][0]+=a.inputs[k].startGroove.get(j);
	    					}else if (n+a.inputs[k].startPos.get(j)<cmd.length() 
	        							&& n+a.inputs[k].startPos.get(j)>-1 
	        							&& a.inputs[k].startPos.get(j)>=0){
	        						paramsPos[k][n+a.inputs[k].startPos.get(j)]+=a.inputs[k].startGroove.get(j);
	    					}
	    					if (a.inputs[k].endPos.get(j) == Association.AT_END_OF_STRING){
	    						endsPos[k][cmd.length()-1]+=a.inputs[k].endGroove.get(j);
	    					}else if (n+a.inputs[k].endPos.get(j)<cmd.length() 
	    							&& n+a.inputs[k].endPos.get(j)>-1 
	    							&& a.inputs[k].endPos.get(j)<=0 ){
	    						endsPos[k][n+a.inputs[k].endPos.get(j)]+=a.inputs[k].endGroove.get(j);
	    					}
	    					n++;
						}
					}
				}
    		}
    		
    		for (int n = 0; n<topOp.strings.length; n++){
	    		int start = 0;
	    		int end = 0;
	    		int mostStart = 0;
	    		int mostEnd = 0;
	    		for (int i = 0; i<cmd.length(); i++){
	    			if (mostStart <= paramsPos[n][i]){
	    				mostStart = paramsPos[n][i];
	    				start = i;
	    			}
	    			if (mostEnd <= endsPos[n][i]){
	    				mostEnd = endsPos[n][i];
	    				end = i;
	    			}
	    		}
	    		
	    		if (start<end){
	    			Log.d("Start/End", "Succeeded: " + cmd.substring(start, end));
	    			topOp.strings[n] = cmd.substring(start, end);
	    		}else{
	    			//TODO: error! should say something.
	    			Log.d("Start/End", "Failed: "+start+", "+end);
	    			return;
	    		}
    		}
    		
    		topOp.context = getActivity();
			topOp.run();
    	}else if (topOp != null){
    		topOp.context = getActivity();
    		topOp.run();
    	}
    }
    
    
    private void seedAssoc(){
        String cmd = "open the app called Pandora";
        String[] inputs = {"Pandora"};
        OpenApp op = new OpenApp();
        associate(cmd, inputs, op);
        
        cmd = "open the app Pandora";
        associate(cmd, inputs, op);
        cmd = "open the Pandora app";
        associate(cmd, inputs, op);
        cmd = "start Pandora";
        associate(cmd, inputs, op);
        cmd = "put on Pandora for me";
        associate(cmd, inputs, op);
        cmd = "put on some Pandora for me";
        associate(cmd, inputs, op);
        cmd = "put Pandora on";
        associate(cmd, inputs, op);
        cmd = "put some Pandora on";
        associate(cmd, inputs, op);
        cmd = "start the app called Pandora";
        associate(cmd, inputs, op);
        cmd = "go ahead and open Pandora";
        associate(cmd, inputs, op);
        cmd = "go ahead and start the Pandora app";
        associate(cmd, inputs, op);
        cmd = "open up Pandora, please";
        associate(cmd, inputs, op);
        cmd = "open Pandora up for me";
        associate(cmd, inputs, op);
        cmd = "start up Pandora";
        associate(cmd, inputs, op);
        cmd = "start Pandora up";
        associate(cmd, inputs, op);

        String[] sendputs = {"me","it works"};
        SendText st = new SendText();
        cmd = "send a text to me that says it works";
        associate(cmd, sendputs, st);
        cmd = "send me a text that says it works";
        associate(cmd, sendputs, st);
        cmd = "send a text to me which says it works";
        associate(cmd, sendputs, st);
        cmd = "send me a text which says it works";
        associate(cmd, sendputs, st);
        cmd = "send me a text saying it works";
        associate(cmd, sendputs, st);

        sendputs[1] = "whoohoo";
        cmd = "send a text to Quinn that says whoohoo";
        associate(cmd, sendputs, st);
        cmd = "send Quinn a text that says whoohoo";
        associate(cmd, sendputs, st);
        cmd = "send a text to Quinn which says whoohoo";
        associate(cmd, sendputs, st);
        cmd = "send Quinn a text which says whoohoo";
        associate(cmd, sendputs, st);
        cmd = "send Quinn a text saying whoohoo";
        associate(cmd, sendputs, st);
    }
}
