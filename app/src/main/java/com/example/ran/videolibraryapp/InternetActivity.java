package com.example.ran.videolibraryapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class InternetActivity extends AppCompatActivity {
    EditText nameofmovieET;
    ListView listView;
    ArrayList<MyMovie> arrayList;
    ArrayAdapter<MyMovie> adapter;
    String movieUrl, subject, movieId, movieSubject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internet);

        nameofmovieET= (EditText) findViewById(R.id.searchET_internetac);
        listView = (ListView)findViewById(R.id.internetmoviesLV);
            arrayList = new ArrayList<MyMovie>();
            adapter = new ArrayAdapter<MyMovie>(this, android.R.layout.simple_list_item_1, arrayList);
            listView.setAdapter(adapter);

        //preventing the soft keyboard from pushing other views and from popping up at start
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);


        //defining the Cancel button
        Button cancelBtn = (Button)findViewById(R.id.cancelBtn_internetac);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //defining the Go button
        Button goBtn = (Button)findViewById(R.id.goBtn_internetac);
            goBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    movieSubject = nameofmovieET.getText().toString();
                    DownloadWebsite downloadWebsite = new DownloadWebsite();
                    downloadWebsite.execute("http://www.omdbapi.com/?s="+movieSubject+"&r=json");
                }
            });

            //sending the chosen-movie details to edit activity
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    MyMovie current = arrayList.get(position);

                    Intent intent = new Intent(InternetActivity.this, AddEditActivity.class);
                    intent.putExtra("subject", current.subject);
                    intent.putExtra("url", current.movieUrl);
                    intent.putExtra("movieId", current.movieBodyId);
                    intent.putExtra("ismovie", true);

                    startActivity(intent);
                    finish();

                }        });
        }


    //async-task that presents the relevant movies the user searched

    public class DownloadWebsite extends AsyncTask<String, Long, String>
        {
            private ProgressDialog dialog;

            @Override
            protected void onPreExecute() {
                arrayList.clear();
                dialog = new ProgressDialog(InternetActivity.this);
                dialog.setTitle("searching "+movieSubject+"...");
                dialog.setMessage("please wait...");
                dialog.setCancelable(true);
                dialog.show();

            }

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
                {     ee.printStackTrace();
                    DownloadWebsite.this.cancel(true);
                    Toast.makeText(InternetActivity.this, "try again...", Toast.LENGTH_SHORT).show();
                }

                return response.toString();

                    //return response.toString();

            }

            @Override
            protected void onPostExecute(String resultJSON) {
                try {
                    JSONObject mainObject= new JSONObject(resultJSON);
                    JSONArray resultsArray= mainObject.getJSONArray("Search");
                    for(int i=0; i<resultsArray.length(); i++ )
                    {   JSONObject currentObject = resultsArray.getJSONObject(i);
                        subject= currentObject.getString("Title");
                        movieUrl = currentObject.getString("Poster");
                        movieId = currentObject.getString("imdbID");
                        arrayList.add(new MyMovie(subject, movieUrl, movieId));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();


                    adapter.notifyDataSetChanged();
                    nameofmovieET.setText("");

            }


            }
}
