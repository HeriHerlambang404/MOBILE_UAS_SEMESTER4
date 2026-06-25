package com.apk.catatkeuanganku.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.apk.catatkeuanganku.R;
import com.apk.catatkeuanganku.database.ChatMessage;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_USER = 1;
    private static final int TYPE_AI = 2;
    private List<ChatMessage> chatMessages;

    public ChatAdapter(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMessages.get(position).getRole().equals(ChatMessage.ROLE_USER)) {
            return TYPE_USER;
        } else {
            return TYPE_AI;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_USER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_user, parent, false);
            return new UserViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_ai, parent, false);
            return new AiViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = chatMessages.get(position);
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).tvMessageUser.setText(message.getText());
        } else {
            ((AiViewHolder) holder).tvMessageAi.setText(message.getText());
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageUser;
        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageUser = itemView.findViewById(R.id.tvMessageUser);
        }
    }

    static class AiViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageAi;
        AiViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageAi = itemView.findViewById(R.id.tvMessageAi);
        }
    }
}
