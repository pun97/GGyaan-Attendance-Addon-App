package com.example.pun.attendanceapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
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

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

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

    EditText urlText;
    EditText userText;
    EditText passwText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button login = (Button) findViewById(R.id.button);
        urlText = (EditText) findViewById(R.id.url);
        userText = (EditText) findViewById(R.id.uname);
        passwText = (EditText) findViewById(R.id.pword);

        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.INTERNET},1);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //String url = "http://10.50.46.232:5000/mobilelogin";
                //String uname="Admin";
                //String pword="admin";
                String url ="http://"+urlText.getText().toString()+":5000";
                String uname = userText.getText().toString();
                String pword = passwText.getText().toString();
                Intent intent = new Intent(MainActivity.this, BarcodeCaptureActivity.class);
                startActivityForResult(intent, 1);
                /*
                if (hasPermissions(MainActivity.this, new String[]{Manifest.permission.INTERNET})) {
                    new SendLoginCredentials(url+"/attendance", uname, pword).execute();
                }*/


            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    Toast.makeText(MainActivity.this,barcode.displayValue,Toast.LENGTH_SHORT).show();
                    if (hasPermissions(MainActivity.this, new String[]{Manifest.permission.INTERNET})) {
                        String url ="http://"+urlText.getText().toString()+":5000";
                        String uname = userText.getText().toString();
                        String pword = passwText.getText().toString();

                        new SendLoginCredentials(url+"/attendance",uname,pword,barcode.displayValue).execute();

                    }
                } else Toast.makeText(MainActivity.this,"NO BAR CODE",Toast.LENGTH_SHORT).show();
            } else Log.e("APP ERROR:", String.format(getString(R.string.barcode_error_format),
                    CommonStatusCodes.getStatusCodeString(resultCode)));
        } else super.onActivityResult(requestCode, resultCode, data);
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

        String qrdata;

        SendLoginCredentials(String url, String username, String pword,String qrdata){
            this.url = url;
            this.uname = username;
            this.password = pword;
            this.qrdata=qrdata;
        }

        @Override
        protected String doInBackground(String... strings) {
            try{
                URL url = new URL(this.url);

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("username",uname);//it was rno before change in flask server
                postDataParams.put("password",password);
                postDataParams.put("qrdata",qrdata);
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
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(), result,
                    Toast.LENGTH_LONG).show();
            super.onPostExecute(result);

            //Intent intent = new Intent(getApplicationContext(), BarcodeCaptureActivity.class);
            //startActivityForResult(intent, 1);
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
