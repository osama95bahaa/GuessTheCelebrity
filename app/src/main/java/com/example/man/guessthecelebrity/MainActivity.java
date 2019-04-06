package com.example.man.guessthecelebrity;

import android.content.SyncStatusObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> celebsURLS = new ArrayList<>();
    ArrayList<String> celebNAmes = new ArrayList<>();
    int chosenCeleb;
    ImageView imgView;
    int locationOfCorrectAnswer = 0;
    int incorrectLocationOfAnswer = 0;
    String [] answers = new String[4];
    Button btn0;
    Button btn1;
    Button btn2;
    Button btn3;

    public class ImageDownloader extends AsyncTask<String,Void,Bitmap>{


        @Override
        protected Bitmap doInBackground(String... strings) {

            try {

                URL url =  new URL(strings[0]);
                URLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(inputStream);
                return myBitmap;

            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }
    }


    public class DownloadTask extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... strings) {

            String result = "";
            URL url;
            URLConnection urlConnection = null;

            try {
                url = new URL(strings[0]);
                urlConnection = url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                int data = reader.read();

                while (data != -1){
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                return result;
            }
            catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }
    }

    public void answer(View view){

        if(view.getTag().toString().equals(Integer.toString(locationOfCorrectAnswer))){
            Toast.makeText(getApplicationContext(),"Correct!" ,Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(getApplicationContext(),"Wrong! its " + celebNAmes.get(chosenCeleb) ,Toast.LENGTH_SHORT).show();

        }
        createNewQuestion();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgView = (ImageView) findViewById(R.id.img);
        btn0 = (Button) findViewById(R.id.button);
        btn1 = (Button) findViewById(R.id.button1);
        btn2 = (Button) findViewById(R.id.button2);
        btn3 = (Button) findViewById(R.id.button3);

        DownloadTask task = new DownloadTask();
        String result = "";

        try {
            result = task.execute("http://www.posh24.se/kandisar").get();
//            Log.i("Content of URL" , result);

            String[] resultSplit = result.split("<div class=\"col-xs-12 col-sm-6 col-md-4\">");

            Pattern p = Pattern.compile("<img src=\"(.*?)\"");
            Matcher m = p.matcher(resultSplit[0]);

            while (m.find()) {
                celebsURLS.add(m.group(1));
//                System.out.println(m.group(1));
            }

            p = Pattern.compile("alt=\"(.*?)\"/>");
            m = p.matcher(resultSplit[0]);

            while (m.find()) {
                celebNAmes.add(m.group(1));
//                System.out.println(m.group(1));
            }

            createNewQuestion();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void createNewQuestion(){
        Random rand = new Random();
        chosenCeleb = rand.nextInt(celebsURLS.size());
        String firstPhoto = celebsURLS.get(chosenCeleb);
        Log.i("Randomly chosen img", firstPhoto);


        ImageDownloader imgTask = new ImageDownloader();
        Bitmap celebImg;

        try {
            celebImg = imgTask.execute(firstPhoto).get();
            imgView.setImageBitmap(celebImg);

            locationOfCorrectAnswer = rand.nextInt(4);

            for(int i=0;i<4;i++){
                if(i == locationOfCorrectAnswer){
                    answers[i] = celebNAmes.get(chosenCeleb);
                }
                else{
                    incorrectLocationOfAnswer = rand.nextInt(celebsURLS.size());

                    while(incorrectLocationOfAnswer == locationOfCorrectAnswer){

                        incorrectLocationOfAnswer = rand.nextInt(celebsURLS.size());
                    }
                    answers[i] = celebNAmes.get(incorrectLocationOfAnswer);
                }
            }

            btn0.setText(answers[0]);
            btn1.setText(answers[1]);
            btn2.setText(answers[2]);
            btn3.setText(answers[3]);

        } catch (Exception e) {

            e.printStackTrace();
        }

    }
}
