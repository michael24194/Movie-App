package com.example.miki.movieapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
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

public class MovieDetailsFragment extends Fragment {
    View view;
    public static Movie movie;
    LinearLayout mTrailersLayout;
    LinearLayout mReviewsLayout;
    LinearLayout mMovieDetailsLayout;
    LinearLayout mPosterLayout;
    LinearLayout mLinearLayout;
    TextView reviewsHeader;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_movie_details,container);
        new doNothing().execute();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        mLinearLayout = (LinearLayout) view.findViewById(R.id.linearlayout);
        mTrailersLayout = (LinearLayout) view.findViewById(R.id.trailerslayout);
        mReviewsLayout = (LinearLayout) view.findViewById(R.id.reviewslayout);
        mMovieDetailsLayout = (LinearLayout) view.findViewById(R.id.moviedetailslayout);
        mPosterLayout = (LinearLayout) view.findViewById(R.id.posterlayout);

        if (mTrailersLayout != null)
        {
            mReviewsLayout.removeAllViewsInLayout();
            mTrailersLayout.removeAllViewsInLayout();

        }

        ArrayList<Movie> favourites = Movies_db.get_instance(getContext()).getMovies();
        ImageButton favourite = (ImageButton) view.findViewById(R.id.favorite);
        for (int i=0;i<favourites.size();i++)
        {
            if (movie.getId() == favourites.get(i).getId())
            {
                favourite.setImageResource(R.drawable.star);
                break;
            }
            else {
                favourite.setImageResource(R.drawable.offstar);
            }
        }

        TextView header = (TextView) view.findViewById(R.id.movieheadertextview);
        header.setText("Movie details\n\n");
        header.setTextAppearance(getContext(), android.R.style.TextAppearance_Large);
        header.setTextColor(getResources().getColor(android.R.color.black));

        TextView details = (TextView) view.findViewById(R.id.moviedetailstextview);
        String fullDetails="";
        fullDetails += "<b>Movie Name</b><br><br>" + movie.getOriginal_title() + "<br><br>";
        fullDetails += "<b>Overview</b><br><br>" + movie.getOverview() + "<br><br>";
        fullDetails += "<b>Release date</b><br><br>" + movie.getRelease_date() + "<br><br>";
        fullDetails += "<b>Average vote</b><br><br>" + movie.getVote_average();
        details.setText(Html.fromHtml(fullDetails));

        TextView posterHeader = (TextView) view.findViewById(R.id.posterTextView);

        posterHeader.setText("Poster\n\n");
        posterHeader.setTextAppearance(getContext(), android.R.style.TextAppearance_Large);
        ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
        imageView.setPadding(0, 0, 0, 100);
        String url = "http://image.tmdb.org/t/p/w780/" + movie.getPoster_path();
        Picasso.with(getContext()).load(url).into(imageView);

        TextView trailersHeader = new TextView(getContext());
        trailersHeader.setText("Trailers\n\n");
        trailersHeader.setTextAppearance(getContext(), android.R.style.TextAppearance_Large);
        trailersHeader.setTextColor(getResources().getColor(android.R.color.black));
        mTrailersLayout.addView(trailersHeader);

        reviewsHeader = new TextView(getContext());
        reviewsHeader.setText("Users reviews\n\n");
        reviewsHeader.setTextAppearance(getContext(), android.R.style.TextAppearance_Large);
        reviewsHeader.setTextColor(getResources().getColor(android.R.color.black));
        mReviewsLayout.addView(reviewsHeader);

        new FetchMovieTrailer().execute("" + movie.getId());
        new FetchMovieReview().execute("" + movie.getId());
    }

    public void setSelectedMovie(Movie movie) {
        this.movie = movie;
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
            final Context mContext = getContext();

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
            final Context mContext = getContext();
            if (reviewsList.size() == 0) {
                reviewsHeader.append("No Reviews available for this movie");
            }
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

    public class doNothing extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }



}
