package com.rndapp.andy.operations;

import java.util.ArrayList;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class SendText extends Operation {
	public static final String SMS_ADDRESS_PARAM="SMS_ADDRESS_PARAM";
	public static final String SMS_SENT_ACTION="com.tilab.msn.SMS_SENT";
	public static final String SMS_DELIVERY_MSG_PARAM="SMS_DELIVERY_MSG_PARAM";
	
	public SendText(){
		super();
		String[] strs = {"Person","Text"};
		this.strings = strs;
	}
	
	public SendText(Context ctxt){
		super();
		context = ctxt;
		String[] strs = {"Person","Text"};
		this.strings = strs;
	}
	
	public SendText(Operation next, Context ctxt, String[] strs){
		super();
		
		this.next = next;
		context = ctxt;
		this.strings = strs;
	}
	
	@Override
	public void run(){
		String name = "Me";//strings[0];
		String smsText = strings[1];

		String number = "";
		String id = "";
    	
    	ContentResolver contentResolver = context.getContentResolver();
        Uri uri = Data.CONTENT_URI;
        String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.CONTACT_ID };
        String selection = StructuredName.DISPLAY_NAME + " = ?";
        String[] selectionArguments = { name };
        Cursor cursor = contentResolver.query(uri, projection, selection, selectionArguments, null);

        if (cursor != null) {
        	//Log.d("Cursor1", "not null");
            while (cursor.moveToNext()) {
                id = cursor.getString(0);
            	//Log.d("First id", id);
            }
        }
        cursor.close();
        
        Log.d("id",id);
        ContentResolver cr = context.getContentResolver();
        Cursor cur = cr.query(
        		ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
                null, 
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?", 
                new String[]{id}, null);

        while (cur.moveToNext()) {
            number = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        }
        cur.close();
        
    	Log.d("Number",": "+number);
		
		String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";
		
		SmsManager smsMgr = SmsManager.getDefault();
		
		ArrayList<String> messages = smsMgr.divideMessage(smsText);
		Intent sentIntent = new Intent(SMS_SENT_ACTION);
        sentIntent.putExtra(SMS_ADDRESS_PARAM, number);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, sentIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
            new Intent(SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0,
            new Intent(DELIVERED), 0);
        //---when the SMS has been sent---
        context.registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(context, "SMS sent", 
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(context, "Generic failure", 
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(context, "No service", 
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(context, "Null PDU", 
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(context, "Radio off", 
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));
        //---when the SMS has been delivered---
        context.registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(context, "SMS delivered", 
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(context, "SMS not delivered", 
                                Toast.LENGTH_SHORT).show();
                        break;                        
                }
            }
        }, new IntentFilter(DELIVERED));  
        ArrayList<PendingIntent> listOfIntents = new ArrayList<PendingIntent>();
        listOfIntents.add(pi);
        listOfIntents.add(sentPI);
        listOfIntents.add(deliveredPI);
		smsMgr.sendMultipartTextMessage(number, null, messages, listOfIntents, null);
	}
	
	

}
