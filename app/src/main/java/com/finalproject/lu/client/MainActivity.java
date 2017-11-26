package com.finalproject.lu.client;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

import POJO.Message;
import POJO.Nodification;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

    TextView textResponse;
    EditText editTextAddress, editTextPort;
    Button buttonConnect, buttonClear, buttonSend;
    private Socket socket;
    String dstAddress;
    int dstPort;
    String response = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextAddress = (EditText)findViewById(R.id.address);
        editTextPort = (EditText)findViewById(R.id.port);
        buttonConnect = (Button)findViewById(R.id.connect);
        buttonClear = (Button)findViewById(R.id.clear);
        buttonSend = (Button)findViewById(R.id.send);
        textResponse = (TextView)findViewById(R.id.response);

        buttonConnect.setOnClickListener(buttonConnectOnClickListener);
        buttonSend.setOnClickListener(buttonSendOnClickListener);
        buttonClear.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                textResponse.setText("");
            }});
    }

    OnClickListener buttonConnectOnClickListener =
            new OnClickListener(){

                @Override
                public void onClick(View arg0) {
                    MyClientTask myClientTask = new MyClientTask(
                            "192.168.200.2",
                            8080);
                    myClientTask.start();
                }};

    OnClickListener buttonSendOnClickListener =
            new OnClickListener(){

                @Override
                public void onClick(View arg0) {
                    SocketServerReplyThread myClientTask = new SocketServerReplyThread(editTextAddress.getText().toString());
                    myClientTask.start();
                }};

    private class SocketServerReplyThread extends Thread {

        private String test;

        SocketServerReplyThread(String test) {
            this.test = test;
        }

        @Override
        public void run() {
            ObjectOutputStream oos;

            try {
                oos = new ObjectOutputStream(socket.getOutputStream());
                //TODO hardcode sample code
                Message message = new Message(new HashMap<String, Integer>(),new Nodification("1231"), false);
                oos.writeObject(message);
                oos.flush();

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                test += "Something wrong! " + e.toString() + "\n";
            }

            MainActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    textResponse.setText(test);
                }
            });
        }

    }

    public class MyClientTask extends Thread {

        MyClientTask(String addr, int port){
            dstAddress = addr;
            dstPort = port;
        }

        @Override
        public void run() {

            try {
                socket = new Socket(dstAddress, dstPort);

                ByteArrayOutputStream byteArrayOutputStream =
                        new ByteArrayOutputStream(1024);
                byte[] buffer = new byte[1024];

                int bytesRead;
                InputStream inputStream = socket.getInputStream();

				/*
				 * notice:
				 * inputStream.read() will block if no data return
				 */
                while ((bytesRead = inputStream.read(buffer)) != -1){
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                    response += byteArrayOutputStream.toString("UTF-8");
                }

            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response = "UnknownHostException: " + e.toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response = "IOException: " + e.toString();
            }
//			finally{
//				if(socket != null){
//					try {
//						socket.close();
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//			}
        }



    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
