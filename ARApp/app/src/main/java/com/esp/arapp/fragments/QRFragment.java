package com.esp.arapp.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.esp.arapp.activities.QRCodeActivity;
import com.esp.arapp.R;

public class QRFragment extends Fragment implements View.OnClickListener {

    private Button tryItButton;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_qr, container, false);
        tryItButton = rootView.findViewById(R.id.try_it);
        tryItButton.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View view) {
        if (view == tryItButton) {
            Intent intent = new Intent(getContext(), QRCodeActivity.class);
            startActivity(intent);
        }
    }
}
