package com.finalproject.lu.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by AY Study on 2017/11/26.
 */

public class MyOrder implements Serializable {
    private String id;
    private Date issuedDate;
    private ArrayList<Item> items;
    private Status currentStatus;

    public enum Status implements Serializable {
        Receive("Order received, Waiting in Queue"),
        Cooking("Order is been cooking"),
        Packing("Order is been Packing"),
        Complete("Order Completed, Waiting to pick up");


        private String value;
        private Status(String value){
            this.value = value;
        }

        public String getValue(){
            return value;
        }

    }

    public MyOrder(){
    items = new ArrayList<>();
    }

    public ArrayList<Item> getItems() {
        return items;
    }

    public Date getIssuedDate() {
        return issuedDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setIssuedDate(Date issuedDate) {
        this.issuedDate = issuedDate;
    }

    public float getTotalPrice(){
        float totalprice = 0;
        for (Item it : items){
            totalprice+= it.getAmount()*it.getPrice();
        }
        return totalprice;
    }

    public void setItems(ArrayList<Item> items) {
        this.items = items;
    }

    public Status getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(Status currentStatus) {
        this.currentStatus = currentStatus;
    }
}
