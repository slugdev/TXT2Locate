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

import java.util.ArrayList;

import slugsoftware.utilities.txt2location.R;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;

/// This service will capture the current location and send it as a txt message
public class ReceiverService extends Service 
{
	LocationManager locationManager = null;
    Location currentLocation = null;
    public ArrayList<TextLocationRequest> textLocationArray = new ArrayList<TextLocationRequest>();
	private Handler handler = new Handler();
	private int locationCnt = 0;
	String locationString = null;
	boolean sendBearing = false;
	boolean sendAltitude = false;
	boolean sendVelocity = false;
	boolean sentTextMessage = false;
	boolean logTextMessage = false;
	String altitudeUnit = "0";
	String velocityUnit = "0";
	
	private Runnable runnable = new Runnable()
	{
	   @Override
	   public void run() 
	   {
	       // kill the service after a few minutes
		   // if we did not get a location then use the cell towers		   
		   stopSelf();
	   }
	};

	protected class TextLocationRequest  
	{
		public String name;
		public String targetMsg;
		public long startTime = System.currentTimeMillis();
	}
	
       
    public int onStartCommand(Intent intent, int flags, int startId)
    {
    	String phone_number=intent.getStringExtra("msg_number");
    	String name = intent.getStringExtra("name");
    	SharedPreferences shared_pref = PreferenceManager.getDefaultSharedPreferences(this);

    	TextLocationRequest txt_loc = new TextLocationRequest();
    	txt_loc.targetMsg = phone_number;
    	txt_loc.name = name;
    	textLocationArray.add(txt_loc);
    	
    	// if this is the first request grab all of the settings
    	if(textLocationArray.size() == 1)
    	{
	    	locationString = shared_pref.getString("pref_return_string", "@string/return_string");
	    	sendBearing = shared_pref.getBoolean("pref_send_bearing", false);
	    	sendAltitude = shared_pref.getBoolean("pref_send_altitude", false);
	    	sendVelocity = shared_pref.getBoolean("pref_send_velocity", false);
	    	altitudeUnit = shared_pref.getString("pref_length_unit","0");
	    	velocityUnit = shared_pref.getString("pref_velocity_unit","0");
	    	logTextMessage = shared_pref.getBoolean("pref_log_text_message", false);
    	}
    	
    	set_location_provider();
    	
    	send_notification(name,getString(R.string.requested_your_location),"");
    	
	   if(locationManager == null || 
				 (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)))
       {
		   send_text_messages();
	       return START_STICKY; 
       }
	   
	    // start the process as sticky to make sure it sticks around
        return START_STICKY;
    }

    @Override
    public void onDestroy() 
    {
    	send_text_messages();
    	
        remove_location_provider();
        super.onDestroy();
    }
    
	// Define a listener that responds to location updates
	LocationListener locationListener = new LocationListener()	
	{
	    public void onLocationChanged(Location location) 
	    {    
    		locationCnt++;

	      // Called when a new location is found by the network location provider.

	    	if(currentLocation == null)
	    	{
	    		currentLocation = new Location(location);
	    	}
	    	else if(location.getAccuracy() < currentLocation.getAccuracy())
	    	{
		    	currentLocation = new Location(location);
	    	}
    	
	    	if(currentLocation != null && locationCnt > 10)
	    	{
	    		send_text_messages();
	    	}
	    }
	    
	    public void onStatusChanged(String provider, int status, Bundle extras) 
	    {
	    	
	    }

	    public void onProviderEnabled(String provider) 
	    {
	    	
	    }

	    public void onProviderDisabled(String provider)
	    {
	    	
	    }
	};
    
	/// Get the N,E,S,W char based on the bearing information
    protected String get_direction_string(float bearing)
    {
	    if((bearing >= 337.5) && (bearing < 22.5))
	    {
	    return "N";    
	    }
	    else if((bearing >= 22.5) && (bearing < 67.5))
	    {
	    	return "NE";
	    }
	    else if((bearing >= 67.5) && (bearing < 112.5))
	    {
	    	return "E";
	    }
	    else if((bearing >= 112.5) && (bearing < 157.5))
	    {
	    	return "SE";
	    }
	    else if((bearing >= 157.5) && (bearing < 202.5))
	    {
	    	return "S";
	    }
	    else if((bearing >= 202.5) && (bearing < 247.5))
	    {
	    	return "SW";
	    }
	    else if((bearing >= 247.5) && (bearing < 292.5))
	    {
	    	return "W";
	    }
	    else if((bearing >= 292.5) && (bearing < 337.5))
	    {
	    	return "NW";
	    }
	    return "N";  
    }
	
    protected void send_text_messages()
    {
    	synchronized(this)
		{
    		if(sentTextMessage)
    			return;    			

    		boolean network_location =false;
    		sentTextMessage = true;
	        if(currentLocation == null)
			{
	         	currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	         	network_location = true;
			}
			while(textLocationArray.size() > 0)
			{                  
				TextLocationRequest tr = textLocationArray.remove(0);
				if(currentLocation != null)
				{
			       String message = locationString.
			    		   replace("_LAT_", String.valueOf(currentLocation.getLatitude())).
			    		   replace("_LONG_", String.valueOf(currentLocation.getLongitude()));
			       if(!network_location && sendBearing )
			       {
			    	   float bearing = currentLocation.getBearing();
			    	   String dr = get_direction_string(bearing);
			    	   message = message + "\n"+ getString(R.string.bearing_str) + ": " + 
			               ((int)bearing) + " " + getString(R.string.degrees_str) + " (" + dr + ")";  			  
			       }
			       if(!network_location && sendAltitude )
			       {
			    	   if(altitudeUnit.contains("0"))
			    	   {
				    	   message = message + "\n" + getString(R.string.altitude_str) + ": " + 
				    			   ((int)currentLocation.getAltitude()) + " " + getString(R.string.meters_str);	
			    	   }
			    	   else
			    	   {
				    	   message = message + "\n" + getString(R.string.altitude_str) + ": " + 
				    			   ((int)(currentLocation.getAltitude()*3.28084)) + " " + getString(R.string.feet_str);	
			    	   }
			       }
			       if(!network_location && sendVelocity)
			       {

			    	   if(velocityUnit.contains("0"))
			    	   {
				    	   message = message + "\n" + getString(R.string.velocity_str) + ": " + 
				    			   ((int)currentLocation.getSpeed()*3.6) + " " + getString(R.string.kmhr_str);	
			    	   }
			    	   else if(velocityUnit.contains("1"))
			    	   {
				    	   message = message + "\n" + getString(R.string.velocity_str) + ": " + 
				    			   ((int)(currentLocation.getSpeed()*2.23694)) + " " + getString(R.string.mph_str);	
			    	   }
			    	   else if(velocityUnit.contains("2"))
			    	   {
				    	   message = message + "\n" + getString(R.string.velocity_str) + ": " + 
				    			   (currentLocation.getSpeed()) + " " + getString(R.string.m_per_s_str);	
			    	   }
			    	   else if(velocityUnit.contains("3"))
			    	   {
				    	   message = message + "\n" + getString(R.string.velocity_str) + ": " + 
				    			   (currentLocation.getSpeed()*3.28084) + " " + getString(R.string.ft_per_s_str);	
			    	   }
			       }
			       
			       if(network_location)
			       {
			    	   message = message + "\n" + getString(R.string.using_network_provider);	
			       }
			       send_txt_message(tr.targetMsg,message); 
			       send_notification(tr.name,getString(R.string.sent_location_using_gps),message);
				}
				else
				{
 			       String message = getString(R.string.unable_to_get_location);
 			       
 			       if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
 			    		   !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
 			       {
 			    	  message = getString(R.string.gps_network_not_available);
 			    	 send_txt_message(tr.targetMsg,message);
 			    	  send_notification(tr.name,message,null); 
 			       }
 			       else
 			       {
 			    	  send_txt_message(tr.targetMsg,message);
 			    	  send_notification(tr.name,message,null); 
 			       }
				}		
			}
			// kill the service
			stopSelf();
		}
    }
	
	private void set_location_provider() 
	{
		if(locationManager == null)
		{
			locationCnt = 0;
			locationManager = 
					(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
			  
			// Register the listener with the Location Manager to receive location updates
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER,
					0,
					0,
					locationListener);
			
			handler.postDelayed(runnable, 180000); //kill this service after 3 minutes
		}
	}
	
	private void remove_location_provider()
	{
		if(locationManager != null &&
				locationListener != null)
        {
        	locationManager.removeUpdates(locationListener);
        	locationManager = null;
        	
        	handler.removeCallbacks(runnable);
        }
	}
	
	private void send_txt_message(String target, String message)
	{
	     SmsManager sms = SmsManager.getDefault();
	       	  sms.sendTextMessage(target, null, message, null, null); 
	    	  
	   	  if(logTextMessage)
	   	  {
			 ContentValues values = new ContentValues(); 
			 values.put("address", target); 
			 values.put("body", message); 
			 getContentResolver().insert(Uri.parse("content://sms/sent"), values);	
	   	  }
	}
	
	private void send_notification(String name,String message,String full_msg)
	{
		Intent intent1 = new Intent (this, NotifyActivity.class);
		intent1.putExtra("title",name);
		intent1.putExtra("msg",message);
		intent1.putExtra("full_msg",full_msg);
		intent1.addFlags (Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
		intent1.addFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent pend = PendingIntent.getActivity(this, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
		
		NotificationCompat.Builder builder =
			    new NotificationCompat.Builder(this)
			    .setSmallIcon(R.drawable.notify)
			    .setContentTitle(name)
			    .setAutoCancel(true)
			    .setContentIntent(pend)
			    .setContentText(message);
		
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(136625, builder.build());
	}

	@Override
	public IBinder onBind(Intent intent) 
	{
		return null;
	}
}