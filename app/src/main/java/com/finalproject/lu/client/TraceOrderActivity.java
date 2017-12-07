package com.finalproject.lu.client;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class TraceOrderActivity extends AppCompatActivity {
    private Message currentOrder;
    private ArrayList<String> statuslist;
    private android.nfc.Tag Tag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trace_order);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        currentOrder = (Message)bundle.getSerializable("CurrentOrder");

        statuslist = new ArrayList<>();
        for (Message.Status s: Message.Status.values()){
            statuslist.add(s.getValue());
        }

        setAdapter();
    }

    private void setAdapter(){
        ArrayList<String> status = new ArrayList<>();
        for (int i = 0 ; i < statuslist.size(); i ++){
            status.add(statuslist.get(i));
            if (statuslist.get(i).equals(currentOrder.getCurrentStatus().getValue())){
                break;
            }
        }
        ListView listview = (ListView)findViewById(R.id.listTraceOrder);
        TraceOrderAdapter toa = new TraceOrderAdapter(this,status,R.layout.trace_adapter);
        listview.setAdapter(toa);
    }

    public void onClick_change(View view){
        Message.Status[] status = Message.Status.values();
        for(int i =0; i<status.length-1;i++){
            if (currentOrder.getCurrentStatus() == status[i]){
                currentOrder.setCurrentStatus(status[i+1]);
                break;
            }
        }
        setAdapter();
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

}
