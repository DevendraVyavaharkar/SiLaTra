package team_silatra.silatra;

import android.support.v7.app.AppCompatActivity;

/**
 * Created by ABC on 06-Feb-18.
 */

public class SilatraDetails extends AppCompatActivity{

    public static String username = "";
    public static String serverIP = "192.168.001.033";
    public static int fps=24;
    public static int imageFileNum=0;
    public static boolean isFirstTime=true;

    public static int nextImageFileNumber()
    {
        imageFileNum=1 + (imageFileNum+1)%fps;
        return imageFileNum;
    }

    public static void openedFirstTime(){
        isFirstTime=false;
    }

}
