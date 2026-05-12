package com.marc.helmet.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.marc.helmet.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * MARC AI chat rows: {@link #TYPE_USER}, {@link #TYPE_MARC}, {@link #TYPE_CORE} (Marc Core — red).
 *
 * <p>Layouts: {@code item_message_user}, {@code item_message_marc}, {@code item_message_core}.
 */
public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_USER = 0;
    public static final int TYPE_MARC = 1;
    public static final int TYPE_CORE = 2;

    private final List<ChatMessage> messages = new ArrayList<>();
    private final SimpleDateFormat timeFormat =
            new SimpleDateFormat("HH:mm", Locale.US);

    private RecyclerView recyclerView;

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        setRecyclerView(recyclerView);
    }

    public void setRecyclerView(RecyclerView rv) {
        this.recyclerView = rv;
    }

    public void addMessage(ChatMessage msg) {
        int pos = messages.size();
        messages.add(msg);
        notifyItemInserted(pos);
        scrollToEnd();
    }

    private void scrollToEnd() {
        if (recyclerView == null) {
            return;
        }
        int n = messages.size();
        if (n <= 0) {
            return;
        }
        recyclerView.post(() -> recyclerView.smoothScrollToPosition(n - 1));
    }

    public void clearMessages() {
        messages.clear();
        notifyDataSetChanged();
    }

    public List<ChatMessage> getHistory() {
        return Collections.unmodifiableList(new ArrayList<>(messages));
    }

    static int viewTypeForRole(String role) {
        if (role == null) {
            return TYPE_USER;
        }
        String r = role.toLowerCase(Locale.US);
        if ("core".equals(r)) {
            return TYPE_CORE;
        }
        if ("marc".equals(r)) {
            return TYPE_MARC;
        }
        return TYPE_USER;
    }

    @Override
    public int getItemViewType(int position) {
        return viewTypeForRole(messages.get(position).getRole());
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE_USER:
                return new ChatUserHolder(
                        inflater.inflate(R.layout.item_message_user, parent, false));
            case TYPE_CORE:
                return new ChatCoreHolder(
                        inflater.inflate(R.layout.item_message_core, parent, false));
            case TYPE_MARC:
            default:
                return new ChatMarcHolder(
                        inflater.inflate(R.layout.item_message_marc, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);
        String time = timeFormat.format(new Date(msg.getTimestamp()));
        if (holder instanceof ChatUserHolder) {
            ChatUserHolder h = (ChatUserHolder) holder;
            h.tvMessage.setText(msg.getContent());
            h.tvTimestamp.setText(time);
        } else if (holder instanceof ChatCoreHolder) {
            ChatCoreHolder h = (ChatCoreHolder) holder;
            h.tvMessage.setText(msg.getContent());
            h.tvTimestamp.setText(time);
        } else if (holder instanceof ChatMarcHolder) {
            ChatMarcHolder h = (ChatMarcHolder) holder;
            h.tvMessage.setText(msg.getContent());
            h.tvTimestamp.setText(time);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private static class ChatUserHolder extends RecyclerView.ViewHolder {

        final TextView tvMessage;
        final TextView tvTimestamp;

        ChatUserHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
        }
    }

    static class ChatMarcHolder extends RecyclerView.ViewHolder {

        final TextView tvMarcLabel;
        final TextView tvMessage;
        final TextView tvTimestamp;

        ChatMarcHolder(@NonNull View itemView) {
            super(itemView);
            tvMarcLabel = itemView.findViewById(R.id.tv_marc_label);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
        }
    }

    static final class ChatCoreHolder extends ChatMarcHolder {

        ChatCoreHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    /** One chat line: role {@code user} | {@code marc} | {@code core}. */
    public static class ChatMessage {

        private String role;
        private String content;
        private long timestamp;

        public ChatMessage() {
            this.timestamp = System.currentTimeMillis();
        }

        public ChatMessage(String role, String content, long timestamp) {
            this.role = role;
            this.content = content;
            this.timestamp = timestamp;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
}
