package com.example.firebasechatdemo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.example.android.firebasechatdemo.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class StartActivity extends AppCompatActivity {
    @Bind(R.id.register_btn)
    Button registerBtn;
    @Bind(R.id.sign_in_btn)
    Button loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.register_btn)
    void openRegister() {
        startActivity(new Intent(StartActivity.this, RegisterActivity.class));
        finish();
    }
    @OnClick(R.id.sign_in_btn)
    void signIn(){
        startActivity(new Intent(StartActivity.this, LoginActivity.class));
        finish();
    }
}
