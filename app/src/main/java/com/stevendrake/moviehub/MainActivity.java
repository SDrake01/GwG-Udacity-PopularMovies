package com.stevendrake.moviehub;

import android.app.ProgressDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.stevendrake.moviehub.AsyncTasks.QueryAsyncTask;
import com.stevendrake.moviehub.Database.Film;
import com.stevendrake.moviehub.Database.FilmDao;
import com.stevendrake.moviehub.Database.FilmDatabase;
import com.stevendrake.moviehub.Database.ReviewsDao;
import com.stevendrake.moviehub.Database.VideoDao;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class MainActivity extends AppCompatActivity {

    // The TEST_LIST_LENGTH is to verify design properties and should be removed before release
    // it will be replaced by the database length when it is included so the app will work offline
    // private static final int TEST_LIST_LENGTH = 20;
    // TEST_LIST_LENGTH may no longer be necessary

    private RecyclerView movieRecyclerView;
    public static MovieAdapter movieGridAdapter;
    private static GridLayoutManager movieGridLayoutManager;
    private String sortFilter;

    public static FilmDao mainFilmDao;
    public static ReviewsDao mainReviewsDao;
    public static VideoDao mainVideoDao;
    private FilmDatabase moviesDb;

    // Create a ViewModel variable
    private FilmViewModel movieViewModel;

    // Create an instance of SharedPreferences and PreferenceChangeListener
    private SharedPreferences prefs;
    private PreferenceChangeListener prefChanges;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        moviesDb = FilmDatabase.getDatabase(this);
        mainFilmDao = moviesDb.filmDao();
        mainReviewsDao = moviesDb.reviewsDao();
        mainVideoDao = moviesDb.videoDao();

        // Get the ViewModel from the view model provider
        movieViewModel = ViewModelProviders.of(this).get(FilmViewModel.class);
        // Add an observer for the ViewModel
        movieViewModel.getMoviesList().observe(this, new Observer<List<Film>>() {
            @Override
            public void onChanged(@Nullable List<Film> films) {
                movieGridAdapter.setMovies(films);
            }
        });

        // Set the default filter_prefs values
        PreferenceManager.setDefaultValues(this, R.xml.filter_prefs, false);

        // Set and register the preference change listener to receive change notifications
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefChanges = new PreferenceChangeListener();
        prefs.registerOnSharedPreferenceChangeListener(prefChanges);

        // Define the RecyclerView instance to match the layout
        movieRecyclerView = findViewById(R.id.rv_movie_hub_recycler_view);

        // Create and assign the GridLayoutManager for the RecyclerView
        int gridNumber = 3;
        if (getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE){
            gridNumber = 5;
        } else {
            gridNumber = 3;
        }
        movieGridLayoutManager = new GridLayoutManager(this, gridNumber);
        movieRecyclerView.setLayoutManager(movieGridLayoutManager);

        // Set the MovieAdapter instance with the number of items
        movieGridAdapter = new MovieAdapter(this);

        // Set the adapter to the RecyclerView
        movieRecyclerView.setAdapter(movieGridAdapter);

        // Get the value for movie filter and the user's api key from Shared Preferences
        sortFilter = prefs.getString("sort_setting", "");
        String apiPref = prefs.getString("api_key_setting", "");
        setActivityTitle(sortFilter);

        // Create a connectivity manager that will be used to verify an internet connection
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        // Verify the device has an active internet connection
        // before trying to get data from the Api using the connectivity manager
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean activeConnection = (activeNetwork != null && activeNetwork.isConnectedOrConnecting());

        // Call the AsyncTask to load the movie data if the connection is active or connecting only
        // and if the MovieAdapter showMovies arraylist is empty
        if (MovieAdapter.showMovies == null) {
            if (activeConnection) {
                new getMovieRawJson().execute(sortFilter, apiPref);
            } else {
                Toast.makeText(this, "No active network connection", Toast.LENGTH_LONG).show();
            }
        }
    }

    // Register the preference change listener on Resume to avoid garbage collection
    @Override
    public void onResume(){
        super.onResume();
        prefs.registerOnSharedPreferenceChangeListener(prefChanges);
        setActivityTitle(sortFilter);
    }

    // Unregister the preference change listener on destroy as good practice
    @Override
    public void onDestroy(){
        super.onDestroy();
        prefs.unregisterOnSharedPreferenceChangeListener(prefChanges);
    }

    public class getMovieRawJson extends AsyncTask<String, Void, String> {

        // Create a progress spinner for the onPreExecute method
        ProgressDialog spinner = new ProgressDialog(MainActivity.this);
        @Override
        protected void onPreExecute(){
            //spinner = new ProgressDialog();
            spinner.setTitle("Please wait...");
            spinner.setMessage("Loading movie data");
            spinner.setCancelable(false);
            spinner.show();
        }

        @Override
        protected String doInBackground(String... params){

            // Create a variable used to pass the returned json data to the parsing method
            String httpJsonResults = null;

            // Build the url using the passed in parameters
            String movieSort = params[0];
            String apiKey = params[1];
            URL movieUrl = MovieNetwork.buildMovieUrl(movieSort, apiKey);

            // Query the Movie Database Api to get the movie json data
            try {
                httpJsonResults = MovieNetwork.getJsonFromHttpUrl(movieUrl);
            } catch (IOException e){
                e.printStackTrace();
            }

            // Parse the movie json data and populate the view arrays
            // only if httpJsonResults returns a value from the Api
            if (httpJsonResults != null) {
                try {
                    //MovieJson.parseMovieJsonData(httpJsonResults);
                    // parse the movie json data and populate the database
                    MovieJson.parseMovieJsonToDatabase(httpJsonResults, movieSort);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        protected void onPostExecute(String httpResults){
            // If I change the async task to return a Void, the onPostExecute doesn't run
            // I won't use the httpResults string for anything other than bug testing, but
            // things break if I eliminate it
            //
            // Dismiss the spinner and notify the adapter so it will refresh the recycler view
            spinner.dismiss();
            movieGridAdapter.notifyDataSetChanged();
        }
    }

    // Create a menu to open from the action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu){

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.moviehub_menu, menu);

        return true;
    }

    // Set the menu "Settings" item to open the settings activity
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int itemId = item.getItemId();
        if (itemId == R.id.app_settings){
            Intent intent = new Intent(this, MoviePreferencesActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class PreferenceChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener{

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals("sort_setting")){
                // Get the new preference value
                SharedPreferences newPref = getDefaultSharedPreferences(MainActivity.this);
                sortFilter = newPref.getString("sort_setting", "");
                String apiPref = newPref.getString("api_key_setting", "");
                // Run the Api query and update the recycler view with the new preference value
                new getMovieRawJson().execute(sortFilter, apiPref);
                if (MovieAdapter.showMovies == null) {
                    new QueryAsyncTask.getDatabaseFilms().execute(sortFilter);
                }
            }
        }
    }

    private void setActivityTitle(String sortValue){
        switch (sortValue){
            case "popular":
                getSupportActionBar().setTitle("Most Popular Movies");
                break;
            case "top_rated":
                getSupportActionBar().setTitle("Top Rated Movies");
                break;
            case "favorite":
                getSupportActionBar().setTitle("My Favorite Movies");
        }
    }
}
