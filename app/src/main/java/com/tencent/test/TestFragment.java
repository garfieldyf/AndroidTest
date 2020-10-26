package com.tencent.test;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.ext.content.AsyncTask;
import android.ext.net.NetworkLiveData;
import android.ext.util.DeviceUtils;
import android.graphics.Color;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.tencent.test.TestFragmentActivity.TestViewModel;

public class TestFragment extends Fragment {
    private static final String TAG = "LifecycleTask";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = new View(getActivity());
        view.setBackgroundColor(Color.RED);
        new LifecycleTask(this).execute((Object[])null);

        TestViewModel viewModel = ViewModelProviders.of(getActivity()).get(TestViewModel.class);
        Log.d(TestFragmentActivity.TAG, "TestViewModel = " + DeviceUtils.toString(viewModel) + ", Data = " + DeviceUtils.toString(viewModel.getData()) + ", Value = " + DeviceUtils.toString(viewModel.getData().getValue()));

        NetworkLiveData.getInstance(getContext()).observe(this, new Observer<NetworkInfo>() {
            @Override
            public void onChanged(NetworkInfo info) {
                Log.d(TAG, "NetworkInfo = " + info);
            }
        });

        return view;
    }

    public static final class LifecycleTask extends AsyncTask<Object, Object, Object> {
        public LifecycleTask(Object owner) {
            super(owner);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onCancelled(Object o) {
            Log.w(TAG, "onCancelled - owner = " + getOwner());
        }

        @Override
        protected void onPostExecute(Object[] objects, Object o) {
            Log.d(TAG, "onPostExecute - owner = " + getOwner());
        }
    }
}
