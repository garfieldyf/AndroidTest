package com.tencent.test;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import com.tencent.test.TestFragment.LifecycleTask;

public class TestFragmentActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_fragment);
        new LifecycleTask(this).execute((Object[])null);
    }

    public void onButtonClick(View view) {
        getSupportFragmentManager().beginTransaction()
            .add(android.R.id.content, new TestFragment())
            .addToBackStack(null)
            .commitAllowingStateLoss();
    }
}