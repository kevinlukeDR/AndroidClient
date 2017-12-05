package com.finalproject.lu.client;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.widget.TextView;

import POJO.FoodsEnum;
import POJO.Message;
import POJO.Nodification;
import POJO.Order;


public class MakeOrderActivity extends AppCompatActivity {

    private ArrayList<Item> items;
    private ArrayList<String> statuslist;
    private ViewPager viewPager;
    private ArrayList<View> pageview;
    private MyOrder currentOrder;
    private Socket socket;
    private Socket listenSocket;
    String dstAddress;
    int dstPort;
    int listenPort;
    private static List<Message> orderList;

    private int customerId;
    private String customerName;
    final Context context = this;
    private ExecutorService submitPool = Executors.newCachedThreadPool();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
    }

    private void inidata(){
        orderList = new ArrayList<>();
        customerId = 0;
        customerName = "Jerry";
        currentOrder = new MyOrder();

        viewPager = (ViewPager) findViewById(R.id.viewPager);

        LayoutInflater inflater = getLayoutInflater();
        View v1 = inflater.inflate(R.layout.activity_make_order,null);
        View v2 = inflater.inflate(R.layout.activity_view_order,null);
        View v3 = inflater.inflate(R.layout.activity_trace_order,null);

        pageview =new ArrayList<View>();
        pageview.add(v1);
        pageview.add(v2);
        pageview.add(v3);
        pageview.get(2).setVisibility(View.GONE);

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

        String[] status = new String[]{"Submitting","Receiving","Preparing","Packaging","Delivery to front cashier"};
        statuslist = new ArrayList<>();
        for (String s: status){
            statuslist.add(s);
        }

        items = new ArrayList<>();
        Item it = new Item();
        it.setName(FoodsEnum.BURGERS.getName());
        it.setAmount(1);
        it.setPrice(7.99f);
        it.setImg(R.drawable.burger);
        Item it1 = new Item();
        it1.setName(FoodsEnum.FRENCHFRIES.getName());
        it1.setAmount(2);
        it1.setPrice(3.99f);
        it1.setImg(R.drawable.chips);
        Item it2 = new Item();
        it2.setName(FoodsEnum.CHICHENS.getName());
        it2.setAmount(3);
        it2.setImg(R.drawable.chicken);
        it2.setPrice(5.99f);
        Item it3 = new Item();
        it3.setName(FoodsEnum.ONIONRINGS.getName());
        it3.setPrice(4.49f);
        it3.setAmount(4);
        it3.setImg(R.drawable.onionring);
        items.add(it);
        items.add(it1);
        items.add(it2);
        items.add(it3);// set datas

        ListView listView = (ListView)v1.findViewById(R.id.listview);
        MyAdapter as = new MyAdapter(MakeOrderActivity.this,
                items,
                R.layout.itemslayout,
                new String[]{"image","name","price"},
                new int[]{R.id.imageView,R.id.textTitle,R.id.textPrice});
        listView.setAdapter(as);// set list view for menu


    }

    public void onClick_Order(View view){
        MyClientTask myClientTask = new MyClientTask("localhost", 8080, 8081);
        myClientTask.start();

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

        MyOrderAdapter as = new MyOrderAdapter(context,
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

        SimpleAdapter as2 = new SimpleAdapter(context,
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

        if (listenSocket != null){
            Map<String, Integer> map = new HashMap<>();
            for(Item i : currentOrder.getItems()){
                map.put(i.getName(), i.getAmount());
            }
            viewPager.setCurrentItem(2);
            submitPool.execute(new SubmitThread(map, false));
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

    private void setAdapter(Message message){
        ArrayList<String> status = new ArrayList<>();
        for (int i = 0 ; i < statuslist.size(); i ++){
            status.add(statuslist.get(i));
            if (statuslist.get(i).equals(message.getNodification().getNodification())){
                break;
            }
        }
        ListView listview = (ListView)pageview.get(2).findViewById(R.id.listTraceOrder);
        TraceOrderAdapter toa = new TraceOrderAdapter(this,status,R.layout.trace_adapter);
        listview.setAdapter(toa);
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

    public class TraceOrderAdapter extends BaseAdapter {
        private Context context;
        private ArrayList<String> data;
        private int layout;
        private LayoutInflater myInflater;

        public TraceOrderAdapter(Context c, ArrayList<String> data, int layoutId){
            this.context = c;
            this.layout = layoutId;
            myInflater = LayoutInflater.from(context);
            if(data.size() < statuslist.size()){
                data.add(statuslist.get(data.size()));
            }else {
                data.add("Ready to Pickup");
            }
            this.data = new ArrayList<>();
            for (int i = data.size()-1; i >=0; i--){
                this.data.add(data.get(i));
            }
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int i) {
            return data.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = myInflater.inflate(this.layout,null);
            final TextView txtStatus = (TextView)view.findViewById(R.id.txtStatus);
            txtStatus.setText(data.get(i));
            final ImageView img = (ImageView)view.findViewById(R.id.statusimg);
            if (i == 0){

//                Log.i("in",img.toString());
//                Drawable d = getResources().getDrawable(R.drawable.timeline_coming);
                //img.setImageResource(R.drawable.timeline_pass);
                img.setImageResource(R.drawable.timeline_coming);
            }else if(i == 1){

//                Log.i("in",img.toString());
//                Drawable d = getResources().getDrawable(R.drawable.timeline_current);
                //img.setImageResource(R.drawable.timeline_pass);
                img.setImageResource(R.drawable.timeline_current);
            }else {
                //Drawable d = getResources().getDrawable(R.drawable.timeline_pass);
                //img.setImageResource(R.drawable.timeline_pass);
                img.setImageResource(R.drawable.timeline_pass);
            }
            return view;
        }
    }

    private class SubmitThread extends Thread {
        Map<String, Integer> map;
        boolean partial;
        SubmitThread(Map<String, Integer> map, boolean partial) {
            this.map = map;
            this.partial = partial;
        }

        @Override
        public void run() {
            ObjectOutputStream oos;

            try {
                if (socket == null) {
                    socket = new Socket(dstAddress, dstPort);
                }
                oos = new ObjectOutputStream(socket.getOutputStream());
                //TODO hardcode sample code
                Order order = new Order(orderList.size(), customerId, customerName, map);
                Message message = partial ? new Message(order, new Nodification(Nodification.Status.PARTIAL.getStatus()), false, null) :
                new Message(order, new Nodification(Nodification.Status.SUBMIT.getStatus()), false, null);
                oos.writeObject(message);
                oos.flush();
                orderList.add(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    public class MyClientTask extends Thread {
        String reply = "";
        MyClientTask(String addr, int port, int listen){
            dstAddress = addr;
            dstPort = port;
            listenPort = listen;
        }

        @Override
        public void run() {

            try {
                listenSocket = new Socket(dstAddress, listenPort);
                while (true) {
                    InputStream is = listenSocket.getInputStream();
                    ObjectInputStream ois = new ObjectInputStream(is);
                    Object object = ois.readObject();
                    Message message = null;
                    boolean isInteger = false;
                    if (object instanceof String) {
                        reply = (String) object;
                        if (reply.equals("Closed")){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MakeOrderActivity.this);
                                    builder.setTitle("Status")
                                            .setMessage("Closed Now!")
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                }
                                            });
                                    builder.create();
                                    builder.show();
                                }
                            });
                            continue;
                        }else{

                        }
                    } else if (object instanceof Message) {
                        message = (Message) object;
                    } else if (object instanceof Integer) {
                        isInteger = true;
                    }
                    if (message != null){
                        int orderId = message.getOrder().getOrderId();
                        if (message.getNodification().getNodification().equals(
                                Nodification.Status.PARTIAL.getStatus())){

                            Message finalMessage = message;

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MakeOrderActivity.this);
                                    builder.setTitle("Delete entry")
                                            .setMessage("Are you sure you want to delete this entry?")
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    // TODO Solve "Skipped 437 frames!  The application may be doing too much work on its main thread." problem
                                                    // continue with place order
                                                    submitPool.execute(new SubmitThread((Map<String, Integer>) finalMessage.getOther(), true));

                                                }
                                            })
                                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                            orderList.get(orderId).getNodification().setNodification(Nodification.Status.CANCEL.getStatus());
                                                }
                                            });
                                    builder.create();
                                    builder.show();
                                    pageview.get(2).setVisibility(View.GONE);
                                }
                            });

                        }
                        else {
                            orderList.get(orderId).setNodification(message.getNodification());
                        }
                    }
                    if (isInteger) {
                        customerId = (Integer) object;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setContentView(R.layout.mainlayout);
                                inidata();
                            }
                        });
                    } else {
                        // TODO Other return case
                    }
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally{
                if(listenSocket != null){
                    try {
                        listenSocket.close();
                    } catch (IOException e) {
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
                e.printStackTrace();
            }
        }
    }
}
