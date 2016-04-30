package com.example.miki.movieapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;

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

public class MainActivity extends AppCompatActivity {
    public final String VIEW_MODE = "VIEW_MODE";
    private GridView gridView;
    private ImageAdapter imageAdapter;
    private static ArrayList<Movie> movieArrayList;
    private int viewmode;
    public static boolean isTablet = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if ( findViewById(R.id.moviedetailscontainer) != null )
            isTablet = true;
        gridView = (GridView) findViewById(R.id.gridview);
        movieArrayList = new ArrayList<Movie>();
        imageAdapter = new ImageAdapter(this,movieArrayList);
        gridView.setAdapter(imageAdapter);
        if (savedInstanceState != null)
            viewmode = savedInstanceState.getInt(VIEW_MODE,0);
        else
            viewmode = R.id.action_popular;
        if (viewmode == R.id.action_top_rated) {
            new FetchTopRatedMovies().execute();
        }
        else if (viewmode == R.id.action_popular) {
            new FetchPopularMovies().execute();
        }
        else if (viewmode == R.id.action_favourites) {
            movieArrayList = Movies_db.get_instance(getApplication()).getMovies();
            imageAdapter.setMoviesList(movieArrayList);
            imageAdapter.notifyDataSetChanged();
        }
        else {
            new FetchPopularMovies().execute();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if (isTablet == false)
                    startActivity(position);
                else {
                    Movie movie = getMovieDetails(position);
                    MovieDetailsFragment movieDetailsFrag = new MovieDetailsFragment();
                    movieDetailsFrag.setSelectedMovie(movie);
                    getSupportFragmentManager().beginTransaction().
                            replace(R.id.moviedetailscontainer, movieDetailsFrag ).commit();
                }
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_top_rated) {
            viewmode = id;
            new FetchTopRatedMovies().execute();
            gridView.setAdapter(imageAdapter);
            return true;
        }
        else if (id == R.id.action_popular) {
            viewmode = id;
            new FetchPopularMovies().execute();
            gridView.setAdapter(imageAdapter);
            return true;
        }
        else if (id == R.id.action_favourites) {
            viewmode = id;
            movieArrayList = Movies_db.get_instance(getApplication()).getMovies();
            imageAdapter.setMoviesList(movieArrayList);
            imageAdapter.notifyDataSetChanged();
            gridView.setAdapter(imageAdapter);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(VIEW_MODE,viewmode);
        super.onSaveInstanceState(outState);
    }

    public void startActivity (int position) {
        Intent intent = new Intent(this,MovieDetails.class);
        String original_title = movieArrayList.get(position).getOriginal_title();
        String overview = movieArrayList.get(position).getOverview();
        String poster_path = movieArrayList.get(position).getPoster_path();
        String release_date = movieArrayList.get(position).getRelease_date();
        String vote_average = ""+movieArrayList.get(position).getVote_average();
        String id = ""+movieArrayList.get(position).getId();
        intent.putExtra("original_title",original_title);
        intent.putExtra("overview",overview);
        intent.putExtra("poster_path",poster_path);
        intent.putExtra("release_date",release_date);
        intent.putExtra("vote_average",vote_average);
        intent.putExtra("id",id);
        startActivity(intent);
    }

    public Movie getMovieDetails (int position) {
        String original_title = movieArrayList.get(position).getOriginal_title();
        String overview = movieArrayList.get(position).getOverview();
        String poster_path = movieArrayList.get(position).getPoster_path();
        String release_date = movieArrayList.get(position).getRelease_date();
        float vote_average = movieArrayList.get(position).getVote_average();
        int id = movieArrayList.get(position).getId();
        Movie movie = new Movie(original_title,overview,poster_path,vote_average,release_date,id);
        return movie;
    }

    public class FetchPopularMovies extends AsyncTask<Void, Void, ArrayList<Movie>> {
        private final String LOG_TAG = FetchPopularMovies.class.getSimpleName();

        @Override
        protected ArrayList<Movie> doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String movieJsonStr = null;
            ArrayList <Movie> movies = new ArrayList<Movie>();
            try {
                String baseUrl = "http://api.themoviedb.org/3/movie/"+"popular"+"?api_key=";
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

                    for (int i=0;i<jsonMovies.length();i++)
                    {
                        JSONObject jsonMovie = jsonMovies.getJSONObject(i);
                        String original_title = jsonMovie.optString("original_title").toString();
                        String overview = jsonMovie.optString("overview").toString();
                        String poster_path = jsonMovie.optString("poster_path").toString();
                        String release_date = jsonMovie.optString("release_date").toString();
                        float vote_average = Float.parseFloat(jsonMovie.optString("vote_average").toString());
                        int id = Integer.parseInt(jsonMovie.optString("id").toString());
                        Movie movie = new Movie(original_title,overview,poster_path,vote_average,release_date,id);
                        movies.add(movie);
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
            return movies;
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> movies) {
            movieArrayList = movies;
            super.onPostExecute(movieArrayList);
            imageAdapter.setMoviesList(movieArrayList);
            imageAdapter.notifyDataSetChanged();

        }
    }

    public class FetchTopRatedMovies extends AsyncTask<Void, Void, ArrayList<Movie>> {
        private final String LOG_TAG = FetchTopRatedMovies.class.getSimpleName();

        @Override
        protected ArrayList<Movie> doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String movieJsonStr = null;
            ArrayList <Movie> movies = new ArrayList<Movie>();
            try {
                String baseUrl = "http://api.themoviedb.org/3/movie/"+"top_rated"+"?api_key=";
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

                    for (int i=0;i<jsonMovies.length();i++)
                    {
                        JSONObject jsonMovie = jsonMovies.getJSONObject(i);
                        String original_title = jsonMovie.optString("original_title").toString();
                        String overview = jsonMovie.optString("overview").toString();
                        String poster_path = jsonMovie.optString("poster_path").toString();
                        String release_date = jsonMovie.optString("release_date").toString();
                        float vote_average = Float.parseFloat(jsonMovie.optString("vote_average").toString());
                        int id = Integer.parseInt(jsonMovie.optString("id").toString());
                        Movie movie = new Movie(original_title,overview,poster_path,vote_average,release_date,id);
                        movies.add(movie);
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
            return movies;
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> movies) {
            movieArrayList = movies;
            super.onPostExecute(movieArrayList);
            imageAdapter.setMoviesList(movieArrayList);
            imageAdapter.notifyDataSetChanged();

        }
    }


    public void addToFavourites(View view) {
        Movies_db.get_instance(getApplicationContext()).addMovie(MovieDetailsFragment.movie);
        ImageButton favourite = (ImageButton) findViewById(R.id.favorite);
        favourite.setImageResource(R.drawable.star);
    }
}
