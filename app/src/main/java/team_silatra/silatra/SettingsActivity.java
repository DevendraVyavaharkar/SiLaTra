package team_silatra.silatra;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by ABC on 06-Feb-18.
 */

public class SettingsActivity extends AppCompatActivity{

    Button btn;
    EditText edt;

    @Override
    protected void onCreate(Bundle savedInstateState)
    {
        super.onCreate(savedInstateState);
        setContentView(R.layout.settings_activity);

        edt=(EditText)findViewById(R.id.usernameEditText);
        edt.setText(SilatraDetails.username);

        edt=(EditText)findViewById(R.id.serverEditText1);
        edt.setText(SilatraDetails.serverIP.substring(0,3));
        edt=(EditText)findViewById(R.id.serverEditText2);
        edt.setText(SilatraDetails.serverIP.substring(4,7));
        edt=(EditText)findViewById(R.id.serverEditText3);
        edt.setText(SilatraDetails.serverIP.substring(8,11));
        edt=(EditText)findViewById(R.id.serverEditText4);
        edt.setText(SilatraDetails.serverIP.substring(12,15));

        edt=(EditText)findViewById(R.id.frameRateEditText);
        edt.setText(SilatraDetails.fps+"");

        btn=(Button)findViewById(R.id.applyChangesBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SilatraDetails.username =((EditText) findViewById(R.id.usernameEditText)).getText().toString();

                SilatraDetails.serverIP =appendZero(((EditText) findViewById(R.id.serverEditText1)).getText().toString()) + "." +
                        appendZero(((EditText) findViewById(R.id.serverEditText2)).getText().toString()) + "." +
                        appendZero(((EditText) findViewById(R.id.serverEditText3)).getText().toString()) + "." +
                        appendZero(((EditText) findViewById(R.id.serverEditText4)).getText().toString());

                SilatraDetails.fps =Integer.parseInt(((EditText) findViewById(R.id.frameRateEditText)).getText().toString());

                Intent i=new Intent (view.getContext(),MainActivity.class);
                startActivity(i);
            }
        });


    }

    public String appendZero(String s)
    {
        if(s.length()==0)
            s="000";
        else if(s.length()==1)
            s="00"+s;
        else if(s.length()==2)
            s="0"+s;
        return s;
    }
}
