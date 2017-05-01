package hr.from.bkoruznjak.accello;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.os.Bundle;

import com.crashlytics.android.Crashlytics;

import hr.from.bkoruznjak.accello.databinding.ActivityMainBinding;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends Activity {

    ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBinding.gameView.start(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mBinding.gameView.pause();
    }
}
