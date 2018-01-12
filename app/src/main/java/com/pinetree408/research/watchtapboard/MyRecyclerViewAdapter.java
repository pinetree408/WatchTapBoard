package com.pinetree408.research.watchtapboard;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

/**
 * Created by leesangyoon on 2017. 12. 22..
 */

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

    private List<String> mData = Collections.emptyList();
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private String inputString;

    // data is passed into the constructor
    public MyRecyclerViewAdapter(Context context, List<String> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.inputString = "";
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    // binds the data to the textview in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String animal = mData.get(position);

        if (inputString.length() > 0) {
            if (animal.split(" ")[0].startsWith(inputString)) {
                animal = "<font@color='green'><b>" + inputString + "</b></font>" + animal.substring(inputString.length(), animal.length());
            } else if (animal.split(" ")[1].startsWith(inputString)) {
                animal = animal.split(" ")[0] + " <font@color='green'><b>" + inputString + "</b></font>" + animal.split(" ")[1].substring(inputString.length(), animal.split(" ")[1].length());
            }
            animal = animal.split(" ")[0] + "<br>" + animal.split(" ")[1];
            animal = animal.replace("@", " ");
            holder.myTextView.setText(Html.fromHtml(animal));
        } else {
            animal = animal.split(" ")[0] + "\n" + animal.split(" ")[1];
            holder.myTextView.setText(animal);
        }

        holder.myTextView.setBackgroundResource(R.drawable.border);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView myTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            myTextView = (TextView) itemView.findViewById(R.id.recyclerview_item);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) {
                mClickListener.onItemClick(view, getAdapterPosition());
            }
        }
    }

    // convenience method for getting data at click position
    public String getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setInputString(String inputString) {
        this.inputString = inputString;
    }
}
