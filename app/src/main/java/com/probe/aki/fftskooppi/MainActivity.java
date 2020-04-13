package com.probe.aki.fftskooppi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private static final int RECORDER_SAMPLERATE = 16384;

    private AudioRecord recorder = null;
    private Thread recordingThread = null;
    private boolean isRecording = false;
    private GraphView graphview;
    private EditText editStart;
    private EditText editEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        graphview = findViewById(R.id.graphview);

        if(savedInstanceState != null) {
            isRecording = savedInstanceState.getBoolean("recordon");
            graphview.getBundleData(savedInstanceState);
        }

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                1);

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setButtonHandlers();
            enableButtons(isRecording);

            editStart = (EditText) findViewById(R.id.editStart);
            editEnd = (EditText) findViewById(R.id.editEnd);
            editStart.setText(Integer.toString(graphview.getStartFrequency()*4));
            editEnd.setText(Integer.toString(graphview.getStopFrequency()*4));

            editStart.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                        int es = parseInt(editStart.getText().toString());
                        int ee = parseInt(editEnd.getText().toString()) - 100;
                        if(es <= ee) {
                            editStart.setBackgroundColor(Color.WHITE);
                            graphview.setStartFrequency(es / 4);
                        } else
                            editStart.setBackgroundColor(Color.RED);
                }
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }
            });
            editEnd.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                        int es = parseInt(editStart.getText().toString()) + 100;
                        int ee = parseInt(editEnd.getText().toString());
                        if(es <= ee && ee <= 8000) {
                            editEnd.setBackgroundColor(Color.WHITE);
                            graphview.setStopFrequency(ee / 4);
                        } else
                            editEnd.setBackgroundColor(Color.RED);
                }
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }
            });
        }
        if(isRecording)
            startRecording();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("recordon",isRecording);
        graphview.setBundleData(savedInstanceState);
    }

    private void setButtonHandlers() {
        ((Button) findViewById(R.id.btnStart)).setOnClickListener(btnClick);
        ((Button) findViewById(R.id.btnStop)).setOnClickListener(btnClick);
    }

    private void enableButton(int id, boolean isEnable) {
        ((Button) findViewById(id)).setEnabled(isEnable);
    }

    private void enableButtons(boolean isRecording) {
        enableButton(R.id.btnStart, !isRecording);
        enableButton(R.id.btnStop, isRecording);
    }

    private void startRecording() {

        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, GraphView.FFTBUFFERSIZE * 2);

        recorder.startRecording();

        isRecording = true;

        recordingThread = new Thread(new Runnable() {

            public void run() {
                writeAudioDataToFile();
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    protected void onStop () {
        super.onStop();
        stopRecording();
    }

    private void writeAudioDataToFile() {
        short sData[] = new short[GraphView.FFTBUFFERSIZE];

        while (isRecording) {
            recorder.read(sData, 0, GraphView.FFTBUFFERSIZE);
            graphview.setFft(sData);
        }
    }

    private void stopRecording() {
        // stops the recording activity
        if (null != recorder && isRecording) {
            isRecording = false;

            recorder.stop();
            recorder.release();

            recorder = null;
            recordingThread = null;
        }
    }

    private View.OnClickListener btnClick = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnStart: {
                    enableButtons(true);
                    startRecording();
                    break;
                }
                case R.id.btnStop: {
                    enableButtons(false);
                    stopRecording();
                    break;
                }
            }
        }
    };

    // onClick of backbutton finishes the activity.
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    private int parseInt(String in){
        return in.isEmpty() ? 0 : Integer.parseInt(in);
    }
}
