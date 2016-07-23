package co.mobiwise.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
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
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;

import co.mobiwise.library.radio.RadioListener;
import co.mobiwise.library.radio.RadioManager;

/**
 * Created by Shami 7/16/2016
 */
public class RadioActivity extends Activity implements RadioListener{

    private int ACTIVITY_START_CAMERA_APP = 0;

    //private final String[] RADIO_URL = {"http://hayatmix.net/;yayin.mp3.m3u"};
    private final String[] RADIO_URL = {"http://184.154.145.114:8128/"};
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
    Button localst;
    Button playpause;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radio);


        mRadioManager.registerListener(this);
        mRadioManager.setLogging(true);


        liploc=(Button)findViewById(R.id.LipLocBtn);

        liploc.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Intent callVideoAppIntent = new Intent();

                callVideoAppIntent.setAction(MediaStore.ACTION_VIDEO_CAPTURE);
                callVideoAppIntent.putExtra("android.intent.extra.durationLimit", 10);
                startActivityForResult(callVideoAppIntent, ACTIVITY_START_CAMERA_APP);
            }
        });

        localst=(Button)findViewById(R.id.globalbtn);



        Bundle extras = getIntent().getExtras();
        final String channel_id = extras.getString("id");
        flag=extras.getBoolean("flag");
        Context context=getApplicationContext();
        Toast toast=Toast.makeText(context, "The Flag is " + flag, Toast.LENGTH_SHORT);
        toast.show();

        int id=Integer.parseInt(channel_id);

        localst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent localstation = new Intent(RadioActivity.this, localstations.class);
                localstation.putExtra("id", channel_id);
                startActivity(localstation);
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

        loginURL="http://radioific.com/api/get_category_posts/?category_id="+id+"&count=500&include=title,url,custom_fields&custom_fields=customfield_reputation_1,customfield_music_link_1,customfield_categories,customfield_radiostation_name,customfield_latitude_geomywp,customfield_longitude_geomywp";
        //  flag=true;
        if(savedInstanceState!=null){
            Log.d("STATE", savedInstanceState.toString());
        }
        requestQueue = Volley.newRequestQueue(this);
        // requestQueue.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        JsonObjectRequest jor = new JsonObjectRequest(Request.Method.GET, loginURL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try{

                            JSONArray ja = response.getJSONArray("posts");

                            for(int i=0; i < ja.length(); i++){

                                JSONObject jsonObject = ja.getJSONObject(i);

                                // int id = Integer.parseInt(jsonObject.optString("id").toString());
                                String url=jsonObject.getString("customfield_music_link_1");
                                //       Log.d("Hey YO",url);
                                titles.add(url);

                                String title = jsonObject.getString("title");

                                titles.add(title);

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




}
