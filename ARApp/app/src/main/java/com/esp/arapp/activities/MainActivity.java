package com.esp.arapp.activities;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.esp.arapp.InstantTrackerActivity;
import com.esp.arapp.MainPagerAdapter;
import com.esp.arapp.R;

public class MainActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private MainPagerAdapter pagerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
    }

    private void initViews() {
        viewPager = findViewById(R.id.main_view_pager);
        pagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
    }

    public void onQRScanner(View view) {
        Intent intent = new Intent(this, QRCodeActivity.class);
        startActivity(intent);
    }

    public void onInstantClick(View view) {
        Intent intent = new Intent(this, InstantTrackerActivity.class);
        startActivity(intent);
    }
}
