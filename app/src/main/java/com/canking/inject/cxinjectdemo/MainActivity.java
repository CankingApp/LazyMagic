package com.canking.inject.cxinjectdemo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.canking.inject.bindview.CxViewBinder;
import com.example.annotation.ViewBinder;

@ViewBinder(R.layout.activity_main)
public class MainActivity extends Activity {
    @ViewBinder(R.id.button)
    Button mBtn;
    @ViewBinder(R.id.textView)
    TextView mTxV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CxViewBinder.bind(this);

        Toast.makeText(this, "" + (mBtn != null ? mBtn.getText() : "Oh is NULL!") + (mTxV != null ? mTxV.getText() : "mTxV is NULL!"), Toast.LENGTH_SHORT).show();
    }
}
