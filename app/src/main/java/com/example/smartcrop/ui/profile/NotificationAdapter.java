package com.example.smartcrop.ui.profile;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smartcrop.databinding.ItemNotificationBinding;
import com.example.smartcrop.models.NotificationModel;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private final List<NotificationModel> notificationList;
    private final OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationModel notification);
    }

    public NotificationAdapter(List<NotificationModel> notificationList, OnNotificationClickListener listener) {
        this.notificationList = notificationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNotificationBinding binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationModel notification = notificationList.get(position);

        String message = "";
        switch (notification.getType()) {
            case "LIKE":
                message = notification.getSenderName() + " đã thích bài viết của bạn: \"" + notification.getPostContent() + "\"";
                break;
            case "COMMENT":
                message = notification.getSenderName() + " đã bình luận về bài viết của bạn: \"" + notification.getPostContent() + "\"";
                break;
            case "SHARE":
                message = notification.getSenderName() + " đã chia sẻ bài viết của bạn: \"" + notification.getPostContent() + "\"";
                break;
        }

        holder.binding.tvNotificationMessage.setText(message);
        holder.binding.tvNotificationTime.setText(DateUtils.getRelativeTimeSpanString(notification.getTimestamp()));

        String avatar = notification.getSenderAvatar();
        if (avatar != null && (avatar.startsWith("BASE64:") || avatar.length() > 500)) {
            byte[] bytes = com.example.smartcrop.utils.ImageUtils.base64ToBytes(avatar);
            if (bytes != null) Glide.with(holder.itemView.getContext()).load(bytes).circleCrop().into(holder.binding.ivSenderAvatar);
        } else {
            Glide.with(holder.itemView.getContext())
                    .load(avatar)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .circleCrop()
                    .into(holder.binding.ivSenderAvatar);
        }

        holder.binding.viewUnread.setVisibility(notification.isRead() ? View.GONE : View.VISIBLE);
        holder.binding.layoutNotification.setBackgroundColor(notification.isRead() ? 0xFFFFFFFF : 0xFFF1F8E9);

        holder.itemView.setOnClickListener(v -> listener.onNotificationClick(notification));
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemNotificationBinding binding;
        ViewHolder(ItemNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
