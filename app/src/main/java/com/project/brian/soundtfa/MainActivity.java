package com.project.brian.soundtfa;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.ProgressBar;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;


public class MainActivity extends AppCompatActivity implements SoundThread.ToneCallback {

    TextView text;
    View play_tone;
    ProgressBar progress;
    String full_hash;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text = (TextView) findViewById(R.id.text);
        if (savedInstanceState == null) {
            text.setText("Hello");
            String hash = "";
            String cut_hash = "";
            hash = text.getText().toString();
            full_hash = md5(hash);
            cut_hash = full_hash.substring(full_hash.length()-5);
            text.setText(cut_hash);
        } else {
            full_hash = savedInstanceState.getString("MyHash");
            String cut_hash = "";
            cut_hash = full_hash.substring(full_hash.length()-5);
            text.setText(cut_hash);
        }

        play_tone = findViewById(R.id.play_tone);
        progress = (ProgressBar) findViewById(R.id.progress);

        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(10000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String cut_hash = "";
                                full_hash = md5(full_hash);
                                cut_hash = full_hash.substring(full_hash.length()-5);
                                text.setText(cut_hash);
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        t.start();
        play_tone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = text.getText().toString();
                byte[] payload = new byte[0];
                payload = message.getBytes(Charset.forName("UTF-8"));

                ByteArrayInputStream bis = new ByteArrayInputStream(payload);

                play_tone.setEnabled(false);
                SoundThread.SoundIterator tone = new BitSound(bis, 7);
                new SoundThread(tone, MainActivity.this).start();
            }
        });
    }

    @Override
    public void onProgress(int current, int total) {
        progress.setMax(total);
        progress.setProgress(current);
    }

    @Override
    public void onDone() {
        play_tone.setEnabled(true);
    }

    public static final String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        savedInstanceState.putString("MyHash", full_hash);
        // etc.
    }
}

