package com.example.smartcrop.ui.forum;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smartcrop.R;
import com.example.smartcrop.databinding.ItemCommentBinding;
import com.example.smartcrop.models.CommentModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private final List<CommentModel> commentList;
    private final List<String> commentIds;
    private final OnCommentInteractionListener listener;
    private final String currentUid;

    public interface OnCommentInteractionListener {
        void onLike(String commentId, boolean isLike);
        void onSad(String commentId, boolean isSad);
        void onReply(CommentModel comment);
    }

    public CommentAdapter(List<CommentModel> commentList, List<String> commentIds, OnCommentInteractionListener listener) {
        this.commentList = commentList;
        this.commentIds = commentIds;
        this.listener = listener;
        this.currentUid = FirebaseAuth.getInstance().getUid();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCommentBinding binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CommentModel comment = commentList.get(position);
        String commentId = commentIds.get(position);

        holder.binding.tvCommentAuthor.setText(comment.authorName);
        holder.binding.tvCommentContent.setText(comment.content);
        holder.binding.tvCommentTime.setText(DateUtils.getRelativeTimeSpanString(comment.timestamp));
        
        // Load Avatar
        Glide.with(holder.itemView.getContext())
                .load(comment.authorPhotoUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.binding.ivCommentAvatar);

        // Reactions logic
        int likeCount = comment.likedBy.size();
        int sadCount = comment.sadBy.size();
        
        if (likeCount > 0 || sadCount > 0) {
            holder.binding.layoutReactions.setVisibility(View.VISIBLE);
            holder.binding.tvReactionCount.setText(String.valueOf(likeCount + sadCount));
            // Show star if likes, otherwise something else or just star
            holder.binding.ivReactionIcon.setImageResource(likeCount >= sadCount ? android.R.drawable.btn_star_big_on : android.R.drawable.ic_menu_info_details);
        } else {
            holder.binding.layoutReactions.setVisibility(View.GONE);
        }

        // Highlight if current user liked/sad
        boolean isLiked = comment.likedBy.containsKey(currentUid);
        boolean isSad = comment.sadBy.containsKey(currentUid);

        holder.binding.btnLikeComment.setTextColor(isLiked ? holder.itemView.getContext().getResources().getColor(R.color.primary) : holder.itemView.getContext().getResources().getColor(R.color.gray_dark));
        holder.binding.btnSadComment.setTextColor(isSad ? holder.itemView.getContext().getResources().getColor(R.color.error) : holder.itemView.getContext().getResources().getColor(R.color.gray_dark));

        // Click listeners
        holder.binding.btnLikeComment.setOnClickListener(v -> listener.onLike(commentId, !isLiked));
        holder.binding.btnSadComment.setOnClickListener(v -> listener.onSad(commentId, !isSad));
        holder.binding.btnReplyComment.setOnClickListener(v -> listener.onReply(comment));
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemCommentBinding binding;
        ViewHolder(ItemCommentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
