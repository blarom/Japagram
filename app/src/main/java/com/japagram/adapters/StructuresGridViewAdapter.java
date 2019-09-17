package com.japagram.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.japagram.R;

import java.util.List;

import androidx.annotation.NonNull;

public class StructuresGridViewAdapter extends ArrayAdapter {
    private final Context context;
    private final int layoutResourceId;
    private final List<Integer> imageIds;

    public StructuresGridViewAdapter(Context context, int layoutResourceId, List<Integer> imageIds) {
        super(context, layoutResourceId, imageIds);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.imageIds = imageIds;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        ViewHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.image = row.findViewById(R.id.list_item_structures_grid_image);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        int imageId = imageIds.get(position);
        holder.image.setBackgroundResource(imageId);
        return row;
    }

    class ViewHolder {
        ImageView image;

    }


}