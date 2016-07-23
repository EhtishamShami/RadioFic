package co.mobiwise.myapplication;


import android.content.IntentSender;
import android.location.Location;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import co.mobiwise.library.radio.RadioListener;
import co.mobiwise.library.radio.RadioManager;
/**
 * Created by Shami 7/16/2016
 */
public class localstations extends Activity implements RadioListener,ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    //Define a request code to send to Google Play services
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double currentLatitude;
    private double currentLongitude;

    private int ACTIVITY_START_CAMERA_APP = 0;

    //private final String[] RADIO_URL = {"http://hayatmix.net/;yayin.mp3.m3u"};

    RadioManager mRadioManager = RadioManager.with(this);

    TextView output ;
    String loginURL;
    String data = "";
    ArrayList titles=new ArrayList<String>();
    RequestQueue requestQueue;
    private ArrayList<String>urlData = new ArrayList<>();
    private int selectedRow = 0;
    boolean flag;
    Button liploc;
    Button playpause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localstations);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                // The next two lines tell the new client that “this” current class will handle connection stuff
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                        //fourth line adds the LocationServices API endpoint from GooglePlayServices
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds


        mRadioManager.registerListener(this);
        mRadioManager.setLogging(true);


        liploc=(Button)findViewById(R.id.LipLocBtn);

        liploc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callVideoAppIntent = new Intent();

                callVideoAppIntent.setAction(MediaStore.ACTION_VIDEO_CAPTURE);
                callVideoAppIntent.putExtra("android.intent.extra.durationLimit", 10);
                startActivityForResult(callVideoAppIntent, ACTIVITY_START_CAMERA_APP);
            }
        });

        playpause=(Button)findViewById(R.id.stopbtn);
        playpause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mRadioManager.isPlaying()) {
                    mRadioManager.stopRadio();
                }

            }
        });

        Bundle extras = getIntent().getExtras();
        final String channel_id = extras.getString("id");
        flag=extras.getBoolean("flag");
        Context context=getApplicationContext();
        Toast toast=Toast.makeText(context, "The Flag is "+flag,Toast.LENGTH_SHORT);
        toast.show();
        int id=Integer.parseInt(channel_id);
        loginURL="http://radioific.com/api/get_category_posts/?category_id="+id+"&count=500&include=title,url,custom_fields&custom_fields=customfield_reputation_1,customfield_music_link_1,customfield_categories,customfield_radiostation_name,customfield_latitude_geomywp,customfield_longitude_geomywp";
        //  flag=true;
        if(savedInstanceState!=null){
            Log.d("STATE", savedInstanceState.toString());
        }
        requestQueue = Volley.newRequestQueue(this);
        // requestQueue.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

    }


    ////////////////////Roll will load the list when we will have it
    public void letsroll()
    {

        ListView urlList = (ListView)findViewById(R.id.stationlistview);
        Log.d("Size of title", "Oey Chootay" + titles.size());
        for (int n = 1; n < titles.size(); n += 2)
        {
            //  Log.d("Rock And Rolla",titles.get(n).toString());
            urlData.add(titles.get(n).toString());
        }
        ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, urlData);
        if (urlList != null) {
            urlList.setAdapter(adapter);
            urlList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
                    view.setSelected(true);
                    Log.d("Now or never", "Position is "+position);
                    if (position != selectedRow) {
                        selectedRow = position;
                        Log.d("Runway", titles.get(position * 2).toString());
                        String url = "http://prclive1.listenon.in:9998///"; // your URL here
                        mRadioManager.startRadio(titles.get(position * 2).toString());
                        /*
                        if (!mRadioManager.isPlaying()) {
                            //  mRadioManager.startRadio(titles.get(position - 1).toString());
                            mRadioManager.startRadio(titles.get(position * 2).toString());
                        }
                        else
                            mRadioManager.stopRadio();
*/

                    }
                }
            });
        }

    }



    ///Commented
/*
    public void initializeUI() {
       // mButtonControlStart = (Button) findViewById(R.id.buttonControlStart);
      //  mTextViewControl = (TextView) findViewById(R.id.textviewControl);

        mButtonControlStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mRadioManager.isPlaying())
                    mRadioManager.startRadio(RADIO_URL[0]);
                else
                    mRadioManager.stopRadio();
            }
        });

    }
*/
    @Override
    protected void onResume() {
        super.onResume();
        mRadioManager.connect();
        //Now lets connect to the API
        mGoogleApiClient.connect();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRadioManager.disconnect();
    }

    @Override
    public void onRadioLoading() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //TODO Do UI works here.
                //  mTextViewControl.setText("RADIO STATE : LOADING...");
            }
        });
    }

    @Override
    public void onRadioConnected() {

    }

    @Override
    public void onRadioStarted() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //TODO Do UI works here.
                // mTextViewControl.setText("RADIO STATE : PLAYING...");
            }
        });
    }

    @Override
    public void onRadioStopped() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //TODO Do UI works here
                //   mTextViewControl.setText("RADIO STATE : STOPPED.");
            }
        });
    }

    @Override
    public void onMetaDataReceived(String s, String s1) {
        //TODO Check metadata values. Singer name, song name or whatever you have.
    }

    @Override
    public void onError() {

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTIVITY_START_CAMERA_APP && resultCode == RESULT_OK) {
            Uri videoUri = data.getData();
            Context context = getApplicationContext();
            int length = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, videoUri.toString(), length);
            toast.show();

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("video/mp4");
            shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "My test");
            shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Lets see");
            shareIntent.putExtra(Intent.EXTRA_STREAM, videoUri);
            startActivity(Intent.createChooser(shareIntent, "Share video using"));


        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.v(this.getClass().getSimpleName(), "onPause()");

        //Disconnect from API onPause()
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }


    }

    /**
     * If connected get lat and long
     *
     */
    @Override
    public void onConnected(Bundle bundle) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        } else {
            //If everything went fine lets get latitude and longitude
            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();
            final int aprox_currentLantitude=(int)currentLatitude;
            final int aprox_currentLongitude=(int)currentLongitude;
            JsonObjectRequest jor = new JsonObjectRequest(Request.Method.GET, loginURL, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            try{

                                JSONArray ja = response.getJSONArray("posts");

                                for(int i=0; i < ja.length(); i++){

                                    JSONObject jsonObject = ja.getJSONObject(i);

                                    // int id = Integer.parseInt(jsonObject.optString("id").toString());
                                    int aprox_mylogi=0; int aprox_mylanti=0;
                                    String logititude = jsonObject.getString("customfield_longitude_geomywp");
                                    String lantitude = jsonObject.getString("customfield_latitude_geomywp");
                                    double mylogi;
                                    double mylanti;
                                  try {
                                      mylogi=Double.parseDouble(logititude);
                                      mylanti=Double.parseDouble(lantitude);
                                      aprox_mylogi = (int)mylogi;
                                      aprox_mylanti = (int)mylanti;
                                  }
                                  catch(NumberFormatException ex)
                                    {
                                        mylogi=0;
                                        mylanti=0;
                                        ex.printStackTrace();
                                    }
                                       Log.d("Hey YO", logititude + "    " + lantitude);
                                        if(aprox_currentLantitude==aprox_mylanti) {
                                        String url = jsonObject.getString("customfield_music_link_1");
                                        Log.d("Hey YO", logititude + "    " + lantitude);
                                        titles.add(url);
                                        String title = jsonObject.getString("title");
                                        titles.add(title);
                                    }
                                }
                                letsroll();

                            }catch(JSONException e){e.printStackTrace();}
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("Volley", "Error");

                        }
                    }
            );
            requestQueue.add(jor);

            Toast.makeText(this, currentLatitude + " WORKS " + currentLongitude + "", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
            /*
             * Google Play services can resolve some errors it detects.
             * If the error has a resolution, try sending an Intent to
             * start a Google Play services activity that can resolve
             * error.
             */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                    /*
                     * Thrown if Google Play services canceled the original
                     * PendingIntent
                     */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
                /*
                 * If no resolution is available, display a dialog to the
                 * user with the error.
                 */
            Log.e("Error", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    /**
     * If locationChanges change lat and long
     *
     *
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();

        Toast.makeText(this, currentLatitude + " WORKS " + currentLongitude + "", Toast.LENGTH_LONG).show();
    }



}
