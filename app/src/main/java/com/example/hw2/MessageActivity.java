package com.example.hw2;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class MessageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            Message message = (Message) bundle.getSerializable("message");

            if (message != null) {
                TextView Name = findViewById(R.id.name);
                TextView Text = findViewById(R.id.text);
                ImageView Avatar = findViewById(R.id.avatar);

                // Check if message fields are not null before using them
                if (message.Avatar != null && !message.Avatar.isEmpty()) {
                    Glide.with(this).load(message.Avatar).into(Avatar);
                } else {
                    // Handle the case where Avatar URL is null or empty
                    Avatar.setImageResource(R.drawable.touchicon180); // Set a default avatar image
                }

                Name.setText(message.Name != null ? message.Name : "Unknown");
                Text.setText(message.Text != null ? message.Text : "No message text available");
            } else {
                // Handle the case where message is null
                // Show an error message or handle appropriately
                finish(); // Optionally, finish the activity
            }
        } else {
            // Handle the case where bundle is null
            // Show an error message or handle appropriately
            finish(); // Optionally, finish the activity
        }
    }
}
