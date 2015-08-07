package com.jfboily.gtd;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CreateProfileActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	
	    setContentView(R.layout.profile);

        Button next = (Button) findViewById(R.id.btnGo);
        next.setOnClickListener(new View.OnClickListener() 
        {
        	//@Override
            public void onClick(View view) 
            {
            	String nom;
            	
            	TextView tv = (TextView)findViewById(R.id.txtNom);
            	
            	nom = tv.getText().toString();
                Intent myIntent = new Intent(view.getContext(), GTDGame.class);
                startActivityForResult(myIntent, 0);
            }
        });
	}

}
