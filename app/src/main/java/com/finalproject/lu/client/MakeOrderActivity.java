package com.finalproject.lu.client;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import android.widget.TextView;

import POJO.FoodsEnum;
import POJO.Message;
import POJO.Nodification;
import POJO.Order;


public class MakeOrderActivity extends AppCompatActivity {

    private ArrayList<Item> items;
    private GestureDetector mgd;
    private static final String TAG = "MakeOrderActivity";
    private ViewPager viewPager;
    private ArrayList<View> pageview;
    private MyOrder currentOrder;
    private Socket socket;
    private static List<Message> orderList;

    private int customerId;
    private String customerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainlayout);
        currentOrder = new MyOrder();
        inidata();
    }

    private void inidata(){
        viewPager = (ViewPager) findViewById(R.id.viewPager);

        LayoutInflater inflater = getLayoutInflater();
        View v1 = inflater.inflate(R.layout.activity_make_order,null);
        View v2 = inflater.inflate(R.layout.activity_view_order,null);

        pageview =new ArrayList<View>();
        pageview.add(v1);
        pageview.add(v2);

        PagerAdapter mpa = new PagerAdapter() {
            @Override
            public int getCount() {
                return pageview.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            public void destroyItem(View arg0, int arg1, Object arg2) {
                ((ViewPager) arg0).removeView(pageview.get(arg1));
            }

            public Object instantiateItem(View arg0, int arg1){
                ((ViewPager)arg0).addView(pageview.get(arg1));
                return pageview.get(arg1);
            }
        };
        viewPager.setAdapter(mpa); //set viewpager

        items = new ArrayList<>();
        Item it = new Item();
        it.setName(FoodsEnum.BURGERS.getName());
        it.setAmount(1);
        it.setDescription("Delicious Burger");
        it.setPrice(7.99f);
        it.setImg(R.drawable.burger);
        Item it1 = new Item();
        it1.setName(FoodsEnum.FRENCHFRIES.getName());
        it1.setAmount(2);
        it1.setDescription("Mild Fries");
        it1.setPrice(3.99f);
        it1.setImg(R.drawable.chips);
        items.add(it);
        items.add(it1);// set datas

        ListView listView = (ListView)v1.findViewById(R.id.listview);
        MyAdapter as = new MyAdapter(MakeOrderActivity.this,
                items,
                R.layout.itemslayout,
                new String[]{"image","name","price"},
                new int[]{R.id.imageView,R.id.textTitle,R.id.textPrice});
        listView.setAdapter(as);// set list view for menu


    }

    private void dispOrder(){
        LayoutInflater inflater = getLayoutInflater();
        View v = viewPager.getChildAt(1);
        //View v = inflater.inflate(R.layout.activity_view_order,null);

        TextView txtid = (TextView)v.findViewById(R.id.txtOrderID);
        if (txtid.getText().toString().equals("(Empty Bag)")){
            txtid.setText(currentOrder.getId());
            TextView txttime = (TextView)v.findViewById(R.id.txtOrderTime);
            txttime.setText(currentOrder.getIssuedDate().toString());
        }
        ListView listView = (ListView)v.findViewById(R.id.listview);
        ArrayList<Map<String, Object>> list = new ArrayList<>();
        for (Item i : currentOrder.getItems()){
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("Amount",i.getAmount());
            map.put("name",i.getName());
            map.put("price", "$"+i.getPrice());
            list.add(map);
        }

        MyOrderAdapter as = new MyOrderAdapter(MakeOrderActivity.this,
                currentOrder.getItems(),
                R.layout.cart_adapter,
                new String[] {"Amount","name","price"},
                new int[] {R.id.txtAmt,R.id.txtName,R.id.txtPrice});

        listView.setAdapter(as);
    }

    private void setupCustomFilterView(View customView){
        ListView listView2 = (ListView)customView.findViewById(R.id.listview);
        ArrayList<Map<String, Object>> list2 = new ArrayList<>();
        for (Item i : items){
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("Amount",i.getAmount());
            map.put("name",i.getName());
            map.put("price", "$"+i.getPrice());
            list2.add(map);
        }

        SimpleAdapter as2 = new SimpleAdapter(MakeOrderActivity.this,
                list2,
                R.layout.cart_adapter,
                new String[] {"Amount","name","price"},
                new int[] {R.id.txtAmt,R.id.txtName,R.id.txtPrice});
        listView2.setAdapter(as2);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    public void onClick_Submit(View view){
//
//        Intent intent = new Intent(this, TraceOrderActivity.class);
//        Bundle bundle = new Bundle();
//        currentOrder.setCurrentStatus(MyOrder.Status.Receive);
//        bundle.putSerializable("CurrentOrder",currentOrder);
//        intent.putExtras(bundle);
//        startActivity(intent);
        MyClientTask myClientTask = new MyClientTask();
        myClientTask.start();
        if (socket != null){
            SubmitThread submitThread = new SubmitThread();
            submitThread.start();
        }



    }

    public class MyAdapter extends BaseAdapter{

        private Context context;
        private ArrayList<?> data;
        private int layout;
        private String[] title;
        private int[] viewsId;
        private LayoutInflater myInflater;


        public  MyAdapter(Context context, ArrayList<?> data, int layoutId, String[] title, int[] viewsId){
            this.context = context;
            this.data=data;
            this.layout = layoutId;
            this.title = title;
            this.viewsId = viewsId;
            myInflater = LayoutInflater.from(context);

        }

        @Override
        public int getCount() {
            return this.data.size();
        }

        @Override
        public Object getItem(int i) {
            return this.data.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            final int position = i;
            Item item = (Item)data.get(i);
            view = myInflater.inflate(this.layout, null);
            final TextView txtName = (TextView)view.findViewById(R.id.textTitle);
            txtName.setText(item.getName());
            final TextView txtPrice = (TextView)view.findViewById(R.id.textPrice);
            txtPrice.setText("$"+item.getPrice());
            final ImageView img = (ImageView)view.findViewById(R.id.imageView);
            img.setImageResource(item.getImg());
            final Button btn = (Button)view.findViewById(R.id.btnAdd);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (currentOrder.getId()==null){
                        Date now = new Date();
                        currentOrder.setIssuedDate(now);
                        currentOrder.setId(now.getTime()+"");
                    }
                    for (Item item : currentOrder.getItems()){
                        if (item.getName().equals(txtName.getText())){
                            item.setAmount(item.getAmount()+1);
                            dispOrder();
                            return;
                        }
                    }
                    Item chosenitem = (Item)data.get(position);
                    chosenitem.setAmount(1);
                    currentOrder.getItems().add(chosenitem);
                    dispOrder();
                    return;
                }
            });

            return view;
        }

    }


    public class MyOrderAdapter extends BaseAdapter{

        private Context context;
        private ArrayList<?> data;
        private int layout;
        private String[] title;
        private int[] viewsId;
        private LayoutInflater myInflater;


        public  MyOrderAdapter(Context context, ArrayList<?> data, int layoutId, String[] title, int[] viewsId){
            this.context = context;
            this.data=data;
            this.layout = layoutId;
            this.title = title;
            this.viewsId = viewsId;
            myInflater = LayoutInflater.from(context);

        }

        @Override
        public int getCount() {
            return this.data.size();
        }

        @Override
        public Object getItem(int i) {
            return this.data.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            final int position = i;
            Item item = (Item)data.get(i);
            view = myInflater.inflate(this.layout, null);
            final TextView txtName = (TextView)view.findViewById(R.id.txtName);
            txtName.setText(item.getName());
            final TextView txtPrice = (TextView)view.findViewById(R.id.txtPrice);
            txtPrice.setText("$"+item.getPrice());
            final TextView txtAmt = (TextView)view.findViewById(R.id.txtAmt);
            txtAmt.setText(item.getAmount()+"");
            final Button btnAdd =(Button) view.findViewById(R.id.btnPlu);
            btnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for (Item item : currentOrder.getItems()){
                        if (item.getName().equals(txtName.getText())){
                            item.setAmount(item.getAmount()+1);
                            dispOrder();
                            return;
                        }
                    }
                }
            });

            Button btnMin = (Button)view.findViewById(R.id.btnMin);
            btnMin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for (Item item : currentOrder.getItems()){
                        if (item.getName().equals(txtName.getText())){
                            item.setAmount(item.getAmount()-1);
                            if (item.getAmount()<=0){
                                currentOrder.getItems().remove(item);
                            }
                            dispOrder();
                            return;
                        }
                    }
                }
            });

            return view;
        }

    }




    private class SubmitThread extends Thread {

        private String test;
//
//        SubmitThread(String test) {
//            this.test = test;
//        }

        @Override
        public void run() {
            ObjectOutputStream oos;

            try {
                OutputStream os = socket.getOutputStream();
                oos = new ObjectOutputStream(os);
//                //TODO hardcode sample code
//                Map<String, Integer> map = Collections.unmodifiableMap(Stream.of(
//                        new AbstractMap.SimpleEntry<>(FoodsEnum.BURGERS.getName(), 2),
//                        new AbstractMap.SimpleEntry<>(FoodsEnum.CHICHENS.getName(), 2),
//                        new AbstractMap.SimpleEntry<>(FoodsEnum.ONIONRINGS.getName(), 3),
//                        new AbstractMap.SimpleEntry<>(FoodsEnum.FRENCHFRIES.getName(), 5)
//                ).collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())));
                Map<String, Integer> map = new HashMap<>();
                for(Item i : currentOrder.getItems()){
                    map.put(i.getName(), i.getAmount());
                }
                Order order = new Order(orderList.size(), customerId, customerName, map);
                Message message = new Message(order, new Nodification(""), false, null);
                oos.writeObject(message);
                oos.flush();
                orderList.add(message);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                //test += "Something wrong! " + e.toString() + "\n";
            }

//            MainActivity.this.runOnUiThread(new Runnable() {
//
//                @Override
//                public void run() {
//                    textResponse.setText("Submitted");
//                }
//            });
        }

    }

    public class MyClientTask extends Thread {
        String reply = "";

        @Override
        public void run() {

            try {
                socket = new Socket("localhost", 8080);
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
//                        MainActivity.this.runOnUiThread(() -> textResponse.setText(response +=
//                                ("\n #" + orderId + " order has been received")));
                    }
                    if (isInteger) {
                        customerId = (Integer) object;
//                        MainActivity.this.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                textResponse.setText(response += "\n Welcome!!");
//                            }
//                        });
                    } else {
                        // TODO Other return case
//                        MainActivity.this.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                textResponse.setText(response += ("\n" + reply));
//                            }
//                        });
                    }
                }
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
               // response = "UnknownHostException: " + e.toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                //response = "IOException: " + e.toString();
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
}
