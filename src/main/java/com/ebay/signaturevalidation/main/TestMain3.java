package com.ebay.signaturevalidation.main;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TestMain3 {

    public static void main(String[] args) {
        long temp = (int)3.9;
        temp%= 2;
        System.out.println(temp);
    }

}
