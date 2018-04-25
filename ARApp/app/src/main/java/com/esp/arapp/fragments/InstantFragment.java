package com.esp.arapp.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.esp.arapp.InstantTrackerActivity;
import com.esp.arapp.R;
import com.esp.arapp.VuforiaSamples.ui.ActivityList.ActivitySplashScreen;

/**
 * A simple {@link Fragment} subclass.
 */
public class InstantFragment extends Fragment implements View.OnClickListener {

    private View rootView;
    private Button tryItButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_instant, container, false);
        tryItButton = rootView.findViewById(R.id.try_it);
        tryItButton.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View view) {
        if (view == tryItButton) {
            Intent intent = new Intent(getContext(), InstantTrackerActivity.class);
            startActivity(intent);
//            String mClassToLaunchPackage = getContext().getPackageName();
//            String mClassToLaunch = mClassToLaunchPackage + ".activities.VuforiaARActivity";
//            Intent i = new Intent();
//            i.setClassName(mClassToLaunchPackage, mClassToLaunch);
//            startActivity(i);
        }
    }
}
