package com.stevendrake.moviehub;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.stevendrake.moviehub.Database.Video;

import java.util.List;

/**
 * Created by calebsdrake on 8/6/2018.
 */

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private static List<Video> showVideos;

    class VideoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private final TextView videoTitle;
        private final TextView videoSite;
        private final TextView videoType;
        private final Button videoPlay;
        private String trailerId;

        public VideoViewHolder(View itemView) {
            super(itemView);
            videoTitle = itemView.findViewById(R.id.tv_trailers_cards_title);
            videoSite = itemView.findViewById(R.id.tv_trailers_cards_site);
            videoType = itemView.findViewById(R.id.tv_trailers_cards_type);
            videoPlay = itemView.findViewById(R.id.btn_trailers_cards_play);
            videoPlay.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            int position = getAdapterPosition();
            trailerId = showVideos.get(position).getVidkey();

            if (position != RecyclerView.NO_POSITION){
                Context context = view.getContext();
                Intent playTrailerIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(trailerId));
                Intent webTrailerIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + trailerId));
                try {
                    context.startActivity(playTrailerIntent);
                } catch (ActivityNotFoundException e){
                    context.startActivity(webTrailerIntent);
                }

            }
        }
    }

    private final LayoutInflater vidInflater;

    VideoAdapter(Context context){
        vidInflater = LayoutInflater.from(context);
    }

    @Override
    public VideoViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = vidInflater.inflate(R.layout.movie_trailers_cards, viewGroup, false);
        return new VideoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(VideoViewHolder holder, int position) {
        if (showVideos != null || showVideos.size() > 0) {
            Video current = showVideos.get(position);
            holder.videoTitle.setText(current.getName());
            holder.videoSite.setText(current.getSite());
            holder.videoType.setText(current.getType());
        }
    }

    void setVideos(List<Video> videos){
        showVideos = videos;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (showVideos == null) {
            return 0;
        } else {
            return showVideos.size();
        }
    }
}
