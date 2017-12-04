package com.finalproject.lu.client;

import android.graphics.drawable.Drawable;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by AY Study on 2017/11/24.
 */

public class Item implements Serializable {
    private String name;
    private float price;
    private int amount;
    private int img;


    public float getPrice() {
        return price;
    }

    public int getImg() {
        return img;
    }

    public void setImg(int img) {
        this.img = img;
    }

    public int getAmount() {
        return amount;
    }

    public String getName() {
        return name;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(float price) {
        this.price = price;
    }
}
