package com.example.miki.movieapp;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Miki on 3/22/2016.
 */
public class ImageAdapter extends BaseAdapter {

    private Context mContext;
    private static ArrayList<Movie> moviesList = new ArrayList<Movie>();

    public ImageAdapter (Context mContext,ArrayList<Movie> moviesList)
    {
        this.mContext = mContext;
        this.moviesList=moviesList;
    }

    public void setMoviesList (ArrayList<Movie> moviesList)
    {
        this.moviesList = moviesList;
    }

    public ArrayList<Movie> getMoviesList ()
    {
        return this.moviesList;// = moviesList;
    }
    @Override
    public int getCount() {
        return moviesList.size();
    }

    @Override
    public Object getItem(int i) {
        return moviesList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null)
        {
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(200,300));
            imageView.setScaleType(ImageView.ScaleType.CENTER);
            imageView.setPadding(0,0,0,0);
        }
        else
        {
            imageView = (ImageView) convertView;
        }
        Movie movie = moviesList.get(position);
        String url = "http://image.tmdb.org/t/p/w185/" + movie.getPoster_path();
        Picasso.with(mContext).load(url).into(imageView);
        return imageView;
    }

}
