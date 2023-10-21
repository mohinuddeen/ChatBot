package com.mohinuddeen.chatbotjv;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
  RecyclerView recyclerView;
  TextView welcomeTextView;
  EditText messageEditText;
  ImageButton sendButton;
  List<Message> messageList;
  MessageAdapter messageAdapter;
  String accessToken,refreshToken;
  String last_message_id="";
  String typedBy="0";
  String url = "http://192.168.29.238/";

  public static final MediaType JSON
      = MediaType.get("application/json; charset=utf-8");
  OkHttpClient client = new OkHttpClient();
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    messageList = new ArrayList<>();

    recyclerView = findViewById(R.id.recycler_view);
    welcomeTextView = findViewById(R.id.welcome_text);
    messageEditText = findViewById(R.id.message_edit_text);
    sendButton = findViewById(R.id.send_btn);

    messageAdapter = new MessageAdapter(messageList);
    recyclerView.setAdapter(messageAdapter);
    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
    linearLayoutManager.setStackFromEnd(true);
    recyclerView.setLayoutManager(linearLayoutManager);
    welcomeTextView.setVisibility(View.GONE);
//
    sendButton.setOnClickListener((v)->{
      typedBy="1";
      String question = messageEditText.getText().toString().trim();
      addToChat(question,Message.SENT_BY_ME,"0");
      messageEditText.setText("");
      callAPI(question);

    });
    Thread thread = new Thread(new Runnable() {
      @Override
      public void run() {
        try  {
          AuthApi();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
    thread.start();
  }
  // used chat to View
  void addToChat(String message,String sentBy,String TypedBy){
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        messageList.add(new Message(message,sentBy,TypedBy));
        messageAdapter.notifyDataSetChanged();
        recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
      }
    });
  }
  // used chat to Message
  void addResponse(String response, String Strby)
  {
    if(!typedBy.equals("0")){
          messageList.remove(messageList.size()-1);
    messageList.remove(messageList.size()-1);
    }
    if(Strby.equals("user")){
      addToChat(response, Message.SENT_BY_ME,"1");
    }else {
      addToChat(response, Message.SENT_BY_BOT,"1");
    }
  }
  //this fun is used authenticate user info call the previous chat details
  void AuthApi ()
  {
    messageList.add(new Message("Typing... ",Message.SENT_BY_BOT,"0"));
    JSONObject jsonBody = new JSONObject();
    try {
      jsonBody.put("username","riya");
      jsonBody.put("password","riya@123");
      jsonBody.put("grant_type","password");

    } catch (JSONException e) {
      e.printStackTrace();
    }
    RequestBody body = RequestBody.create(jsonBody.toString(),JSON);
    Request request = new Request.Builder()
            .url(url+"auth/token")
            .post(body)
            .build();
    try{
      Response response = client.newCall(request).execute();
    }catch (IOException e) {
      throw new RuntimeException(e);
    }
    client.newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(@NonNull Call call, @NonNull IOException e) {
        addResponse("Failed to load response due to "+e.getMessage(),"bot");
      }

      @Override
      public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
        if(response.isSuccessful()){
          JSONObject  jsonObject = null;
          try {
            jsonObject = new JSONObject(response.body().string());
            accessToken = jsonObject.getString("access_token");
            refreshToken = jsonObject.getString("refresh_token");
            callAPI("");
          } catch (JSONException e) {
            e.printStackTrace();
          }
        }else{
          addResponse("Failed to load response due to "+response.body().toString(),"bot");
        }
      }
    });
  }
  //this fun is used to get reply from the bot
  void callAPI(String question)
  {
    messageList.add(new Message("Typing... ",Message.SENT_BY_BOT,"0"));
    RequestBody formBody = new FormBody.Builder()
            .addEncoded("last_message_id",last_message_id)
            .addEncoded("message",question)
            .build();

    Request request = new Request.Builder()
        .url(url+"webhook/")
        .header("Authorization","Bearer "+accessToken)
        .post(formBody)
        .build();

    client.newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(@NonNull Call call, @NonNull IOException e) {
        addResponse("Failed to load response due to "+e.getMessage(),"bot");
      }

      @Override
      public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
        if(response.isSuccessful()){
          JSONObject  jsonObject = null;
          try {
            jsonObject = new JSONObject(response.body().string());
            JSONArray jsonArray = jsonObject.getJSONArray("messages");
            for (int i=0;i<jsonArray.length();i++) {
              last_message_id = jsonArray.getJSONObject(i).getString("id");
              String result = jsonArray.getJSONObject(i).getString("text");
              String by = jsonArray.getJSONObject(i).getString("by");
              addResponse(result.trim(), by);
            }
          } catch (JSONException e) {
            e.printStackTrace();
          }
        }else{
          addResponse("Failed to load response due to "+response.body().toString(),"bot");
        }
      }
    });
  }
}