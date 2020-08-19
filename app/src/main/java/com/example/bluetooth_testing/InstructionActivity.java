package com.example.bluetooth_testing;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

public class InstructionActivity extends Activity
{

    Button easyBtn, mediumBtn, FreePlaybtn, PianoGamebtn;
    TextView instructionText;

    String  gifImagePath;
    WebView webView;

    //GifImageView fingerGif;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruction);

        easyBtn = (Button) findViewById(R.id.btnEasy);
        mediumBtn = (Button) findViewById(R.id.btnMedium);
        instructionText = (TextView) findViewById(R.id.instruction);
        FreePlaybtn = findViewById(R.id.Piano_int);
        PianoGamebtn = findViewById(R.id.PianoGame_int);
        //Default instruction
        instructionText.setText("Bending of fingers for basic exercises");

        //References - https://stackoverflow.com/posts/34776689/revisions
        webView = (WebView) findViewById(R.id.imageWebView);
        gifImagePath  = "<body><center><img src = \"file:///android_res/drawable/easy.gif\"/></center></body>";
        webView.loadDataWithBaseURL("file:///android_asset/",gifImagePath,"text/html","UTF-8",null);

        //Easy Mode Instructions
        easyBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                gifImagePath  = "<body><center><img src = \"file:///android_res/drawable/easy.gif\"/></center></body>";
                webView.loadDataWithBaseURL("file:///android_asset/",gifImagePath,"text/html","UTF-8",null);
                instructionText.setText("Bend your finger as shown on the image above, 1-by-1");
            }
        });

        //Medium mode Instructions
        mediumBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                gifImagePath  = "<body><center><img src = \"file:///android_res/drawable/medium.gif\"/></center></body>";
                webView.loadDataWithBaseURL("file:///android_asset/",gifImagePath,"text/html","UTF-8",null);
                instructionText.setText("Bend your finger as shown on the image above, simultaneously");
            }
        });

        //Piano Game mode Instructions
        FreePlaybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gifImagePath  = "<body><center><img src = \"file:///android_res/drawable/piano.gif\"/></center></body>";
                webView.loadDataWithBaseURL("file:///android_asset/",gifImagePath,"text/html","UTF-8",null);
                instructionText.setText("Play the piano however you like");
            }
        });

        //Marry Had a Little Lamb Mode Instructions
        PianoGamebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                gifImagePath  = "<body><center><img src = \"file:///android_res/drawable/pianotutorial.gif\"/></center></body>";
                webView.loadDataWithBaseURL("file:///android_asset/",gifImagePath,"text/html","UTF-8",null);
                instructionText.setText("Follow the highlighted piano bars and bend your respective finger to play the game");
            }
        });
    }
}
