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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List; 
import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.preference.ListPreference;
import android.provider.ContactsContract;
import android.util.AttributeSet;
 
public class MultiselectContactPreference extends ListPreference
{
    private String separator;
    private static final String DEFAULT_SEPARATOR = "\u0001\u0007\u001D\u0007\u0001";
    private boolean[] entryChecked;
 
    @Override
    protected Object onGetDefaultValue(TypedArray typedArray, int index) 
    {
        return typedArray.getTextArray(index);
    }
 
    @Override
    protected void onSetInitialValue(boolean restoreValue,Object rawDefaultValue) 
    {
        String value = null;
        CharSequence[] defaultValue;
        if (rawDefaultValue == null)
        {
            defaultValue = new CharSequence[0];
        } 
        else 
        {
            defaultValue = (CharSequence[]) rawDefaultValue;
        }
        List<CharSequence> joined = Arrays.asList(defaultValue);
        String joinedDefaultValue = join(joined, separator);
        if (restoreValue) 
        {
            value = getPersistedString(joinedDefaultValue);
        } else {
            value = joinedDefaultValue;
        }
 
        setSummary(prepareSummary(Arrays.asList(unpack(value))));
        setValueAndEvent(value);
    }
    
    @Override
    protected void onDialogClosed(boolean positiveResult) 
    {
        List<CharSequence> values = new ArrayList<CharSequence>();
 
        CharSequence[] entryValues = getEntryValues();
        if (positiveResult && entryValues != null) 
        {
            for (int i = 0; i < entryValues.length; i++) 
            {
                if (entryChecked[i] == true) 
                {
                    String val = (String) entryValues[i];
                    values.add(val);
                }
            }
 
            String value = join(values, separator);
            setSummary(prepareSummary(values));
            setValueAndEvent(value);
        }
    }
    
    @Override
    protected void onPrepareDialogBuilder(Builder builder) 
    {
        CharSequence[] entries = getEntries();
        CharSequence[] entryValues = getEntryValues();
        
        if (entries == null || entryValues == null
                || entries.length != entryValues.length)
        {
            throw new IllegalStateException(
                    "Entries and entryValues arrays must be the same length");
        }
 
        load_checked_entities();
        OnMultiChoiceClickListener listener = new DialogInterface.OnMultiChoiceClickListener()
        {
            public void onClick(DialogInterface dialog, int which, boolean val) 
            {
                entryChecked[which] = val;
            }
        };
        builder.setMultiChoiceItems(entries, entryChecked, listener);
    }
    
    public MultiselectContactPreference(Context context, AttributeSet attributeSet) 
    {
    	//this.setEntries(entries)
        super(context, attributeSet);
        SetContactEntries();
        entryChecked = new boolean[getEntries().length];
        separator = DEFAULT_SEPARATOR;
    }
 
    public MultiselectContactPreference(Context context) 
    {
        this(context, null);
    }
 
    public void SetContactEntries() 
    {
	    ContentResolver cr = this.getContext().getContentResolver();
	    
	    Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, "UPPER(display_name) asc");
	    ArrayList<String> entries_list = new ArrayList<String>();
	    ArrayList<String> entries_values_list = new ArrayList<String>();

	    while (cur.moveToNext()) 
	    {
	         String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
             String display_name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
             int has_phone = cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
             if(has_phone == 1)
             {
	             entries_values_list.add(id);
	             entries_list.add(display_name);
             }
	    }
	    cur.close();
	    
	    CharSequence[] entries = entries_list.toArray(new CharSequence[entries_list.size()]);
	    CharSequence[] entry_values = entries_values_list.toArray(new CharSequence[entries_values_list.size()]);
	    this.setEntries(entries);
	    this.setEntryValues(entry_values);
     }
 
    private CharSequence[] unpack(CharSequence val)
    {
        if (val == null || "".equals(val)) 
        {
            return new CharSequence[0];
        } else {
            return ((String) val).split(separator);
        }
    }
    
    public CharSequence[] getCheckedValues()
    {
        return unpack(getValue());
    }
 
    private void load_checked_entities() 
    {
        CharSequence[] entryValues = getEntryValues();
        CharSequence[] vals = unpack(getValue());
 
        if (vals != null)
        {
            List<CharSequence> valuesList = Arrays.asList(vals);
            for (int i = 0; i < entryValues.length; i++) 
            {
                CharSequence entry = entryValues[i];
                entryChecked[i] = valuesList.contains(entry);
            }
        }
    }
 
    private void setValueAndEvent(String value)
    {
        if (callChangeListener(unpack(value)))
        {
            setValue(value);
        }
    }
 
    private CharSequence prepareSummary(List<CharSequence> joined)
    {
        List<String> titles = new ArrayList<String>();
        CharSequence[] entryTitle = getEntries();
        CharSequence[] entryValues = getEntryValues();
        int ix = 0;
        for (CharSequence value : entryValues)
        {
            if (joined.contains(value)) 
            {
                titles.add((String) entryTitle[ix]);
            }
            ix += 1;
        }
        return join(titles, ", ");
    }
 
    // string join
    protected static String join(Iterable<?> iterable, String separator)
    {
        Iterator<?> iter;
        if (iterable == null || (!(iter = iterable.iterator()).hasNext()))
            return "";
        StringBuilder oBuilder = new StringBuilder(String.valueOf(iter.next()));
        while (iter.hasNext())
            oBuilder.append(separator).append(iter.next());
        return oBuilder.toString();
    }
 
}
