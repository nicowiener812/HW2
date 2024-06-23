package com.example.hw2;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class AddMessageActivity extends AppCompatActivity {

    private static final String TAG = "AddMessageActivity";

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ImageView addPhoto;
    int REQUEST_PERMISSIONS_CODE = 1;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private Uri currentImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_message);

        addPhoto = findViewById(R.id.addPhotoImageView);

        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
            if (result) {
                addPhoto.setImageURI(currentImage);
                Log.d(TAG, "Image captured: " + currentImage.toString());
            } else {
                Log.e(TAG, "Image capture failed.");
            }
        });

        addPhoto.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(AddMessageActivity.this, new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.ACCESS_MEDIA_LOCATION
                }, REQUEST_PERMISSIONS_CODE);
            } else {
                captureImage();
            }
        });

        Button btn = findViewById(R.id.button);
        btn.setOnClickListener(v -> {
            Log.d(TAG, "Submit button clicked");
            EditText name = findViewById(R.id.name);
            EditText text = findViewById(R.id.text);
            if (currentImage != null) {
                StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                StorageReference fileRef = storageRef.child("images/" + Objects.requireNonNull(currentImage.getLastPathSegment()));
                fileRef.putFile(currentImage).addOnSuccessListener(taskSnapshot -> {
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        Log.d(TAG, "Image uploaded successfully: " + uri.toString());
                        saveMessage(uri.toString(), name.getText().toString(), text.getText().toString());
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to get download URL", e);
                    });
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Image upload failed", e);
                });
            } else {
                saveMessage("", name.getText().toString(), text.getText().toString());
            }
        });
    }

    public void saveMessage(String avatarUrl, String name, String text) {
        Message c = new Message(avatarUrl, name, text);
        db.collection("Messages").document(c.ID).set(c.getAsMap()).addOnSuccessListener(aVoid -> {
            Log.d(TAG, "DocumentSnapshot successfully written!");
            Intent i = new Intent();
            i.putExtra("message", c);
            setResult(RESULT_OK, i);
            finish();
        }).addOnFailureListener(e -> {
            Log.w(TAG, "Error writing document", e);
        });
    }

    private Uri createImageUri() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
        return getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    public void captureImage() {
        Uri imageUri = createImageUri();
        if (imageUri != null) {
            currentImage = imageUri;
            takePictureLauncher.launch(imageUri);
        }
    }
}
