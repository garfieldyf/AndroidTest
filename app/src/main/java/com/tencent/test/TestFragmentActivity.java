package com.tencent.test;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProviders;
import android.ext.json.JSONArray;
import android.ext.json.JSONObject;
import android.ext.net.AsyncDownloadTask;
import android.ext.net.DownloadPostRequest;
import android.ext.net.DownloadRequest;
import android.ext.util.DeviceUtils;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import com.tencent.temp.NetworkStateLiveData;

public class TestFragmentActivity extends FragmentActivity implements Observer<JSONObject> {
    public static final String TAG = "abcd";
    TestViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_fragment);
        //new LifecycleTask(this).execute((Object[])null);

        mViewModel = ViewModelProviders.of(this).get(TestViewModel.class);
        mViewModel.getData().observe(this, new Observer<JSONObject>() {
            @Override
            public void onChanged(JSONObject result) {
                Log.d(TAG, "observe - result = " + DeviceUtils.toString(result));
            }
        });

        //mViewModel.getData().observeForever(this);
        Log.d(TAG, "TestViewModel = " + DeviceUtils.toString(mViewModel) + ", Data = " + DeviceUtils.toString(mViewModel.getData()) + ", Value = " + DeviceUtils.toString(mViewModel.getData().getValue()));

        // 164, 165, 181
        new JSONLoader(this).execute(164);

        NetworkStateLiveData.getInstance(this).observe(this, new Observer<State>() {
            @Override
            public void onChanged(State state) {
                Log.d(TAG, "NetworkState = " + state);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //mViewModel.getData().removeObserver(this);
    }

    @Override
    public void onChanged(JSONObject result) {
        Log.d(TAG, "observeForever - result = " + DeviceUtils.toString(result));
    }

    public void onButtonClick(View view) {
        getSupportFragmentManager().beginTransaction()
            .add(android.R.id.content, new TestFragment())
            .addToBackStack(null)
            .commitAllowingStateLoss();
    }

    public static final class TestViewModel extends ViewModel {
        private MutableLiveData<JSONObject> mData = new JSONLiveData();

        public TestViewModel() {
            Log.d(TAG, "TestViewModel ctor");
        }

        public final MutableLiveData<JSONObject> getData() {
            return mData;
        }
    }

    static final class JSONLiveData extends MutableLiveData<JSONObject> {
        @Override
        protected void onActive() {
            Log.d(TAG, "JSONLiveData onActive");
        }

        @Override
        protected void onInactive() {
            Log.d(TAG, "JSONLiveData onInactive");
        }
    }

    private static final class JSONLoader extends AsyncDownloadTask<Integer, Object, JSONObject> {
        private static final String URL = "http://appv2.funtv.bestv.com.cn/frontpage/all/tomato/v4/floors";

        public JSONLoader(TestFragmentActivity ownerActivity) {
            super(ownerActivity);
        }

        @Override
        protected DownloadRequest newDownloadRequest(Integer[] params) throws Exception {
            final JSONArray packages = new JSONArray();
            packages.add("com.bestv.mediapay");
            packages.add("com.mstar.tv.service");

            final JSONObject postData = new JSONObject()
                .put("brand", "funshion")
                .put("cpuType", "638")
                .put("deviceCode", "FD4351A-LU")
                .put("mac", "28:76:cd:03:c2:fa")
                .put("version", "7.1.1.1")
                .put("romVersion", "5.3.4.32_d")
                .put("packages", packages)
                .put("channelId", params[0]);

            return new DownloadPostRequest(URL)
                .post(postData)
                .readTimeout(30000)
                .connectTimeout(30000)
                .contentType("application/json");
        }

        @Override
        protected void onPostExecute(Integer[] params, JSONObject object) {
            final TestFragmentActivity activity = getOwner();
            if (activity != null) {
                Log.d(TAG, "onPostExecute");
                activity.mViewModel.getData().setValue(object);
            }
        }
    }
}