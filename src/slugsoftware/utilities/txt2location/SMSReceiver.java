/*
     TXT2Locate Copyright (C) 2011-2016 Sam Showman

     This program is free software; you can redistribute it and/or
     modify it under the terms of the GNU General Public License as
     published by the Free Software Foundation(version 2);
     
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of 
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
     GNU General Public License for more details.

     You should have received a copy of the GNU General Public License
     along with this program; if not, write to the Free Software
     Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/
package slugsoftware.utilities.txt2location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;

/// Registered class to listen to text messages as they arrive 
/** All text messages that contain the keyword are passed on to a parsing service.*/
public class SMSReceiver extends BroadcastReceiver
{  
	/// Callback for every txt message
    @Override
    public void onReceive(Context context, Intent intent)
    {
    	SharedPreferences shared_pref = PreferenceManager.getDefaultSharedPreferences(context);
    	// get the enabled option and the location keyword
    	boolean pref_enabled = shared_pref.getBoolean("pref_enable", true);
    	
    	if(!pref_enabled)
    		return;
    	
    	String location_string = shared_pref.getString("pref_location_string", "TXT2Location");
        Bundle bundle = intent.getExtras();
        
        Object messages[] = (Object[]) bundle.get("pdus");
        SmsMessage smsMessage[] = new SmsMessage[messages.length];
        for (int n = 0; n < messages.length; n++)
        {
            smsMessage[n] = SmsMessage.createFromPdu((byte[]) messages[n]);
            String msg = smsMessage[n].getMessageBody();
            
    	    if(!msg.contains(location_string))
    		    continue;
    	    
    	    // Start the parser service to check the contact filters
		    Intent service_intent = new Intent(context,SMSParserService.class);
		    service_intent.putExtra("msg_number", smsMessage[n].getOriginatingAddress());
		    service_intent.putExtra("msg",msg);
		    context.startService(service_intent);
        }
    }
}

