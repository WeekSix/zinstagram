package com.example.zinstagram;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.content.Context;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

    private ArrayList<PostedPhoto> postedPhotos;
    private Context mContext;

    public RecyclerViewAdapter(Context context, ArrayList<PostedPhoto> postedPhotos) {
        //pass a list of photos to create RecyclerViewAdapter instance
        this.postedPhotos = postedPhotos;
        this.mContext = context;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        //find the layout imageview
        private ImageView imageView;

        public MyViewHolder(final View view) {
            super(view);
            imageView = view.findViewById(R.id.imageView);
        }

    }
    @NonNull
    @Override
    public RecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate the imageView holder
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.activity_image_view, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapter.MyViewHolder holder, int position) {
        //update the photo in image view
        //Glide.with(mContext).load(postedPhotos.get(position).getStorageRef()).into(holder.imageView);
        Picasso.get().load(postedPhotos.get(position).getStorageRef()).into(holder.imageView);
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ViewImageActivity.class);
                intent.putExtra("image_url", postedPhotos.get(position).getStorageRef());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return postedPhotos.size();
    }
}
