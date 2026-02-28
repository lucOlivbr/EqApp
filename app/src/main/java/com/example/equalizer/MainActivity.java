package com.example.equalizer;

import android.media.AudioManager;
import android.media.audiofx.Equalizer;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Equalizer equalizer;
    private LinearLayout eqContainer;
    private List<SeekBar> seekBars = new ArrayList<>();

    private static final short[][] PRESETS = {
        {0, 0, 0, 0, 0},
        {600, 400, -400, 200, 600},
        {800, 600, -200, 500, 700},
        {-200, -100, 400, 600, 200},
        {400, 200, 0, 200, 400},
        {0, -200, -400, -200, 0}
    };

    private static final String[] PRESET_NAMES = {
        "Flat", "Rock", "Bass Boost", "Treble Boost", "Pop", "Classical"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        eqContainer = findViewById(R.id.eq_container);
        Spinner presetSpinner = findViewById(R.id.preset_spinner);
        Button resetBtn = findViewById(R.id.btn_reset);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, PRESET_NAMES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        presetSpinner.setAdapter(adapter);
        presetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, android.view.View v, int pos, long id) {
                applyPreset(pos);
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
        resetBtn.setOnClickListener(v -> { presetSpinner.setSelection(0); applyPreset(0); });
        initEqualizer();
    }

    private void initEqualizer() {
        try {
            AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
            equalizer = new Equalizer(0, am.generateAudioSessionId());
            equalizer.setEnabled(true);
            short numBands = equalizer.getNumberOfBands();
            short[] range = equalizer.getBandLevelRange();
            short min = range[0], max = range[1];
            eqContainer.removeAllViews();
            seekBars.clear();
            for (short i = 0; i < numBands; i++) {
                int freq = equalizer.getCenterFreq(i) / 1000;
                String label = freq >= 1000 ? (freq/1000) + "kHz" : freq + "Hz";
                LinearLayout col = new LinearLayout(this);
                col.setOrientation(LinearLayout.VERTICAL);
                col.setPadding(12, 8, 12, 8);
                TextView freqTv = new TextView(this);
                freqTv.setText(label);
                freqTv.setTextColor(0xFFFFFFFF);
                freqTv.setTextSize(11);
                freqTv.setGravity(android.view.Gravity.CENTER);
                SeekBar sb = new SeekBar(this);
                sb.setRotation(270f);
                sb.setMax(max - min);
                sb.setProgress(equalizer.getBandLevel(i) - min);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(180, 150);
                sb.setLayoutParams(params);
                TextView dbTv = new TextView(this);
                dbTv.setText("0dB");
                dbTv.setTextColor(0xFF90CAF9);
                dbTv.setTextSize(11);
                dbTv.setGravity(android.view.Gravity.CENTER);
                final short band = i;
                final short minF = min;
                sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar s, int progress, boolean fromUser) {
                        short level = (short)(progress + minF);
                        if (equalizer != null) equalizer.setBandLevel(band, level);
                        dbTv.setText(String.format("%+.0fdB", level / 100f));
                    }
                    @Override public void onStartTrackingTouch(SeekBar s) {}
                    @Override public void onStopTrackingTouch(SeekBar s) {}
                });
                col.addView(freqTv);
                col.addView(sb);
                col.addView(dbTv);
                eqContainer.addView(col);
                seekBars.add(sb);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void applyPreset(int index) {
        if (equalizer == null || seekBars.isEmpty()) return;
        short[] gains = PRESETS[index];
        short min = equalizer.getBandLevelRange()[0];
        for (int i = 0; i < seekBars.size() && i < gains.length; i++) {
            seekBars.get(i).setProgress(gains[i] - min);
            equalizer.setBandLevel((short) i, gains[i]);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (equalizer != null) equalizer.release();
    }
}
