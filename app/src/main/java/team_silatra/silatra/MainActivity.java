package team_silatra.silatra;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity{

    Button btn;
    TextView txt;

    MainActivity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt=findViewById(R.id.mainUsername);
        txt.setText(SilatraDetails.username);

        btn=findViewById(R.id.cameraBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i=new Intent (v.getContext(),CameraActivity.class);
                startActivity(i);
            }
        });

        //Opening the settings to set the Server IP when App is opened for the first time
        if(SilatraDetails.isFirstTime)
        {
            final Dialog dialog=null;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setTitle("Welcome to SiLaTra");
            builder.setMessage("Please enter the Server IP Address");
            builder.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    SilatraDetails.openedFirstTime();
                    startActivity(new Intent(getBaseContext(),SettingsActivity.class));
                    finish();
                }
            })
                .setNegativeButton("Not now", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SilatraDetails.openedFirstTime();
                        startActivity(new Intent(getBaseContext(),MainActivity.class));
                        finish();
                    }
                });

            //Create a AlertDialog object and return it
            builder.create().show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        Intent i;
        switch(item.getItemId()){
            case R.id.item1:
                i = new Intent(this,SettingsActivity.class);
                startActivity(i);
                return true;

            case R.id.item2:
                i = new Intent(this,CreditsActivity.class);
                startActivity(i);
                return true;

            case R.id.item3:
                i = new Intent(this,AboutActivity.class);
                startActivity(i);
                return true;
        }
        return true;
    }

}


