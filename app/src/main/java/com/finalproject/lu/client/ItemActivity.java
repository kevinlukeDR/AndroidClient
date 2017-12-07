package com.finalproject.lu.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import POJO.FoodsEnum;
import POJO.Message;

public class ItemActivity extends Activity {

    private Message currentOrder;
    Button button;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewitemlayout);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        currentOrder = (Message)bundle.getSerializable("Order");
        TextView txtOrderID = (TextView)findViewById(R.id.txtOrderID);
        txtOrderID.setText(currentOrder.getOrder().getOrderId()+"");
        getData();
        DecimalFormat df = new DecimalFormat("0.00");
        TextView itemprice = (TextView)findViewById(R.id.txtItemsPrice);
        TextView tax = (TextView)findViewById(R.id.txtTax);
        TextView totalprice = (TextView)findViewById(R.id.txtTotalPrice);
        itemprice.setText(df.format(getprice(currentOrder)));
        tax.setText(df.format(getprice(currentOrder)*0.06));
        totalprice.setText("$"+df.format(getprice(currentOrder)*1.06));
    }

    private void getData() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

        Message order = currentOrder;
        Map<String, Integer> foods = order.getOrder().getFoods();
        for(String key : foods.keySet()){
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("names", key);
            map.put("Quantity", foods.get(key));
            list.add(map);
        }
        ListView listviewi = (ListView) findViewById(R.id.listview2);
        SimpleAdapter sa = new SimpleAdapter(this, list, R.layout.itemadapter, new String[]{"names", "Quantity"}, new int[]{R.id.orderItem, R.id.OrderAmount});
        listviewi.setAdapter(sa);

    }

    private double getprice(Message m){
        double price = 0;
        Map<String, Integer> foods = m.getOrder().getFoods();
        for (String key : foods.keySet()){
            for (FoodsEnum f : FoodsEnum.values()){
                if (f.getName().equals(key)){
                    price+= f.getPrice()*foods.get(key);
                }
            }
        }
        return price;
    }
}