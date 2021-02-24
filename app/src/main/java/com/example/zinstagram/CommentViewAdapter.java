package com.example.zinstagram;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CommentViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int HEADER_FLAG = 0;
    private static final int ITEM_FLAG = 1;
    ArrayList<Comments> comments;
    private Context mContext;
    private String photoURL;
    private String caption;


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_FLAG) {
            //inflate comment layout and pass it to view holder
            return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_view, parent,false));
        } else if (viewType == HEADER_FLAG) {
            //Header
            return new HeadViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_header, parent, false));
        }

        throw new RuntimeException("Error: Could not find type " + viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            itemViewHolder.bind(comments.get(position - 1).getUsername(), comments.get(position-1).getComment(), comments.get(position-1).getProfileRef());
        }
        else if (holder instanceof HeadViewHolder) {
            HeadViewHolder headViewHolder = (HeadViewHolder) holder;
            headViewHolder.bind(caption, photoURL);
        }
    }

    @Override
    public int getItemCount() {
        return comments.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position)) {return HEADER_FLAG;}
        return ITEM_FLAG;
    }

    private boolean isPositionHeader(int position) { return position == 0;}

    public CommentViewAdapter(Context context, ArrayList<Comments> commentsList, String caption, String photoURL){
        this.mContext = context;
        this.comments = commentsList;
        this.photoURL = photoURL;
        this.caption = caption;
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        //Inflate comment view holder
        TextView username;
        TextView comments;
        ImageView imageView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.usernameComment);
            comments = itemView.findViewById(R.id.comment);
            imageView = itemView.findViewById(R.id.profileCommentImage);
        }

        void bind(String strUsername, String strComment, String strURL) {
            username.setText(strUsername);
            comments.setText(strComment);
            Picasso.get().load(strURL).into(imageView);
        }

    }

    class HeadViewHolder extends RecyclerView.ViewHolder {
        //Inflate comment header view holder
        ImageView photoImage;  //user's display photo
        TextView caption;

        public HeadViewHolder(View itemView) {
            super(itemView);
            photoImage = itemView.findViewById(R.id.imageHeaderView);
            caption = itemView.findViewById(R.id.captionHeader);
        }

        void bind(String strCaptionStr, String strURL) {
            caption.setText(strCaptionStr);
            Picasso.get().load(strURL).into(photoImage);
        }
    }
}


