package com.esp.arapp.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.esp.arapp.R;
import com.esp.arapp.activities.VuforiaARActivity;

public class VuforiaARFragment extends Fragment {

    private View rootView;
    private Button tryItButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_vuforia_ar, container, false);
        tryItButton = rootView.findViewById(R.id.try_it);
        tryItButton.setOnClickListener(v -> startActivity());
        return rootView;
    }

    private void startActivity() {
        String mClassToLaunchPackage = getContext().getPackageName();
        String mClassToLaunch = mClassToLaunchPackage + ".activities.VuforiaARActivity";
        Intent intent = new Intent();
        intent.setClassName(mClassToLaunchPackage, mClassToLaunch);
        startActivity(intent);
    }

}
