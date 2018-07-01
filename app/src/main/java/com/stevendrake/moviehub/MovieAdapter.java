package com.stevendrake.moviehub;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by Steven Drake on 5/10/2018. For the Popular Films - Stage 1 project.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.PosterViewHolder> {

    private final int movieCount;

    public MovieAdapter(int numberOfMovies){
        movieCount = numberOfMovies;
    }

    @Override
    public PosterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
        Context context = viewGroup.getContext();
        int movieCardLayoutId = R.layout.movie_poster_cards;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean attachToParentImmediately = false;

        View view = inflater.inflate(movieCardLayoutId, viewGroup, attachToParentImmediately);

        return new PosterViewHolder(context, view);
    }

    public void onBindViewHolder(PosterViewHolder holder, int position){
        holder.bind(position);
    }

    @Override
    public int getItemCount(){
        return movieCount;
    }

    class PosterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // Create instances of the TextView and ImageView for the movie poster and title
        // from the movie_poster_cards layout
        final TextView moviePosterTitleView;
        final ImageView moviePosterImageView;
        final Context context;

        PosterViewHolder (Context context, View itemView){
            super(itemView);
            // Define the instances of the movie poster ImageView and the movie title TextView
            moviePosterTitleView = itemView.findViewById(R.id.hub_movie_title);
            moviePosterImageView = itemView.findViewById(R.id.iv_hub_movie_image);
            // define the context, and pass it into the click listener
            this.context = context;
            itemView.setOnClickListener(this);
        }

        // Handle the item being clicked
        @Override
        public void onClick(View view){
            int position = getAdapterPosition();
            String positionId = MovieData.movieIdNumber[getAdapterPosition()];
            if (position != RecyclerView.NO_POSITION){
                Context context = view.getContext();
                Intent detailIntent = new Intent(context, MovieDetail.class);
                detailIntent.putExtra("movieNumber", position);
                context.startActivity(detailIntent);
            }
        }
        void bind(int position){
            // Bind each instance of the movie poster ImageView and movie title TextView
            // with with its own data
            //
            // Update this to pull the data from the viewmodel instead of from the moviedata class
            // Research how to pull specific data from an object passed in to this method
            //
            moviePosterTitleView.setText(MovieData.movieTitles[position]);
            Picasso.with(itemView.getContext())
                    .load(MovieData.movieImageUrls[position])
                    .placeholder(R.drawable.movie_hub_logo_full)
                    .into(moviePosterImageView);
        }
    }
}