package com.example.indoo.mixslack_1;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Hashtable;

public class MainActivity extends AppCompatActivity {

    private static final String token = "xoxb-456534988487-463490781060-ACLt0V65iEtDUDxNQMssltXb";
    private static final String tokenUser = "xoxp-456534988487-455488467666-463661678274-4ee432a3602f872e82b9ad2f65377ba1";
    private static final String url = "https://slack.com/api/chat.postMessage?";
    private static final String channel = "CDDBC1BT5";
    private static final String text = "Someone needs help at the front desk! If you want to help, reply yes to this message";
    final String[] unique_ts = new String[1];
    final boolean[] replied = {false};
    final Hashtable<String, String> id_name_table = new Hashtable<String, String>();
    final String[] repliedMsg = new String[1];
    final String[] user_name = {null};
    //URLS
    String name_url = "https://slack.com/api/users.list?token=" + token + "&pretty=1";
    String postMsg_url = url + "token=" + token + "&channel=" + channel + "&text=" + text + "&pretty=1";
    private Button help_button;
    private AlertDialog.Builder alertIfResponded, alertIfTimeout;
    private ProgressBar progrssBarForReplies;
    private boolean clickedRespondAlert, clickedTimeoutAlert, showProgessDialog;
    private TextView mTextView;
    private RequestQueue queue;
    private int timer = 0;
    private AsyncTask asyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //To initiate the AlertDialogs for Responded case and TImeout case
        alertIfResponded = new AlertDialog.Builder(this);
        alertIfTimeout = new AlertDialog.Builder(this);

        //Set the booleans to false
        showProgessDialog = false;
        clickedRespondAlert = false;
        clickedTimeoutAlert = false;

        //Set the id for help_button
        help_button = findViewById(R.id.help_button);
        mTextView = findViewById(R.id.help_textView);
        ///Set ID for progressBar used when checking for replies
        progrssBarForReplies = findViewById(R.id.replyProgressBar);
        //If we don't want to show the progress dialog, hide it
        progrssBarForReplies.setVisibility(View.GONE);

        //set attributes for alert messages:
        alertIfResponded.setCancelable(false);
        alertIfResponded.setTitle("Incoming!");
        alertIfResponded.setNeutralButton("Okay!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progrssBarForReplies.setVisibility(View.GONE);
                help_button.setVisibility(View.VISIBLE);
                mTextView.setText("Please wait while someone responds..");
            }
        });

        alertIfTimeout.setCancelable(false);
        alertIfTimeout.setTitle("Such Late. Much Time!");
        alertIfTimeout.setMessage("Everyone busy! Try again easy!");
        alertIfTimeout.setNegativeButton("Ja!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progrssBarForReplies.setVisibility(View.GONE);
                help_button.setVisibility(View.VISIBLE);
                mTextView.setText("Need help?\nTap the button below!");
            }
        });
        //String Requests
        // Request a string response for NAMES URL.
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, name_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Code for names to hashTable:
                        Log.v("stringRequest0", response);
                        try {
                            JSONObject namesJSON = new JSONObject(response);
                            JSONArray name_id_array;
                            name_id_array = namesJSON.getJSONArray("members");
                            JSONObject arrayElem;
                            for (int i = 0; i < name_id_array.length(); i++) {
                                arrayElem = (JSONObject) name_id_array.get(i);
                                if (arrayElem.has("real_name")) {
                                    id_name_table.put(arrayElem.getString("id"), arrayElem.getString("real_name"));
                                } else
                                    id_name_table.put(arrayElem.getString("id"), "Someone");
                            }
                        } catch (JSONException ignored) {
                            mTextView.setText("That didn't work1!");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mTextView.setText("That didn't work2!");
            }
        });

        // Request a string to post a message into SLACK and getback its unique_ts:
        final StringRequest stringRequest1 = new StringRequest(Request.Method.GET, postMsg_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Code for getting unique_ts of the posted message:
                        Log.v("stringRequest1", response);
                        try {
                            JSONObject postMsgResp = new JSONObject(response);
                            JSONObject msgObj = postMsgResp.getJSONObject("message");
                            unique_ts[0] = msgObj.getString("ts");
                        } catch (JSONException ignored) {
                            mTextView.setText("That didn't work3!");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mTextView.setText("That didn't work4!");
            }
        });

        help_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer = 0;
                asyncTask = new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] objects) {
                        Log.v("BACKGROUND", "Timer = " + timer);

                        try {
                            while (timer < 120) {
                                Thread.sleep(2000);
                                timer += 2;
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                };
                asyncTask.execute();
                unique_ts[0] = "";
                replied[0] = false;
                user_name[0] = "";
                if (!replied[0]) {
                    help_button.setVisibility(View.GONE);
                    progrssBarForReplies.setVisibility(View.VISIBLE);
                    mTextView.setText("Please wait while someone responds..");
                }
                //Show the progress bar here and hide the button

                // Instantiate the RequestQueue.
                queue = SingletonRequestQueue.getInstance(MainActivity.this).getmRequestQueue();
                // Add the NAMES request to the RequestQueue.
                queue.add(stringRequest);
                queue.add(stringRequest1);
                yesResponse();
            }
        });
    }

    public void yesResponse() {
        Log.v("TIMER", "time = " + timer);
        if (timer > 10) {
            replied[0] = false;
            progrssBarForReplies.setVisibility(View.GONE);
            help_button.setVisibility(View.VISIBLE);
            mTextView.setText("Need help?\nTap the button below!");
            unique_ts[0] = "";
            replied[0] = false;
            user_name[0] = "";
            alertIfTimeout.show();
            return;
        } else {
            String uniq_ts = unique_ts[0];
            //Request a string to check for reply:
            String replyUrl = "https://slack.com/api/conversations.replies?token=" + tokenUser + "&channel=" + channel + "&ts=" + unique_ts[0] + "&pretty=1";
            StringRequest stringRequest2 = new StringRequest(Request.Method.GET, replyUrl,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            //if more than 2 minutes:
                            //Code for getting the replied message and username:
                            try {
                                replied[0] = false;
                                JSONObject replyResp = new JSONObject(response);
                                JSONArray replyMsgsArray = replyResp.getJSONArray("messages");
                                int numOfMsgs = replyMsgsArray.length();
                                JSONObject myMsgObj = replyMsgsArray.getJSONObject(numOfMsgs - 1);
                                repliedMsg[0] = myMsgObj.getString("text");//The replied msg is checked if it is a YES
                                //If it is a yes, update the replied to true and get the username
                                if (repliedMsg[0].toLowerCase().equals("yes")) {
                                    replied[0] = true;
                                    String user_id = myMsgObj.getString("user");
                                    user_name[0] = id_name_table.get(user_id);
                                    progrssBarForReplies.setVisibility(View.GONE);
                                    help_button.setVisibility(View.VISIBLE);
                                    mTextView.setText("Need help?\nTap the button below!");
                                    alertIfResponded.setTitle("Almost there!");
                                    alertIfResponded.setMessage(user_name[0] + " will be with you shortly..");
                                    alertIfResponded.show();
                                    confirmationMessagePoster(user_name[0]);
                                } else {
                                    yesResponse();
                                }
                            } catch (JSONException ignored) {
                                yesResponse();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    yesResponse();
                }
            });
            queue.add(stringRequest2);
        }
    }

    public void confirmationMessagePoster(String UserName) {
        String text = UserName + " has confirmed.";
        String postMsgURL = url + "token=" + token + "&channel=" + channel + "&text=" + text + "&pretty=1";
        // Request a string to post a message into SLACK :
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, postMsgURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.v("confirmResponse = ", response);
                        //Code for getting unique_ts of the posted message:
                        try {
                            JSONObject postMsgResp = new JSONObject(response);
                            JSONObject msgObj = postMsgResp.getJSONObject("message");
                        } catch (JSONException ignored) {
                            mTextView.setText("That didn't work3!");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mTextView.setText("That didn't work4!");
            }
        });

        queue.add(stringRequest);
    }
}
