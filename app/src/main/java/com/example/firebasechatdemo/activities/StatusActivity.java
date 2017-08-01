package com.example.firebasechatdemo.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import com.example.android.firebasechatdemo.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class StatusActivity extends AppCompatActivity {
    @Bind(R.id.status_toolbar)
    Toolbar mToolbar;
    @Bind(R.id.save_status_btn)
    Button mSaveStatusBtn;
    @Bind(R.id.status_input)
    TextInputLayout mStatusInput;
    private DatabaseReference mStatusDatabase;
    private FirebaseUser mCurrentUser;
    private ProgressDialog mProgressDialog;
    private String lastStatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        lastStatus = getIntent().getStringExtra("status");
        mStatusInput.getEditText().setText(lastStatus);
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uId = mCurrentUser.getUid();
        mStatusDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uId);
    }

    @OnClick(R.id.save_status_btn)
    void saveStatus() {
        String status = mStatusInput.getEditText().getText().toString().trim();
        if (TextUtils.isEmpty(status)) {
            mStatusInput.setError(getString(R.string.input_empty_error));
            return;
        }
        showLoading();
        mStatusDatabase.child("status").setValue(status).addOnCompleteListener(
                new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    mProgressDialog.dismiss();
                }else{
                    mProgressDialog.dismiss();
                    Toast.makeText(StatusActivity.this, task.getException().toString(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void showLoading(){
        mProgressDialog = new ProgressDialog(StatusActivity.this);
        mProgressDialog.setTitle("Saving Status");
        mProgressDialog.setMessage("Loading Please wait!");
        mProgressDialog.show();
    }
}
