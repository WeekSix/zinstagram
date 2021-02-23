package com.example.zinstagram;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.content.Context;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

    private static String TAG = "RecyclerViewAdapter";

    private ArrayList<PostedPhoto> postedPhotos;
    private Context mContext;

    public RecyclerViewAdapter(Context context, ArrayList<PostedPhoto> postedPhotos) {
        //pass a list of photos to create RecyclerViewAdapter instance
        this.postedPhotos = postedPhotos;
        this.mContext = context;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        //find the layout imageview
        ImageView imageView;

        public MyViewHolder(final View view) {
            super(view);
            imageView = view.findViewById(R.id.imageView);
        }

    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate the imageView holder
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.image_view, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(itemView);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        //update the photo in image view
        //Glide.with(mContext).load(postedPhotos.get(position).getStorageRef()).into(holder.imageView);
        Picasso.get().load(postedPhotos.get(position).getStorageRef()).into(holder.imageView);
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //send new intent to open on click image.
                Log.d(TAG, "CLICK INTO OPEN IMAGE");
                Intent intent = new Intent(v.getContext(), ViewImageActivity.class);
                intent.putExtra("image_url", postedPhotos.get(position).getStorageRef());
                v.getContext().startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return postedPhotos.size();
    }
}
