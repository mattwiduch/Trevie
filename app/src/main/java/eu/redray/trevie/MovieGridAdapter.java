/*
 * Copyright (C) 2015 Mateusz Widuch. All rights reserved.
 */

package eu.redray.trevie;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Creates custom adapter to display movie items in grid view.
 */
public class MovieGridAdapter extends ArrayAdapter<Movie> {
    private Context mContext;

    public MovieGridAdapter(Context context, int resourceId, List<Movie> items) {
        super(context, resourceId, items);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            // inflate the GridView item layout
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.movie_grid_item, parent, false);
            //set views
            viewHolder = new ViewHolder();
            viewHolder.moviePoster = (ImageView) convertView.findViewById(R.id.grid_movie_poster);
            viewHolder.favouriteIcon = (ImageView) convertView.findViewById(R.id.grid_movie_favourite);
            viewHolder.movieTitle = (TextView) convertView.findViewById(R.id.grid_movie_title);
            viewHolder.movieYear = (TextView) convertView.findViewById(R.id.grid_movie_genre);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Update view
        Movie movie = getItem(position);
        Picasso.with(mContext).load(movie.getPosterPath()).placeholder(R.drawable.temp)
                .error(R.drawable.error).fit().into(viewHolder.moviePoster);
        viewHolder.movieTitle.setText(movie.getTitle());
        viewHolder.movieTitle.setSelected(true);
        viewHolder.movieYear.setText(movie.getReleaseYear());
        // Set correct favourite icon
        setFavouriteIcon(viewHolder, movie);
        return convertView;
    }

    /**
     * Holds all the views used to display data in movie grid item.
     */
    private static class ViewHolder {
        ImageView moviePoster;
        ImageView favouriteIcon;
        TextView movieTitle;
        TextView movieYear;
    }

    /**
     * Sets correct favourite icon based on favourites collection
     */
    private void setFavouriteIcon(ViewHolder viewHolder, Movie movie) {
        if(movie.isFavourite(mContext)) {
            // Show icon if movie is favourite
            viewHolder.favouriteIcon.setVisibility(View.VISIBLE);
        } else {
            // Hide icon if movie is not favourite
            viewHolder.favouriteIcon.setVisibility(View.INVISIBLE);
        }
    }
}
