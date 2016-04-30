package com.example.miki.movieapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MovieDetails extends AppCompatActivity {
    LinearLayout mLinearLayout;
    LinearLayout mTrailersLayout;
    LinearLayout mReviewsLayout;
    LinearLayout mMovieDetailsLayout;
    LinearLayout mPosterLayout;
    static Movie selectedMovie;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        mLinearLayout = (LinearLayout) findViewById(R.id.linearlayout);
        mTrailersLayout = (LinearLayout) findViewById(R.id.trailerslayout);
        mReviewsLayout = (LinearLayout) findViewById(R.id.reviewslayout);
        mMovieDetailsLayout = (LinearLayout) findViewById(R.id.moviedetailslayout);
        mPosterLayout = (LinearLayout) findViewById(R.id.posterlayout);

        Intent intent = getIntent();
        String original_title = intent.getStringExtra("original_title");
        String overview = intent.getStringExtra("overview");
        String poster_path = intent.getStringExtra("poster_path");
        String release_date = intent.getStringExtra("release_date");
        String vote_average =intent.getStringExtra("vote_average");
        String id =intent.getStringExtra("id");

        selectedMovie = new Movie(original_title,overview,poster_path,Float.parseFloat(vote_average),release_date,Integer.parseInt(id));
        ArrayList<Movie> favourites = Movies_db.get_instance(getApplication()).getMovies();
        for (int i=0;i<favourites.size();i++)
        {
            if (selectedMovie.getId() == favourites.get(i).getId())
            {
                ImageButton favourite = (ImageButton) findViewById(R.id.favorite);
                favourite.setImageResource(R.drawable.star);
            }
        }

        TextView header = (TextView) findViewById(R.id.movieheadertextview);
        header.setText("Movie details\n\n");
        header.setTextAppearance(this, android.R.style.TextAppearance_Large);
        header.setTextColor(getResources().getColor(android.R.color.black));
        //mMovieDetailsLayout.addView(header);

        TextView details = (TextView) findViewById(R.id.moviedetailstextview);
        String fullDetails="";
        fullDetails += "<b>Movie Name</b><br><br>" + original_title + "<br><br>";
        fullDetails += "<b>Overview</b><br><br>" + overview + "<br><br>";
        fullDetails += "<b>Release date</b><br><br>" + release_date + "<br><br>";
        fullDetails += "<b>Average vote</b><br><br>" + vote_average;
        //TextView details = new TextView(this);
        details.setText(Html.fromHtml(fullDetails));
        //mMovieDetailsLayout.addView(details);

        TextView posterHeader = (TextView) findViewById(R.id.posterTextView);

        posterHeader.setText("Poster\n\n");
        posterHeader.setTextAppearance(this, android.R.style.TextAppearance_Large);
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setPadding(0, 0, 0, 100);
        String url = "http://image.tmdb.org/t/p/w780/" + poster_path;
        Picasso.with(this).load(url).into(imageView);

        new FetchMovieTrailer().execute(id);
        new FetchMovieReview().execute(id);


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    public class FetchMovieTrailer extends AsyncTask<String, Void,ArrayList<String>> {
        private final String LOG_TAG = FetchMovieTrailer.class.getSimpleName();


        @Override
        protected ArrayList<String> doInBackground(String ... params) {
            ArrayList <String> trailerList = new ArrayList<String>();
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String movieJsonStr = null;
            try {
                String baseUrl = "http://api.themoviedb.org/3/movie/"+params[0]+"/videos?api_key=";
                String apiKey = "94be95bc19036855dc9528bb41b89887";
                URL url = new URL(baseUrl.concat(apiKey));
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) {
                    return null;
                }
                movieJsonStr = buffer.toString();
                try {
                    JSONObject jsonRoot = new JSONObject(movieJsonStr);
                    JSONArray jsonMovies = jsonRoot.optJSONArray("results");

                    for (int i=0;i<jsonMovies.length();i++)
                    {
                        JSONObject jsonMovie = jsonMovies.getJSONObject(i);
                        String key = jsonMovie.optString("key").toString();
                        trailerList.add(key);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            return trailerList;
        }

        @Override
        protected void onPostExecute(final ArrayList<String> trailerList) {
            super.onPostExecute(trailerList);
            final Context mContext = getApplicationContext();

            TextView header = new TextView(mContext);
            header.setText("Trailers\n\n");
            header.setTextAppearance(mContext, android.R.style.TextAppearance_Large);
            header.setTextColor(getResources().getColor(android.R.color.black));
            //header.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));
            mTrailersLayout.addView(header);
            for (int i=0;i<trailerList.size();i++) {
                final Button temp = new Button(mContext);
                temp.setText("Trailer " + (i + 1));
                temp.setTextAppearance(mContext, android.R.style.TextAppearance_Large);
                temp.setTextColor(getResources().getColor(android.R.color.black));
                temp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String text = "" + temp.getText();
                        int index = Integer.parseInt("" + text.charAt(text.length() - 1)) - 1;
                        String URL = "https://www.youtube.com/watch?v=" + trailerList.get(index);
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(URL));
                        startActivity(intent);
                    }
                });
                mTrailersLayout.addView(temp);
            }
        }
    }

    public class FetchMovieReview extends AsyncTask<String, Void,ArrayList<Review>> {
        private final String LOG_TAG = FetchMovieTrailer.class.getSimpleName();


        @Override
        protected ArrayList<Review> doInBackground(String ... params) {
            ArrayList <Review> reviewsList = new ArrayList<Review>();
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String movieJsonStr = null;
            try {
                String baseUrl = "http://api.themoviedb.org/3/movie/"+params[0]+"/reviews?api_key=";
                String apiKey = "94be95bc19036855dc9528bb41b89887";
                URL url = new URL(baseUrl.concat(apiKey));
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) {
                    return null;
                }
                movieJsonStr = buffer.toString();
                //Log.v(LOG_TAG,"Fetched String = " + movieJsonStr);
                try {
                    JSONObject jsonRoot = new JSONObject(movieJsonStr);
                    JSONArray jsonMovies = jsonRoot.optJSONArray("results");
                    Review movieReview;
                    for (int i=0;i<jsonMovies.length();i++)
                    {
                        JSONObject jsonMovie = jsonMovies.getJSONObject(i);

                        String id = jsonMovie.optString("id").toString();
                        String author = jsonMovie.optString("author").toString();
                        String content = jsonMovie.optString("content").toString();
                        movieReview = new Review(id,author,content);
                        reviewsList.add(movieReview);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            return reviewsList;
        }

        @Override
        protected void onPostExecute(final ArrayList<Review> reviewsList) {
            super.onPostExecute(reviewsList);
            final Context mContext = getApplicationContext();
            TextView header = new TextView(mContext);
            header.setText("Users reviews\n\n");
            if (reviewsList.size() == 0 ) {
                header.append("No Reviews available for this movie");
            }
            header.setTextAppearance(mContext, android.R.style.TextAppearance_Large);
            header.setTextColor(getResources().getColor(android.R.color.black));
            mReviewsLayout.addView(header);
            for (int i=0;i<reviewsList.size();i++) {

                TextView reviewContent = new TextView(mContext);
                reviewContent.setText("' " + reviewsList.get(i).getContent() + " '");
                reviewContent.setTextSize(16f);
                reviewContent.setTextColor(getResources().getColor(android.R.color.black));
                reviewContent.setBackgroundColor(getResources().getColor(android.R.color.white));
                mReviewsLayout.addView(reviewContent);

                TextView reviewAuthor = new TextView(mContext);
                reviewAuthor.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                reviewAuthor.setText("\nBy user : " + reviewsList.get(i).getAuthor() + "\n\n");
                reviewAuthor.setTextAppearance(mContext, android.R.style.TextAppearance_Medium);
                reviewAuthor.setTypeface(null,Typeface.ITALIC);
                reviewAuthor.setTextColor(getResources().getColor(android.R.color.black));
                mReviewsLayout.addView(reviewAuthor);

            }
        }
    }

    public void addToFavourites(View view) {
        Movies_db.get_instance(getApplicationContext()).addMovie(selectedMovie);
        ImageButton favourite = (ImageButton) findViewById(R.id.favorite);
        favourite.setImageResource(R.drawable.star);
    }

}
