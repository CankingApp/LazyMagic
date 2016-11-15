package com.canking.inject.cxinjectdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.canking.inject.bindview.CxViewBinder;
import com.example.annotation.ViewBinder;

@ViewBinder(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CxViewBinder.bind(this);
    }
}
