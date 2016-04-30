package com.example.miki.movieapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Miki on 4/9/2016.
 */
public class Movies_db extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MoviesDataBase";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_MOVIES = "movies";
    private static final String MOVIE_ORIGINAL_TITLE = "original_title";
    private static final String MOVIE_OVERVIEW = "overview";
    private static final String MOVIE_POSTER_PATH = "poster_path";
    private static final String MOVIE_VOTE_AVG = "vote_average";
    private static final String MOVIE_RELEASE_DATE = "release_date";
    private static final String MOVIE_ID = "id";
    private static Movies_db _instance;

    public void addMovie (Movie movie) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MOVIE_ORIGINAL_TITLE, movie.getOriginal_title());
            contentValues.put(MOVIE_OVERVIEW, movie.getOverview());
            contentValues.put(MOVIE_POSTER_PATH, movie.getPoster_path());
            contentValues.put(MOVIE_VOTE_AVG, movie.getVote_average());
            contentValues.put(MOVIE_RELEASE_DATE, movie.getRelease_date());
            contentValues.put(MOVIE_ID, movie.getId());
            db.insertOrThrow(TABLE_MOVIES, null, contentValues);
        }catch (Exception e) {
            Log.d("TAG", "Error while trying to add movie to database");
        }
            finally{
                db.setTransactionSuccessful();
                db.endTransaction();
            }
    }

    public ArrayList<Movie> getMovies ()  {
        ArrayList<Movie> movieArrayList = new ArrayList<Movie>();
        String SELECT_QUERY = String.format("SELECT * from %s", TABLE_MOVIES);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                cursor.moveToPrevious();
                while (cursor.moveToNext()) {
                    Movie movie = new Movie();
                    movie.setOriginal_title(cursor.getString(cursor.getColumnIndex(MOVIE_ORIGINAL_TITLE)));
                    movie.setOverview(cursor.getString(cursor.getColumnIndex(MOVIE_OVERVIEW)));
                    movie.setPoster_path(cursor.getString(cursor.getColumnIndex(MOVIE_POSTER_PATH)));
                    movie.setVote_average(cursor.getFloat(cursor.getColumnIndex(MOVIE_VOTE_AVG)));
                    movie.setRelease_date(cursor.getString(cursor.getColumnIndex(MOVIE_RELEASE_DATE)));
                    movie.setId(cursor.getInt(cursor.getColumnIndex(MOVIE_ID)));
                    movieArrayList.add(movie);
                }
            }
        }
        catch (Exception e) {
            Log.d("TAG", "Error while trying to get movies from database");
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return movieArrayList;
    }

    public static synchronized Movies_db get_instance(Context context) {

        if (_instance == null)
            _instance = new Movies_db(context.getApplicationContext());
        return _instance;
    }

    private Movies_db(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_MOVIES);
        String CREATE_MOVIES_TABLE = "CREATE TABLE " + TABLE_MOVIES + " ( " + MOVIE_ORIGINAL_TITLE + " VARCHAR(50) , "
                                                                            + MOVIE_OVERVIEW + " TEXT , "
                                                                            + MOVIE_POSTER_PATH + " VARCHAR(50) , "
                                                                            + MOVIE_VOTE_AVG + " FLOAT , "
                                                                            + MOVIE_RELEASE_DATE + " VARCHAR(50),"
                                                                            + MOVIE_ID + " INT PRIMARY KEY"
                                                                            + " )";
        sqLiteDatabase.execSQL(CREATE_MOVIES_TABLE);
        //Log.v("DBCON" , "Database constructed!");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        if (i != i1) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_MOVIES);
        }
        onCreate(sqLiteDatabase);
    }
}
