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

import java.util.Arrays;
import java.util.List;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.PhoneLookup;

/// SMS Parser service
/**This class parses incoming text messages to find if they pass
 * the contacts filter*/
public class SMSParserService extends Service 
{
    private static final String DEFAULT_SEPARATOR = "\u0001\u0007\u001D\u0007\u0001";
     
    public int onStartCommand(Intent intent, int flags, int startId)
    {
    	// grab the number of the incoming text message
    	String phone_number=intent.getStringExtra("msg_number");
    	SharedPreferences shared_pref = PreferenceManager.getDefaultSharedPreferences(this);
        ContentResolver cr = getContentResolver();
        
    	String name = null;
    	
    	// check to see if the incoming message needs a location
    	String contact_filter = shared_pref.getString("pref_contact_filter", "0");
    	String contact_selection =  shared_pref.getString("contact_selection", "");
    	List<String> contact_list = Arrays.asList(contact_selection.split(DEFAULT_SEPARATOR));
    	
        Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone_number));
        Cursor phones_cursor = cr.query(uri, new String[]{PhoneLookup._ID,PhoneLookup.DISPLAY_NAME}, null, null, null);
        name = phone_number;
        
        if(phones_cursor != null && phones_cursor.moveToFirst())
        {
        	String id = phones_cursor.getString(0); // this is the contact name
            name = phones_cursor.getString(1);
            if((contact_filter.contains("1") && !contact_list.contains(id)) ||
            		(contact_filter.contains("2") && contact_list.contains(id)))
            {
            	// contact is not in the list or the contact is excluded
            	phones_cursor.close(); 
            	return START_NOT_STICKY;	                	
            }
        } 
        else if(contact_filter.contains("1"))
        {
        	// in this case we could not find the contact 
        	phones_cursor.close();     	
        	return START_NOT_STICKY;
        }
        
        // make sure the phone cursor is closed to avoid a warning
        if(phones_cursor != null)
        	phones_cursor.close(); 	
	    
	    // if we get here a location request has been made so start the gps service
	    Intent service_intent = new Intent(this,ReceiverService.class);
	    service_intent.putExtra("msg_number", phone_number);
	    service_intent.putExtra("name",name);
	    startService(service_intent);
    	
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() 
    {
        super.onDestroy();
    }
    
	@Override
	public IBinder onBind(Intent intent) 
	{
		return null;
	}
}