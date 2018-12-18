package com.shinwari.storagefirebase;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Dialog;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.EditText;
import android.view.View;
import android.widget.Toast;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private ImageView  imageView;
    private EditText textImageName;
    private Uri imagUri;


    public static final String FB_STORAGE_PATH = "image/";
    public static final String FB_DATABASE_PATH = "image/";
    public static final int REQUEST_CODE = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference(FB_DATABASE_PATH);

        imageView = (ImageView)findViewById(R.id.imageView);
        textImageName = (EditText)findViewById(R.id.textImageName);
    }

    public void btnBrowse_click(View view) {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select image"),REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE && requestCode == RESULT_OK && data != null && data.getData() != null);
        imagUri = data.getData();
        try {
            Bitmap bm = MediaStore.Images.Media.getBitmap(getContentResolver(),imagUri);
            imageView.setImageBitmap(bm);
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public  String getImageExt(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    public void btnUpload_click(View view) {
        if(imagUri != null){
            final ProgressDialog  progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading Image");
            progressDialog.show();
             ///get storage referance
            StorageReference ref =  mStorageRef.child(FB_STORAGE_PATH + System.currentTimeMillis() + "." + getImageExt(imagUri));

            //add file
            ref.putFile(imagUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    //dismiss dialog on succss
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, "Image uploaded", Toast.LENGTH_SHORT).show();
                    ImaageUpload imageUpload = new ImaageUpload(textImageName.getText().toString(),taskSnapshot.getUploadSessionUri().toString());
                    //save image info to firebase database
                    String uploaded = mDatabaseRef.push().getKey();
                    mDatabaseRef.child(uploaded).setValue(imageUpload);


                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            //dismiss dialog on succss
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                       /////////show uploaded progress
                            double progress = (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage("Uploaded " +(int)progress + "%");
                        }
                    });

        }
        else {
            Toast.makeText(MainActivity.this,"Please upload the image", Toast.LENGTH_SHORT).show();
        }
    }
}
