package team_silatra.silatra;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Created by ABC on 15-Jan-18.
 */

public class ClientSendAndListen extends AppCompatActivity implements Runnable{

    Boolean flag=true;
    DatagramSocket udpSocket;
    InetAddress serverAddr;

    @Override
    public void run() {
        try {
            udpSocket = new DatagramSocket();
            serverAddr = InetAddress.getByName("192.168.1.33");
        } catch (SocketException e) {
            Log.e("Socket Open:", "Error:", e);
        } catch (UnknownHostException e) {
            Log.e("Wrong IP:", "Error:", e);
        }
    }

    public void startVideo(View view)
    {
        DatagramPacket packet;
        Boolean run=true;
        try {
            byte[] buf = ("files").getBytes();
                packet = new DatagramPacket(buf, buf.length, serverAddr, 9001);
                udpSocket.send(packet);
        } catch (IOException e) {}
        finally {
            //receiveTranslation();
            while (run)
            {
                try
                {
                    byte[] message = new byte[8000];
                    packet = new DatagramPacket(message,message.length);
                    Log.i("UDP client: ", "about to wait to receive");
                    udpSocket.setSoTimeout(10000);
                    udpSocket.receive(packet);

                    String text = new String(message, 0, packet.getLength());
                    Log.d("Received text", text);

                    //TextView tx= (TextView) findViewById(R.id.msgTextView);
                    //tx.setText(text);

                }
                catch (SocketTimeoutException e)
                {
                    Log.e("Timeout Exception","UDP Connection:",e);
                    run = false;
                    udpSocket.close();
                }
                catch (IOException e)
                {
                    Log.e("UDP client - IOExcept.", "error: ", e);
                    run = false;
                    udpSocket.close();
                }
            }
        }
    }

    public void stopVideo(View view)
    {
        flag=false;
        //receiveTranslation();
    }

    /*public void receiveTranslation()
    {
        DatagramPacket packet;
        Boolean run=true;
        while (run)
        {
            try
            {
                byte[] message = new byte[8000];
                packet = new DatagramPacket(message,message.length);
                Log.i("UDP client: ", "about to wait to receive");
                udpSocket.setSoTimeout(10000);
                udpSocket.receive(packet);

                String text = new String(message, 0, packet.getLength());
                Log.d("Received text", text);

                TextView tx= (TextView) findViewById(R.id.msgTextView);
                tx.setText(text);

            }
            catch (SocketTimeoutException e)
            {
                Log.e("Timeout Exception","UDP Connection:",e);
                run = false;
                udpSocket.close();
            }
            catch (IOException e)
            {
                Log.e("UDP client - IOExcept.", "error: ", e);
                run = false;
                udpSocket.close();
            }

        }
    }*/
}
