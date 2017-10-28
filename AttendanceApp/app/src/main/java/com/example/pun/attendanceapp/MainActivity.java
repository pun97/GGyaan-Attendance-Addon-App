package com.example.pun.attendanceapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.Permission;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button login = (Button) findViewById(R.id.button);
        final EditText urlText = (EditText) findViewById(R.id.url);
        final EditText userText = (EditText) findViewById(R.id.uname);
        final EditText passwText = (EditText) findViewById(R.id.pword);

        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.INTERNET},1);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //String url = "http://10.50.46.232:5000/mobilelogin";
                //String uname="Admin";
                //String pword="admin";

                String url = urlText.getText().toString();
                String uname = userText.getText().toString();
                String pword = passwText.getText().toString();

                if (hasPermissions(MainActivity.this, new String[]{Manifest.permission.INTERNET})) {
                    new SendLoginCredentials(url, uname, pword).execute();
                }


            }
        });
        }


    public static boolean hasPermissions(Context context, String[] permissions)
    {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null)
        {
            for (String permission : permissions)
            {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
                {
                    return false;
                }
            }
        }
        return true;
    }

    public class SendLoginCredentials extends AsyncTask<String,Void,String> {
        String url;
        String uname;
        String password;

        SendLoginCredentials(String url, String username, String pword){
            this.url = url;
            this.uname = username;
            this.password = pword;
        }
        @Override
        protected String doInBackground(String... strings) {
            try{
                URL url = new URL(this.url);

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("username",uname);//it was rno before change in flask server
                postDataParams.put("password",password);

                Log.d("ATTENDACE:",postDataParams.toString());

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout(15000);
                connection.setConnectTimeout(15000);
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);

                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();

                int responseCode=connection.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(
                                    connection.getInputStream()));
                    StringBuffer sb = new StringBuffer("");
                    String line = "";

                    while ((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }

                    in.close();
                    return sb.toString();
                }
                else{
                    return new String("False : "+responseCode);
                }
            }
            catch (Exception e){
                return new String("Exception: "+e.getMessage());
            }
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(), result,
                    Toast.LENGTH_LONG).show();
            super.onPostExecute(result);
        }
    }
    public String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while(itr.hasNext()){

            String key= itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }
        return result.toString();
    }
}
