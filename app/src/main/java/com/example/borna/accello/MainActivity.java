package com.example.borna.accello;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.os.Bundle;

import com.example.borna.accello.databinding.ActivityMainBinding;

public class MainActivity extends Activity {

    ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBinding.mainView.resume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        mBinding.mainView.pause();
    }
}
