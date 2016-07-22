package co.mobiwise.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import co.mobiwise.library.radio.RadioListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
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


public class MainActivity extends Activity {
    boolean flag;
    TextView output ;
    String loginURL="http://radioific.com/api/get_category_index/";
    String data = "";
    String[] mobileArray = {"Android","IPhone","WindowsMobile","Blackberry","WebOS","Ubuntu","Windows7","Max OS X"};
    ArrayList titles=new ArrayList<String>();
    RequestQueue requestQueue;
    private int selectedRow = 0;
    private ArrayList<String>urlData = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if(savedInstanceState!=null){
            Log.d("STATE",savedInstanceState.toString());
        }
        flag=true;
        requestQueue = Volley.newRequestQueue(this);
        // requestQueue.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        output = (TextView) findViewById(R.id.textView);
        JsonObjectRequest jor = new JsonObjectRequest(Request.Method.GET, loginURL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try{

                            JSONArray ja = response.getJSONArray("categories");

                            for(int i=0; i < ja.length(); i++){

                                JSONObject jsonObject = ja.getJSONObject(i);

                                String id = jsonObject.optString("id").toString();
                                titles.add(id);
                                String title = jsonObject.getString("title");
                                titles.add(title);
                                data += title+ "\n" ;
                            }
                            letsroll();
                            // output.setText(titles.get(9).toString());
                        }catch(JSONException e){e.printStackTrace();}
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley","Error");

                    }
                }
        );
        requestQueue.add(jor);


    }

    public void letsroll()
    {


        for (int n = 1; n < titles.size(); n += 2)
        {
            //  Log.d("Rock And Rolla",titles.get(n).toString());
            urlData.add(titles.get(n).toString());
        }

        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.activity_listview, R.id.label, urlData);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
        if (listView != null) {
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
                    view.setSelected(true);
                    Log.d("Now or never", "Position is " + position);
                    if (position != selectedRow) {
                        selectedRow = position;
                        Log.d("Runway", titles.get(position - 1).toString());
                        Intent intent = new Intent(MainActivity.this, RadioActivity.class);
                        intent.putExtra("id", titles.get(position * 2).toString());
                        intent.putExtra("flag", flag);
                        flag=false;
                        startActivity(intent);
                    }
                }
            });

        }
/*
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long arg3) {
                String value = (String) adapter.getItemAtPosition(position);
                // assuming string and if you want to get the value on click of list item
                // do what you intend to do on click of listview row
                Context context = getApplicationContext();
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, value, duration);
                toast.show();
                Intent intent = new Intent(Starter.this, MainActivity.class);
                intent.putExtra("slug", value);
                startActivity(intent);
            }
        });

*/

    }



}