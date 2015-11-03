package com.redraysoftware.trevie;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.List;

/**
 * Created by frano on 03/11/2015.
 */
public class MovieItemAdapter extends ArrayAdapter<MovieItem> {
    private Context mContext;

    public MovieItemAdapter(Context context, int resourceId, List<MovieItem> items) {
        super(context, resourceId, items);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        MovieItem movieItem = getItem(position);

        if (convertView == null) {
            // inflate the GridView item layout
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.movie_grid_item, parent, false);

            //set views
            imageView = (ImageView) convertView.findViewById(R.id.movie_grid_poster);
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource(movieItem.getPosterId());
        return imageView;
    }
}
