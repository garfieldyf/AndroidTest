package com.tencent.test;

import android.ext.content.AsyncTask;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TestFragment extends Fragment {
    private static final String TAG = "LifecycleTask";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = new View(getActivity());
        view.setBackgroundColor(Color.RED);
        new LifecycleTask(this).execute(MainApplication.sThreadPool );
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
