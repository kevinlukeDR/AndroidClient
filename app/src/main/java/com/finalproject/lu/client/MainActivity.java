package com.finalproject.lu.client;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import POJO.FoodsEnum;
import POJO.Message;
import POJO.Nodification;
import POJO.Order;
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
    private int customerId;
    private String customerName;
    private static List<Message> orderList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        orderList = new ArrayList<>();
        editTextAddress = (EditText)findViewById(R.id.address);
        editTextPort = (EditText)findViewById(R.id.port);
        buttonConnect = (Button)findViewById(R.id.connect);
        buttonClear = (Button)findViewById(R.id.clear);
        buttonSend = (Button)findViewById(R.id.send);
        textResponse = (TextView)findViewById(R.id.response);
        customerName = editTextAddress.getText().toString();
        buttonConnect.setOnClickListener(buttonConnectOnClickListener);
        buttonSend.setOnClickListener(buttonSubmitOnClickListener);
        buttonClear.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                textResponse.setText("");
            }});
    }

    OnClickListener buttonConnectOnClickListener =
            arg0 -> {
                MyClientTask myClientTask = new MyClientTask(
                        "192.168.200.2",
                        8080);
                myClientTask.start();
            };

    OnClickListener buttonSubmitOnClickListener =
            new OnClickListener(){

                @Override
                public void onClick(View arg0) {
                    SubmitThread myClientTask = new SubmitThread(editTextAddress.getText().toString());
                    myClientTask.start();
                }};

    private class SubmitThread extends Thread {

        private String test;

        SubmitThread(String test) {
            this.test = test;
        }

        @Override
        public void run() {
            ObjectOutputStream oos;

            try {
                oos = new ObjectOutputStream(socket.getOutputStream());
                //TODO hardcode sample code
                Map<String, Integer> map = Collections.unmodifiableMap(Stream.of(
                        new AbstractMap.SimpleEntry<>(FoodsEnum.BURGERS.getName(), 2),
                        new AbstractMap.SimpleEntry<>(FoodsEnum.CHICHENS.getName(), 2),
                        new AbstractMap.SimpleEntry<>(FoodsEnum.ONIONRINGS.getName(), 3),
                        new AbstractMap.SimpleEntry<>(FoodsEnum.FRENCHFRIES.getName(), 5)
                ).collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())));
                Order order = new Order(orderList.size(), customerId, customerName, map);
                Message message = new Message(order, new Nodification(""), false, null);
                oos.writeObject(message);
                oos.flush();
                orderList.add(message);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                test += "Something wrong! " + e.toString() + "\n";
            }

            MainActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    textResponse.setText("Submitted");
                }
            });
        }

    }

    public class MyClientTask extends Thread {
        String reply = "";
        MyClientTask(String addr, int port){
            dstAddress = addr;
            dstPort = port;
        }

        @Override
        public void run() {

            try {
                socket = new Socket(dstAddress, dstPort);
                while (true) {
                    InputStream is = socket.getInputStream();
                    ObjectInputStream ois = new ObjectInputStream(is);
                    Object object = ois.readObject();
                    Message message = null;
                    boolean isInteger = false;
                    if (object instanceof String) {
                        reply = (String) object;
                    } else if (object instanceof Message) {
                        message = (Message) object;
                    } else if (object instanceof Integer) {
                        isInteger = true;
                    }
                    if (message != null){
                        int orderId = message.getOrder().getOrderId();
                        orderList.get(orderId).setNodification(new Nodification(Nodification.Status.RECEIVE.getStatus()));
                        MainActivity.this.runOnUiThread(() -> textResponse.setText(response +=
                                ("\n #" + orderId + " order has been received")));
                    }
                    if (isInteger) {
                        customerId = (Integer) object;
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textResponse.setText(response += "\n Welcome!!");
                            }
                        });
                    } else {
                        // TODO Other return case
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textResponse.setText(response += ("\n" + reply));
                            }
                        });
                    }
                }
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response = "UnknownHostException: " + e.toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response = "IOException: " + e.toString();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally{
				if(socket != null){
					try {
						socket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
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

    // TODO move to Util
    private static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        } catch(NullPointerException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }
}
