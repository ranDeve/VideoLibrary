package com.example.ran.videolibraryapp;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class AddEditActivity extends AppCompatActivity {

    // defining global variables
    EditText movieSubjectEditET, movieBodyEditET, movieUrlEditET;
    MySqlHelper mySqlHelper;
    Cursor cursor;
    boolean exists;
    String thebody;
    int id;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        //plumbing
        movieSubjectEditET = (EditText)findViewById(R.id.movieSubjectEditET);
        movieBodyEditET = (EditText)findViewById(R.id.movieBodyEditET);
        movieUrlEditET = (EditText)findViewById(R.id.movieUrlEditET);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);


        //getting intent-extra from main or internet activities
        final String theSubject = getIntent().getStringExtra("subject");
        final String theUrl = getIntent().getStringExtra("url");
        final String movieUrlId = getIntent().getStringExtra("movieUrlId");

        final String theBody = getIntent().getStringExtra("body");
        final String theId = getIntent().getStringExtra("movieId");
        final boolean ismovie = getIntent().getBooleanExtra("ismovie", false);

        id = getIntent().getIntExtra("id", -1);

       //setting movie details according to extras
        movieSubjectEditET.setText(theSubject);
        movieUrlEditET.setText(theUrl);
        movieBodyEditET.setText(theBody);

        if(ismovie) {
            AddEditActivity.DownloadWebsite downloadWebsite1 = new AddEditActivity.DownloadWebsite();
            downloadWebsite1.execute("http://www.omdbapi.com/?i=" + theId + "&plot=short&r=json");
        }


        mySqlHelper = new MySqlHelper(this);

        Button okBtn = (Button)findViewById(R.id.okBtn_AddEdit);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String subject = movieSubjectEditET.getText().toString();
                String body = movieBodyEditET.getText().toString();
                String imageUrl = movieUrlEditET.getText().toString();

                ContentValues contentValues = new ContentValues();
                contentValues.put(DbConstants.subjectColumn, subject);
                contentValues.put(DbConstants.bodyColumn, body);
                contentValues.put(DbConstants.movieUrlColumn, imageUrl);


                //set subject field to mandatory
                if (movieSubjectEditET.getText().toString().trim().equals("")) {
                    movieSubjectEditET.setError("Movie subject is required!");

                    movieSubjectEditET.setHint("Please enter a subject...");
                } else {

                    //calling the 'exists' method that checks if movie subject already exists in DB
                    Exists(movieSubjectEditET.getText().toString());


                    if (id == -1) {
                        Exists(movieSubjectEditET.getText().toString());
                        if (exists == false) {
                            //when movie does'nt exist, add to DB
                            mySqlHelper.getWritableDatabase().insert(DbConstants.tableName, null, contentValues);
                            finish();
                        }
                        if (exists == true) {
                            Toast.makeText(AddEditActivity.this, "The same Movie name already exists!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        //if movie exists, upddate the DB
                        mySqlHelper.getWritableDatabase().update(DbConstants.tableName, contentValues, "_id=?", new String[]{"" + id});
                        finish();
                    }
                }
            }
            });

        //defining the Cancel button
        Button cancelBtn = (Button)findViewById(R.id.cancelBtn_addedit);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        //difining the Show button to present the image using async-task
        ((Button)findViewById(R.id.showUrlBtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DownloadImgeTask downloadImgeTask= new DownloadImgeTask();
                downloadImgeTask.execute(theUrl);
            }
        });



    }

    public boolean Exists(String searchItem) {
        //a boolean method that checks whether movie subject already exists
        String[] columns = { DbConstants.subjectColumn };
        String selection = DbConstants.subjectColumn + " =?";
        String[] selectionArgs = { movieSubjectEditET.getText().toString() };
        String limit = "1";

        cursor = mySqlHelper.getReadableDatabase().query(DbConstants.tableName, columns, selection, selectionArgs, null, null, null, limit);
        exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }



    //async task that downloads the body of the movie
    public  class DownloadWebsite extends AsyncTask<String, Long, String>
    {
        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(AddEditActivity.this);
            dialog.setTitle("searching "+movieSubjectEditET.getText().toString()+"'s plot...");
            dialog.setMessage("please wait...");
            dialog.setCancelable(true);
            dialog.show();
        }

        private ProgressDialog dialog;

        @Override
        protected String doInBackground(String... params) {
            StringBuilder response= null;
            try{
                URL website = new URL(params[0]);
                URLConnection connection = website.openConnection();
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                connection.getInputStream()));
                response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null)
                    response.append(inputLine);
                in.close();
            } catch(Exception ee)
            {                      DownloadWebsite.this.cancel(true);
            }
            return response.toString();
        }

        @Override
        protected void onPostExecute(String resultJSON) {
            try {
                JSONObject mainObject = new JSONObject(resultJSON);
                thebody = mainObject.getString("Plot");
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
            dialog.dismiss();
            movieBodyEditET.append(thebody);
        }


    }


    //async-task that presents the movie image from existing url
    class DownloadImgeTask extends AsyncTask<String, Void, Bitmap> {
        private ProgressDialog dialog;
        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(AddEditActivity.this);
            dialog.setTitle("connecting");
            dialog.setMessage("please wait...");
            dialog.setCancelable(true);
            dialog.show();
        }
        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap= null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                InputStream in = (InputStream) url.getContent();
                bitmap = BitmapFactory.decodeStream(in);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return bitmap;
        }
        @Override
        protected void onPostExecute(Bitmap downloadedImage) {
            ((ImageView)findViewById(R.id.imageView2)).setImageBitmap(downloadedImage);
                        dialog.dismiss();
        }
    }




}
