## Overview
    This Android application is a simple chatbot that interacts with a backend API to provide responses to user input. The app allows users to send messages and receive responses from a chatbot.

## Features
    Chat Interface: Users can type messages in the app and receive responses from the chatbot.
    Prerequisites
    Android Studio
    Internet connection for API communication

## Usage
    Launch the app on your Android device.
    Type a message in the input field.
    Press the send button to send the message to the chatbot.
    View responses from the chatbot in the chat interface.
    
## Dependencies
    OkHttp: Used for making HTTP requests.
    
## Configuration
    Update the url variable in MainActivity.java with the correct backend API endpoint.
    String url = "http://your-backend-api-url/";

## Function Details
#### onCreate
    Initializes UI components and sets up the RecyclerView for displaying messages.
    Configures the send button click listener to send messages to the chatbot.
#### addToChat
    Adds a message to the chat interface.
    Called when a message is sent or received.
#### addResponse
    Adds a response to the chat interface.
    Handles the removal of typing indicators.
#### AuthApi
    Authenticates the user with the backend API using a username and password.
    Retrieves access and refresh tokens for subsequent API calls.
    void AuthApi ()
    {
        messageList.add(new Message("Typing... ",Message.SENT_BY_BOT,"0"));
        JSONObject jsonBody = new JSONObject();
        try {
          jsonBody.put("username","username");
          jsonBody.put("password","password");
          jsonBody.put("grant_type","grant_type");
    
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
  
#### callAPI
    Makes an API call to the chatbot backend, sending user input and receiving responses.
    Handles the display of typing indicators during API calls.
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

## API Authentication
    The app authenticates with the backend API using a username and password. Update the authentication details in the AuthApi method in MainActivity.java.

## Notes
This is a basic implementation and can be extended to include additional features and improvements.
