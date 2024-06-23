package com.example.hw2;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageViewHolder> {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    List<Message> Messages;

    public MessageAdapter(List<Message> messageList) {
        this.Messages = messageList;
        loadMessages();
    }

    private void loadMessages() {
        db.collection("Messages")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Messages.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Message c = new Message(
                                    document.getString("Avatar"),
                                    document.getString("Name"),
                                    document.getString("Text"),
                                    document.getString("ID")
                            );
                            Messages.add(c);
                        }
                        notifyDataSetChanged();
                    }
                });

        db.collection("Messages").addSnapshotListener((value, error) -> {
            if (error != null) {
                return;
            }
            Messages.clear();
            for (QueryDocumentSnapshot document : value) {
                Message c = new Message(
                        document.getString("Avatar"),
                        document.getString("Name"),
                        document.getString("Text"),
                        document.getString("ID")
                );
                Messages.add(c);
            }
            notifyDataSetChanged();
        });
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = Messages.get(position);
        Glide.with(holder.Avatar.getContext()).load(message.Avatar).into(holder.Avatar);
        holder.Name.setText(message.Name);
        holder.Text.setText(message.Text);
        holder.Card.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), MessageActivity.class);
            intent.putExtra("message", message);
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    (Activity) v.getContext(),
                    holder.Card,
                    "cardTransition"
            );
            v.getContext().startActivity(intent, options.toBundle());
        });
    }

    @Override
    public int getItemCount() {
        return Messages.size();
    }

    public void DeleteMessage(int pos) {
        Message m = Messages.get(pos);
        db.collection("Messages").document(m.ID).delete();
        Messages.remove(pos);
        notifyItemRemoved(pos);
    }
}

