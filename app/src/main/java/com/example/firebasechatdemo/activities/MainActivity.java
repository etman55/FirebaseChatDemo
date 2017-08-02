package com.example.firebasechatdemo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.android.firebasechatdemo.R;
import com.example.firebasechatdemo.adapters.SectionsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    @Bind(R.id.main_toolbar)
    Toolbar mToolbar;
    @Bind(R.id.tab_pager)
    ViewPager mViewPager;
    @Bind(R.id.main_tabs)
    TabLayout mTabLayout;
    private SectionsAdapter sectionsAdapter;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("FireBase Demo");
        if (mAuth.getCurrentUser() != null)
            mUserRef = FirebaseDatabase.getInstance().getReference()
                    .child("Users").child(mAuth.getCurrentUser().getUid());
        sectionsAdapter = new SectionsAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(sectionsAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
//            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
            sendToStart();
        } else {
            mUserRef.child("online").setValue("true");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuth.getCurrentUser() != null) {
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.main_logout_btn) {
            FirebaseAuth.getInstance().signOut();
            if (mAuth.getCurrentUser() == null)
                mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
            sendToStart();
        }
        if (item.getItemId() == R.id.main_settings_btn)
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        if (item.getItemId() == R.id.main_users_btn)
            startActivity(new Intent(MainActivity.this, UserActivity.class));
        return true;
    }

    private void sendToStart() {
        startActivity(new Intent(MainActivity.this, StartActivity.class));
        finish();
    }
}
