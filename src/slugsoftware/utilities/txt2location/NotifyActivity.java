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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


public class NotifyActivity extends Activity 
{			
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notify);

        Intent intent = this.getIntent();
        
    	String title=intent.getStringExtra("title");
    	String msg=intent.getStringExtra("msg");
    	String full_msg=intent.getStringExtra("full_msg");
        
    	if(title == null)
    		title = new String("");
    	
    	if(full_msg == null)
    		full_msg = new String("");
    	
    	if(msg == null)
    		msg = new String("");
    	
		TextView text = (TextView) findViewById(R.id.text);
		String complete_msg = msg + "\n" + full_msg;
	  	  final SpannableString s = new SpannableString(complete_msg);
	  	  Linkify.addLinks(s, Linkify.WEB_URLS);
  	  
	  	text.setMovementMethod(LinkMovementMethod.getInstance());
		text.setText(s);
		ImageView image = (ImageView) findViewById(R.id.image);
		image.setImageResource(R.drawable.ic_launcher);

		Button dialogButton = (Button) findViewById(R.id.doneButton);
		dialogButton.setOnClickListener(myhandler);
    	
        this.setTitle(title);
     }
	
	  View.OnClickListener myhandler = new View.OnClickListener()
	  {
		    public void onClick(View v)
		    {
		      finish();
		    } 
	  };


}