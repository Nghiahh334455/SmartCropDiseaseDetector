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

        // Hiển thị bình luận lồng nhau: Nếu là phản hồi thì thụt lề và ẩn nút Trả lời
        boolean isReply = comment.parentCommentId != null && !comment.parentCommentId.isEmpty();
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
        params.leftMargin = isReply ? 100 : 0; // Thụt lề 100px
        holder.itemView.setLayoutParams(params);
        
        holder.binding.btnReplyComment.setVisibility(isReply ? View.GONE : View.VISIBLE);

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
        
        if (likeCount > 0) {
            holder.binding.layoutReactions.setVisibility(View.VISIBLE);
            holder.binding.tvReactionCount.setText(String.valueOf(likeCount));
            holder.binding.ivReactionIcon.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            holder.binding.layoutReactions.setVisibility(View.GONE);
        }

        // Highlight if current user liked
        boolean isLiked = comment.likedBy.containsKey(currentUid);

        holder.binding.btnLikeComment.setTextColor(isLiked ? holder.itemView.getContext().getResources().getColor(R.color.primary) : holder.itemView.getContext().getResources().getColor(R.color.gray_dark));

        // Click listeners
        holder.binding.btnLikeComment.setOnClickListener(v -> listener.onLike(commentId, !isLiked));
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
