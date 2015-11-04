package com.redraysoftware.trevie;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

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
    private List<MovieDetails> mMovieList;

    public MovieGridFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //placeholder items
        mMovieList = new ArrayList<MovieDetails>();
        for (int i = 0; i < 33; i++) {
            MovieDetails item = new MovieDetails();
            mMovieList.add(item);
        }

        View view = inflater.inflate(R.layout.movie_grid_fragment, container, false);
        ButterKnife.bind(this, view);
        gridView.setAdapter(new MovieDetailsAdapter(getActivity(), R.layout.movie_grid_item, mMovieList));

        return view;
    }

    @OnItemClick(R.id.movie_grid)
    public void startMovieActivity(int position) {
        Intent intent = new Intent(getActivity(), MovieDetailsActivity.class);
        intent.putExtra("MovieDetails", mMovieList.get(position));
        startActivity(intent);
    }
}
