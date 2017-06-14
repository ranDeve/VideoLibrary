package com.example.ran.videolibraryapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    // defining global variables
    MySqlHelper mySqlHelper;
    Cursor cursor;
    SimpleCursorAdapter adapter;
    int currentPosition;
    Animation animFadein;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //plumbing
        ImageView addBtn = (ImageView)findViewById(R.id.addMovieBtnMain);
        ListView listView = (ListView)findViewById(R.id.moviesLV);

        //creating SQLiteOpenHelper object
        mySqlHelper = new MySqlHelper(this);

        // making query from DB with cursor
        cursor= mySqlHelper.getReadableDatabase().query(DbConstants.tableName, null, null, null, null, null, null);


        // defining the relevant DB table column and view in String arrays for the cursor-adapter
        String[] fromColumn = new String[]{DbConstants.subjectColumn};

        int[] toItem = new int[]{R.id.movieItemTV};
        adapter = new SimpleCursorAdapter(this, R.layout.movie_item_layout, cursor, fromColumn, toItem);

        //setting the adapter for list
        listView.setAdapter(adapter);

        //registering relevant views for context menu
        registerForContextMenu(addBtn);
        registerForContextMenu(listView);

        //setting the rolling animation on the rolling film image
        animFadein = AnimationUtils.loadAnimation(getApplicationContext(),  R.anim.rolling);
        ((ImageView)findViewById(R.id.rollingFilmImg)).setAnimation(animFadein);


        // sending clicked list-item to AddEdit activity
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //cursor targets the matching row
                cursor.moveToPosition(i);
                //collecting the relevant info for edit activity
                String editableSubject = cursor.getString(cursor.getColumnIndex(DbConstants.subjectColumn));
                String editableBody = cursor.getString(cursor.getColumnIndex(DbConstants.bodyColumn));
                String editableUrl = cursor.getString(cursor.getColumnIndex(DbConstants.movieUrlColumn));
                int ids = cursor.getInt(cursor.getColumnIndex(DbConstants.idColumn));
                Intent intent = new Intent(MainActivity.this, AddEditActivity.class);
                intent.putExtra("subject", editableSubject);
                intent.putExtra("body", editableBody);
                intent.putExtra("id", ids);
                intent.putExtra("url", editableUrl);

                startActivity(intent);
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerForContextMenu(view);
                openContextMenu(view);
                unregisterForContextMenu(view);

            }
        });
    }

    @Override
    protected void onResume() {

        // refreshing the cursor-adapter while resuming to main activity
        super.onResume();
        cursor= mySqlHelper.getReadableDatabase().query(DbConstants.tableName, null, null, null, null, null, null);
        adapter.swapCursor(cursor);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();}


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // creating the options-menu
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // defining actions of options-menu items
        if(item.getItemId()==R.id.exitMenuItem)
        {
            //the exit button finishes the app
            finish();
            System.exit(0);
        }
        if(item.getItemId()==R.id.deleteallMenuItem)
        {
            // the delete button is specified to delete all SQL table elements. pops alert-dialog first

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder( MainActivity.this);
            // setting the title
            alertDialogBuilder.setTitle("Are you sure you want to delete all data from your library?");
            // setting the message
            alertDialogBuilder
                    .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            // deleting if user presses yes
                            mySqlHelper.getWritableDatabase().delete(DbConstants.tableName , null, null);
                            //afterward refreshing the data from the DB
                            cursor= mySqlHelper.getReadableDatabase().query(DbConstants.tableName, null, null, null, null, null, null);
                            adapter.swapCursor(cursor);
                        }                        })
                    .setNegativeButton("No",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                        }                        });
            // creating the alert-dialog
            AlertDialog alertDialog = alertDialogBuilder.create();
            // showing the alert-dialog
            alertDialog.show();
        }
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        //inflating the relevant context menu - from plus button or listView
        if(v.getId()==R.id.addMovieBtnMain)
        {
            getMenuInflater().inflate(R.menu.conmenu_adding, menu);
        }
        if(v.getId()==R.id.moviesLV)
        {
            getMenuInflater().inflate(R.menu.list_conmenu, menu);
            currentPosition = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        //defining edit or delete options
        if(item.getItemId()==R.id.editmovieListConmenu)
        {
            cursor.moveToPosition(currentPosition);
            String editableTitle = cursor.getString(cursor.getColumnIndex(DbConstants.subjectColumn));
            String editableDesc = cursor.getString(cursor.getColumnIndex(DbConstants.bodyColumn));
            int ids = cursor.getInt(cursor.getColumnIndex(DbConstants.idColumn));
            Intent intent = new Intent(this, AddEditActivity.class);
            intent.putExtra("subject", editableTitle);
            intent.putExtra("body", editableDesc);
            intent.putExtra("id", ids);
            startActivity(intent);
        }
        if(item.getItemId()==R.id.deletemovieListconmenu) {

            String name = cursor.getString(cursor.getColumnIndex(DbConstants.subjectColumn));
            final int ids = cursor.getInt(cursor.getColumnIndex(DbConstants.idColumn));

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
            // setting title
            alertDialogBuilder.setTitle("Are you sure you want to delete \"" + name + "\"?");
            // setting dialog message
            alertDialogBuilder
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mySqlHelper.getWritableDatabase().delete(DbConstants.tableName, "_id=?", new String[]{"" + ids});
                            cursor = mySqlHelper.getReadableDatabase().query(DbConstants.tableName, null, null, null, null, null, null);
                            adapter.swapCursor(cursor);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
        //defining add manually or add from internet options
        if(item.getItemId()==R.id.addManuallyConMenu)
        {
            Intent intent = new Intent(this, AddEditActivity.class);
            startActivity(intent);
        }
        if(item.getItemId()==R.id.addinternetConmenu)
        {
            //first checking if connected
            boolean isconnected = isNetworkAvailable();

            if(isconnected){
            Intent intent = new Intent(this, InternetActivity.class);
            startActivity(intent);}
            else {
                Toast.makeText(this, "Your are not connected!", Toast.LENGTH_SHORT).show();
            }
        }
        if(item.getItemId()==R.id.share)
        {

            //defining SHARE button
            cursor.moveToPosition(currentPosition);
            String editableTitle = cursor.getString(cursor.getColumnIndex(DbConstants.subjectColumn));
            String editableDesc = cursor.getString(cursor.getColumnIndex(DbConstants.bodyColumn));

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "WATCH "+editableTitle + " (recommended by Video Library App!)");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Plot: "+editableDesc);
            startActivity(Intent.createChooser(shareIntent, "Share Via"));

        }

        return true;
    }

}
