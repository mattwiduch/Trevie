/*
 * Copyright (C) 2016 Mateusz Widuch
 */
package eu.redray.trevie;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Creates custom CursorAdapter to display records from favourites database
 */
public class FavouritesGridAdapter extends CursorAdapter {
    public FavouritesGridAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.movie_grid_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        // Read title from cursor
        String title = cursor.getString(MovieGridFragment.COL_MOVIE_TITLE);
        viewHolder.titleView.setText(title);
        viewHolder.titleView.setSelected(true);

        // Read release year from cursor
        String year = cursor.getString(MovieGridFragment.COL_MOVIE_RELEASE_DATE);
        viewHolder.yearView.setText(year.substring(0, 4));

        // Read poster path
        Picasso.with(context).load(cursor.getString(MovieGridFragment.COL_MOVIE_POSTER_PATH)).
                placeholder(R.drawable.temp).error(R.drawable.error).fit().into(viewHolder.posterView);
    }

    /** Holds all the views used to display data in movie grid item. */
    private static class ViewHolder {
        ImageView posterView;
        ImageView favouriteIconView;
        TextView titleView;
        TextView yearView;

        public ViewHolder(View view) {
            posterView = (ImageView) view.findViewById(R.id.grid_movie_poster);
            favouriteIconView = (ImageView) view.findViewById(R.id.grid_movie_favourite);
            titleView = (TextView) view.findViewById(R.id.grid_movie_title);
            yearView = (TextView) view.findViewById(R.id.grid_movie_genre);
        }
    }
}
