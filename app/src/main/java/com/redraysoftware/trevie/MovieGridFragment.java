package com.redraysoftware.trevie;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnItemClick;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieGridFragment extends Fragment {
    @Bind(R.id.movie_grid) GridView gridView;

    public MovieGridFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //placeholder items
        List<MovieItem> movieItems = new ArrayList<MovieItem>();
        for (int i = 0; i < 33; i++) {
            MovieItem item = new MovieItem();
            movieItems.add(item);
        }

        View view = inflater.inflate(R.layout.movie_grid_fragment, container, false);
        ButterKnife.bind(this, view);
        gridView.setAdapter(new MovieItemAdapter(getActivity(), R.layout.movie_grid_item, movieItems));

        return view;
    }

    @OnItemClick(R.id.movie_grid)
    public void startMovieActivity(int position) {
        Toast.makeText(getActivity(), "" + position,
                Toast.LENGTH_SHORT).show();
    }
}
