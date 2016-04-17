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

import java.util.List;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;
import slugsoftware.utilities.txt2location.R;

public class MainActivity extends PreferenceActivity 
{
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        this.addPreferencesFromResource(R.xml.preferences);
        
		SharedPreferences shared_pref = PreferenceManager.getDefaultSharedPreferences(this);
    	boolean offensive = shared_pref.getBoolean("pref_check_offending_apps", true);
    	if(offensive)
    	{
	        // check to see if resources 
	        Intent intent = new Intent("android.provider.Telephony.SMS_RECEIVED");
	        List<ResolveInfo> infos = getPackageManager().queryBroadcastReceivers(intent, 0);
	        boolean found_sms_pro = false;
			for (ResolveInfo info : infos) 
			{
				if ((info.activityInfo.name.contains("com.jb.gosms.smspopup.SmsReceiver") || 
						info.activityInfo.name.contains("com.jb.gosms.transaction.PrivilegedSmsReceiver"))
						&& info.priority >= 500) 
				{
					found_sms_pro = true;
				}
			}
	        
	        if(found_sms_pro)
	        {
	        	  final TextView message = new TextView(this);
	        	  // i.e.: R.string.dialog_message =>
	        	            // "Test this dialog following the link to dtmilano.blogspot.com"
	        	  final SpannableString s = new SpannableString(getText(R.string.sms_pro_message));
	        	  Linkify.addLinks(s, Linkify.WEB_URLS);
	        	  message.setText(s);
	        	  message.setMovementMethod(LinkMovementMethod.getInstance());

	        	
	            AlertDialog.Builder builder = new AlertDialog.Builder(this);
	            builder.setTitle(getString(R.string.sms_pro_message_title));
	            builder.setView(message);
	            builder.setPositiveButton(getString(R.string.close_button),null);

	            builder.create().show();
	        }
    	}
        
        ListPreference pref_contact_filter = (ListPreference) findPreference("pref_contact_filter");
        pref_contact_filter.setSummary(pref_contact_filter.getEntry());

        pref_contact_filter.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
        {
          public boolean onPreferenceChange(Preference preference, Object newValue)
          {
            String nv = (String) newValue;

            if (preference.getKey().equals("pref_contact_filter"))
            {
              ListPreference pref_contact_filter = (ListPreference) preference;
              pref_contact_filter.setSummary(pref_contact_filter.getEntries()[pref_contact_filter.findIndexOfValue(nv)]);
            }
            return true;
          }

        });
        
        ListPreference pref_length_unit = (ListPreference) findPreference("pref_length_unit");
        pref_length_unit.setSummary(pref_length_unit.getEntry());
        
        pref_length_unit.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
        {
          public boolean onPreferenceChange(Preference preference, Object newValue)
          {
            String nv = (String) newValue;
            if (preference.getKey().equals("pref_length_unit"))
            {
              ListPreference pref_length_unit = (ListPreference) preference;
              pref_length_unit.setSummary(pref_length_unit.getEntries()[pref_length_unit.findIndexOfValue(nv)]);
            }
            return true;
          }

        });
       
        ListPreference pref_velocity_unit = (ListPreference) findPreference("pref_velocity_unit");
        pref_velocity_unit.setSummary(pref_velocity_unit.getEntry());
        
        pref_velocity_unit.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
        {
          public boolean onPreferenceChange(Preference preference, Object newValue)
          {
            String nv = (String) newValue;
            if (preference.getKey().equals("pref_velocity_unit"))
            {
              ListPreference pref_velocity_unit = (ListPreference) preference;
              pref_velocity_unit.setSummary(pref_velocity_unit.getEntries()[pref_velocity_unit.findIndexOfValue(nv)]);
            }
            return true;
          }

        });
    }
	
    @Override
    protected void onStart()
    {
        super.onStart();
        // The activity is about to become visible.
    }
    
    @Override
    protected void onResume() 
    {
        super.onResume();
        // The activity has become visible (it is now "resumed").
    }
    
    @Override
    protected void onPause() 
    {
        super.onPause();
        // Another activity is taking focus (this activity is about to be "paused").
    }
    
    @Override
    protected void onStop()
    {
        super.onStop();
        // The activity is no longer visible (it is now "stopped")
    }
    
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        // The activity is about to be destroyed.
    }
	
}