package com.finalproject.lu.client;

import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    Calendar date = Calendar.getInstance();
    int currentHour = date.get(Calendar.HOUR_OF_DAY);

    @Test
    public void testRestaurantClosed(){
        //  int expectedHour = currentHour>19 || currentHour<11;
        assertEquals("Restaurant is closed",currentHour>19  || currentHour<11,true);


    }

    @Test
    public void testRestaurantOpened(){
        //  int expectedHour = currentHour>19 || currentHour<11;
        assertEquals("Restaurant is open",currentHour<=19 && currentHour>11,true);

    }
}