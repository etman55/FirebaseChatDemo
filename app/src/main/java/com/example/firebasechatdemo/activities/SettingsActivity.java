package com.example.firebasechatdemo.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.firebasechatdemo.R;
import com.example.firebasechatdemo.utils.DocumentHelper;
import com.example.firebasechatdemo.utils.ImageUtils;
import com.example.firebasechatdemo.utils.PicassoCache;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 1;
    private static final String TAG = SettingsActivity.class.getSimpleName();
    private static final int MAX_LENGTH = 10;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 9001;
    @Bind(R.id.avatar)
    CircleImageView circleImageView;
    @Bind(R.id.display_name_txt)
    TextView mDisplayName;
    @Bind(R.id.status_txt)
    TextView mStatus;
    @Bind(R.id.change_img)
    Button mChangeImageBtn;
    @Bind(R.id.change_status)
    Button mChangeStatusBtn;
    private Uri imagePath;
    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;
    private StorageReference mStorageRef;
    private ProgressDialog mProgressDialog;
    private String uId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://fir-chatdemo-14d66.appspot.com/");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        uId = mCurrentUser.getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(uId);
        mUserDatabase.keepSynced(true);
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumbImage = dataSnapshot.child("thumb_image").getValue().toString();
                PicassoCache.get(SettingsActivity.this)
                        .load(image)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.mipmap.default_avatar)
                        .resizeDimen(R.dimen.user_avatar_size, R.dimen.user_avatar_size)
                        .centerCrop()
                        .into(circleImageView, new Callback() {
                            @Override
                            public void onSuccess() {

                            }
                            @Override
                            public void onError() {
                                Picasso.with(SettingsActivity.this)
                                        .load(image)
                                        .placeholder(R.mipmap.default_avatar)
                                        .resizeDimen(R.dimen.user_avatar_size, R.dimen.user_avatar_size)
                                        .centerCrop()
                                        .into(circleImageView);
                            }
                        });
                mDisplayName.setText(name);
                mStatus.setText(status);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @OnClick(R.id.change_status)
    void changeStatus() {
        String status = mStatus.getText().toString().trim();
        Intent i = new Intent(SettingsActivity.this, StatusActivity.class);
        i.putExtra("status", status);
        startActivity(i);
        finish();
    }

    @OnClick(R.id.change_img)
    void changeImage() {
        if (ContextCompat.checkSelfPermission(SettingsActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SettingsActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
            getIntent.setType("image/*");
            startActivityForResult(getIntent, PICK_IMAGE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE)
            if (resultCode == RESULT_OK) {
                imagePath = data.getData();
                if (data.getData() != null) {
                    showLoading();
                    String filePath = DocumentHelper.getPath(this, imagePath);
                    final Uri file = Uri.fromFile(new File(ImageUtils.compressImage(filePath)));
                    StorageReference fileStorage = mStorageRef.child("profile_images").
                            child("profile_" + mCurrentUser.getUid() + ".jpg");
                    final StorageReference thumbStorage = mStorageRef.child("profile_images")
                            .child("thumbs").child("profile_" + mCurrentUser.getUid() + ".jpg");
                    fileStorage.putFile(imagePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            final String downloadUrl = taskSnapshot.getDownloadUrl().toString();
                            UploadTask uploadTask = thumbStorage.putFile(file);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    String thumbUrl = task.getResult().getDownloadUrl().toString();
                                    if (task.isSuccessful()) {
                                        Map updateHash = new HashMap();
                                        updateHash.put("image", downloadUrl);
                                        updateHash.put("thumb_image", thumbUrl);
                                        mUserDatabase.updateChildren(updateHash).
                                                addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        mProgressDialog.dismiss();
                                                        if (!task.isSuccessful()) {
                                                            Log.d(TAG, "onFailure: " + task.getException());
                                                            Toast.makeText(SettingsActivity.this, task.getException().toString(),
                                                                    Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    } else {
                                        Log.d(TAG, "onFailure: " + task.getException());
                                        mProgressDialog.dismiss();
                                        Toast.makeText(SettingsActivity.this, task.getException().toString(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.d(TAG, "onFailure: " + exception.getMessage());
                            mProgressDialog.dismiss();
                            Toast.makeText(SettingsActivity.this, exception.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showLoading() {
        mProgressDialog = new ProgressDialog(SettingsActivity.this);
        mProgressDialog.setTitle("Saving Image");
        mProgressDialog.setMessage("Loading Please wait!");
        mProgressDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    getIntent.setType("image/*");
                    startActivityForResult(getIntent, PICK_IMAGE);
                } else {
                    Toast.makeText(SettingsActivity.this, "You should grant permission to proceed",
                            Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

}
