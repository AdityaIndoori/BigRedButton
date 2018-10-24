package com.example.indoo.mixslack_1;

import android.content.DialogInterface;
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

    private static final String token = "xoxp-456534988487-457103289222-455926624885-6a89615d41d90936a0a0e0adfaa3c7a8";
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
    String postMsg_url = url + "token=" + token + "&channel=" + channel + "&text=" + text + "&as_user=true&pretty=1";
    private Button help_button;
    private AlertDialog.Builder alertIfResponded, alertIfTimeout;
    private ProgressBar progrssBarForReplies;
    private boolean clickedRespondAlert, clickedTimeoutAlert, showProgessDialog;
    private TextView mTextView;
    private RequestQueue queue;
    private int timer = 0;

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
            }
        });

        help_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unique_ts[0] = "";
                replied[0] = false;
                //Show the progress bar here and hide the button

                // Instantiate the RequestQueue.
                queue = SingletonRequestQueue.getInstance(MainActivity.this).getmRequestQueue();

                // Request a string response for NAMES URL.
                StringRequest stringRequest = new StringRequest(Request.Method.GET, name_url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                //Code for names to hashTable:
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
                StringRequest stringRequest1 = new StringRequest(Request.Method.GET, postMsg_url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                //Code for getting unique_ts of the posted message:
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

                // Add the NAMES request to the RequestQueue.
                queue.add(stringRequest);
                queue.add(stringRequest1);
                if (!replied[0]) {
                    help_button.setVisibility(View.GONE);
                    progrssBarForReplies.setVisibility(View.VISIBLE);
                }
                yesResponse();
            }
        });
    }

    public void yesResponse() {
        Log.v("yesResponse", "Inside yesResponse");
        {
            String uniq_ts = unique_ts[0];
            //Request a string to check for reply:
            String replyUrl = "https://slack.com/api/conversations.replies?token=" + token + "&channel=" + channel + "&ts=" + unique_ts[0] + "&pretty=1";
            Log.v("yesResponse", "Creating string request");
            StringRequest stringRequest2 = new StringRequest(Request.Method.GET, replyUrl,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            //Code for getting the replied message and username:
                            try {
                                Log.v("yesResponse", "Trying to get JSON Data");
                                replied[0] = false;
                                JSONObject replyResp = new JSONObject(response);
                                JSONArray replyMsgsArray = replyResp.getJSONArray("messages");
                                int numOfMsgs = replyMsgsArray.length();
                                JSONObject myMsgObj = replyMsgsArray.getJSONObject(numOfMsgs - 1);
                                repliedMsg[0] = myMsgObj.getString("text");//The replied msg is checked if it is a YES
                                //If it is a yes, update the replied to true and get the username
                                if (repliedMsg[0].toLowerCase().equals("yes")) {
                                    Log.v("yesResponse", " The replied string is yes");
                                    replied[0] = true;
                                    String user_id = myMsgObj.getString("user");
                                    user_name[0] = id_name_table.get(user_id);
                                    progrssBarForReplies.setVisibility(View.GONE);
                                    help_button.setVisibility(View.VISIBLE);
                                    alertIfResponded.setTitle("Success");
                                    alertIfResponded.setMessage(user_name[0] + " is coming. Please wait");
                                    alertIfResponded.show();
                                } else {
                                    Log.v("yesResponse", "The replied string isn't yes - so calling yesResponse again");
                                    yesResponse();
                                }
                            } catch (JSONException ignored) {
                                Log.v("yesResponse", "JSON Parsing exception");
                                yesResponse();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.v("yesResponse", "Inside yesResponse Error Response");
                    yesResponse();
                }
            });
            Log.v("yesResponse", "Adding stringRequest2 into the queue");
            queue.add(stringRequest2);
        }
    }
    /**
     *
     final OkHttpClient client = new OkHttpClient();
     final String[] responseString = {null};
     final Request request = new Request.Builder().url(url).build();

     client.newCall(request).enqueue(new Callback() {
    @Override public void onFailure(Call call, IOException e) {
    responseString[0] = null;
    }

    @Override public void onResponse(Call call, Response response) throws IOException {
    responseString[0] = response.body().string();
    }
    });
     if (responseString[0]!=null){
     try {
     return new JSONObject(responseString[0]);
     } catch (JSONException e) {
     return  null;
     }
     }
     return null;
     */
}


/**
 * alertIfResponded.setMessage("Someone is coming!");
 * alertIfResponded.setCancelable(false);
 * clickedRespondAlert = false;
 * alertIfResponded.setNeutralButton("Okay!", new DialogInterface.OnClickListener() {
 *
 * @Override public void onClick(DialogInterface dialog, int which) {
 * clickedRespondAlert = true;
 * }
 * });
 * if (!clickedRespondAlert)
 * alertIfResponded.show();
 * <p>
 * showProgessDialog = !showProgessDialog;
 * if (showProgessDialog) {
 * progrssBarForReplies.setVisibility(View.VISIBLE);
 * help_button.setVisibility(View.GONE);
 * } else {
 * progrssBarForReplies.setVisibility(View.GONE);
 * }
 * <p>
 * try {
 * name_id_array = namesJSON.getJSONArray("members");
 * Hashtable<String ,String> id_name_table = new Hashtable<String, String>();
 * JSONObject arrayElem = new JSONObject();
 * for (int i =0; i<name_id_array.length();i++){
 * arrayElem = (JSONObject) name_id_array.get(i);
 * if (id_name_table.containsKey("real_name")){
 * id_name_table.put(arrayElem.getString("id"),arrayElem.getString("real_name"));
 * }
 * else
 * id_name_table.put(arrayElem.getString("id"),"Someone");
 * }
 * <p>
 * //Code to post the message into slack and getthe message's unique_ts
 * JSONObject postMsgResp = httpToString(url +
 * "token=" + token +
 * "&channel=" + channel +
 * "&text=" + text +
 * "&as_user=true&pretty=1");
 * <p>
 * if (postMsgResp!=null){
 * JSONObject msgObj = postMsgResp.getJSONObject("messages");
 * String unique_ts = msgObj.getString("ts");
 * <p>
 * //Code to keep checking for the replies and timer part
 * boolean replied = false;
 * String repliedMsg = null;
 * String user_name = null;
 * int timer = 0;
 * while (!replied){
 * String replyUrl = "https://slack.com/api/conversations.replies?token="+token+"&channel="+channel+"&ts="+unique_ts+"&pretty=1";
 * JSONObject replyResp = httpToString(replyUrl);
 * if (replyResp!=null){
 * if (timer >= 120){
 * //hide the progress bar, show the button, alert message (No one responded, try again) and break
 * }
 * JSONArray replyMsgsArray = replyResp.getJSONArray("messages");
 * int numOfMsgs = replyMsgsArray.length();
 * JSONObject myMsgObj = replyMsgsArray.getJSONObject(numOfMsgs-1);
 * repliedMsg = myMsgObj.getString("text");//The replied msg is checked if it is a YES
 * if (repliedMsg.toLowerCase().equals("yes")){
 * //If someone replied yes, get their username, and post the alert message
 * String user_id = myMsgObj.getString("user");
 * user_name = id_name_table.get(user_id);
 * alertIfResponded.setMessage(user_name + "is coming to you. Please wait..");
 * alertIfResponded.show();
 * }
 * else {
 * try{
 * Thread.sleep(1000);
 * }catch(InterruptedException e){
 * }
 * timer++;
 * }
 * }
 * else{
 * //i.e the reply response was null, so, hide the progress bar, show the button, toast message(Failed to ask, try again) and break
 * }
 * }
 * }
 * else {
 * //the json is null, so Hide the progress bar and show the button and toast message (Failed to ask, try again)
 * }
 * } catch (JSONException e) {//No Array was formed
 * name_id_array = null;
 * //Hide the progress bar and show the button here:
 * }
 * <p>
 * showProgessDialog = !showProgessDialog;
 * if (showProgessDialog) {
 * progrssBarForReplies.setVisibility(View.VISIBLE);
 * help_button.setVisibility(View.GONE);
 * } else {
 * progrssBarForReplies.setVisibility(View.GONE);
 * }
 * <p>
 * try {
 * name_id_array = namesJSON.getJSONArray("members");
 * Hashtable<String ,String> id_name_table = new Hashtable<String, String>();
 * JSONObject arrayElem = new JSONObject();
 * for (int i =0; i<name_id_array.length();i++){
 * arrayElem = (JSONObject) name_id_array.get(i);
 * if (id_name_table.containsKey("real_name")){
 * id_name_table.put(arrayElem.getString("id"),arrayElem.getString("real_name"));
 * }
 * else
 * id_name_table.put(arrayElem.getString("id"),"Someone");
 * }
 * <p>
 * //Code to post the message into slack and getthe message's unique_ts
 * JSONObject postMsgResp = httpToString(url +
 * "token=" + token +
 * "&channel=" + channel +
 * "&text=" + text +
 * "&as_user=true&pretty=1");
 * <p>
 * if (postMsgResp!=null){
 * JSONObject msgObj = postMsgResp.getJSONObject("messages");
 * String unique_ts = msgObj.getString("ts");
 * <p>
 * //Code to keep checking for the replies and timer part
 * boolean replied = false;
 * String repliedMsg = null;
 * String user_name = null;
 * int timer = 0;
 * while (!replied){
 * String replyUrl = "https://slack.com/api/conversations.replies?token="+token+"&channel="+channel+"&ts="+unique_ts+"&pretty=1";
 * JSONObject replyResp = httpToString(replyUrl);
 * if (replyResp!=null){
 * if (timer >= 120){
 * //hide the progress bar, show the button, alert message (No one responded, try again) and break
 * }
 * JSONArray replyMsgsArray = replyResp.getJSONArray("messages");
 * int numOfMsgs = replyMsgsArray.length();
 * JSONObject myMsgObj = replyMsgsArray.getJSONObject(numOfMsgs-1);
 * repliedMsg = myMsgObj.getString("text");//The replied msg is checked if it is a YES
 * if (repliedMsg.toLowerCase().equals("yes")){
 * //If someone replied yes, get their username, and post the alert message
 * String user_id = myMsgObj.getString("user");
 * user_name = id_name_table.get(user_id);
 * alertIfResponded.setMessage(user_name + "is coming to you. Please wait..");
 * alertIfResponded.show();
 * }
 * else {
 * try{
 * Thread.sleep(1000);
 * }catch(InterruptedException e){
 * }
 * timer++;
 * }
 * }
 * else{
 * //i.e the reply response was null, so, hide the progress bar, show the button, toast message(Failed to ask, try again) and break
 * }
 * }
 * }
 * else {
 * //the json is null, so Hide the progress bar and show the button and toast message (Failed to ask, try again)
 * }
 * } catch (JSONException e) {//No Array was formed
 * name_id_array = null;
 * //Hide the progress bar and show the button here:
 * }
 */


/**
 showProgessDialog = !showProgessDialog;
 if (showProgessDialog) {
 progrssBarForReplies.setVisibility(View.VISIBLE);
 help_button.setVisibility(View.GONE);
 } else {
 progrssBarForReplies.setVisibility(View.GONE);
 }
 */


/**
 * try {
 *                         name_id_array = namesJSON.getJSONArray("members");
 *                         Hashtable<String ,String> id_name_table = new Hashtable<String, String>();
 *                         JSONObject arrayElem = new JSONObject();
 *                         for (int i =0; i<name_id_array.length();i++){
 *                             arrayElem = (JSONObject) name_id_array.get(i);
 *                             if (id_name_table.containsKey("real_name")){
 *                                 id_name_table.put(arrayElem.getString("id"),arrayElem.getString("real_name"));
 *                             }
 *                             else
 *                                 id_name_table.put(arrayElem.getString("id"),"Someone");
 *                         }
 *
 *                         //Code to post the message into slack and getthe message's unique_ts
 *                         JSONObject postMsgResp = httpToString(url +
 *                                 "token=" + token +
 *                                 "&channel=" + channel +
 *                                 "&text=" + text +
 *                                 "&as_user=true&pretty=1");
 *
 *                         if (postMsgResp!=null){
 *                             JSONObject msgObj = postMsgResp.getJSONObject("messages");
 *                             String unique_ts = msgObj.getString("ts");
 *
 *                             //Code to keep checking for the replies and timer part
 *                             boolean replied = false;
 *                             String repliedMsg = null;
 *                             String user_name = null;
 *                             int timer = 0;
 *                             while (!replied){
 *                                 String replyUrl = "https://slack.com/api/conversations.replies?token="+token+"&channel="+channel+"&ts="+unique_ts+"&pretty=1";
 *                                 JSONObject replyResp = httpToString(replyUrl);
 *                                 if (replyResp!=null){
 *                                     if (timer >= 120){
 *                                         //hide the progress bar, show the button, alert message (No one responded, try again) and break
 *                                     }
 *                                     JSONArray replyMsgsArray = replyResp.getJSONArray("messages");
 *                                     int numOfMsgs = replyMsgsArray.length();
 *                                     JSONObject myMsgObj = replyMsgsArray.getJSONObject(numOfMsgs-1);
 *                                     repliedMsg = myMsgObj.getString("text");//The replied msg is checked if it is a YES
 *                                     if (repliedMsg.toLowerCase().equals("yes")){
 *                                         //If someone replied yes, get their username, and post the alert message
 *                                         String user_id = myMsgObj.getString("user");
 *                                         user_name = id_name_table.get(user_id);
 *                                         alertIfResponded.setMessage(user_name + "is coming to you. Please wait..");
 *                                         alertIfResponded.show();
 *                                     }
 *                                     else {
 *                                         try{
 *                                             Thread.sleep(1000);
 *                                         }catch(InterruptedException e){
 *                                         }
 *                                         timer++;
 *                                     }
 *                                 }
 *                                 else{
 *                                     //i.e the reply response was null, so, hide the progress bar, show the button, toast message(Failed to ask, try again) and break
 *                                 }
 *                             }
 *                         }
 *                         else {
 *                             //the json is null, so Hide the progress bar and show the button and toast message (Failed to ask, try again)
 *                         }
 *                     } catch (JSONException e) {//No Array was formed
 *                         name_id_array = null;
 *                         //Hide the progress bar and show the button here:
 *                     }
 */