package com.tencent.test;

import android.arch.core.util.Function;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.Transformations;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

public class TestMediatorActivity extends FragmentActivity {
    public static final String TAG = "abcd";

    public MutableLiveData<String> mNameEvent = new MutableLiveData<>();
    public MutableLiveData<Integer> mEvent = new MutableLiveData<>();
    public MediatorLiveData<Integer> myMediatorLiveData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_fragment);

        myMediatorLiveData = (MediatorLiveData<Integer>)Transformations.map(mNameEvent, new Function<String, Integer>() {
            @Override
            public Integer apply(String input) {
                return input.length();
            }
        });

        myMediatorLiveData.addSource(mEvent, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                myMediatorLiveData.setValue(integer);
            }
        });

//        myMediatorLiveData = new MediatorLiveData<Integer>();
//        myMediatorLiveData.addSource(mNameEvent, new Observer<String>() {
//            @Override
//            public void onChanged(String s) {
//                myMediatorLiveData.setValue(s.length());
//            }
//        });

        myMediatorLiveData.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer o) {
                Log.d(TAG,"myMediatorLiveData="+o);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //myMediatorLiveData.removeSource(mNameEvent);
    }

    public void onButtonClick(View view) {
        String name="tan";
        mNameEvent.setValue(name);
        mEvent.setValue(120);
    }
}
