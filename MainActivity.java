package com.example.mysmartcallrecorder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.text.DateFormat;
import java.util.*;

public class MainActivity extends Activity {
    private LinearLayout list;
    private final ArrayList<Recording> recordings = new ArrayList<>();
    private MediaPlayer player;

    static class Recording {
        String file, contact, phone, date;
        Recording(String f, String c, String p, String d) { file=f; contact=c; phone=p; date=d; }
        JSONObject json() throws Exception {
            JSONObject o = new JSONObject();
            o.put("file", file); o.put("contact", contact); o.put("phone", phone); o.put("date", date);
            return o;
        }
    }

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        load();
        buildUi();
        handleShare(getIntent());
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleShare(intent);
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(20), dp(24), dp(20), dp(24));
        page.setBackgroundColor(0xFFF5F7F6);
        scroll.addView(page);

        TextView title = new TextView(this);
        title.setText("My Smart Call Recorder");
        title.setTextSize(24); title.setTextColor(0xFF124D38); title.setTypeface(null, 1);
        page.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Your shared call recordings, together in one place");
        subtitle.setTextSize(14); subtitle.setTextColor(0xFF66736D);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, -2);
        sp.bottomMargin = dp(18); page.addView(subtitle, sp);

        Button importBtn = new Button(this);
        importBtn.setText("Import audio file");
        importBtn.setOnClickListener(v -> {
            Intent pick = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            pick.setType("audio/*"); pick.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(pick, 21);
        });
        page.addView(importBtn);

        TextView heading = new TextView(this);
        heading.setText("Call recordings");
        heading.setTextSize(19); heading.setTypeface(null, 1); heading.setTextColor(0xFF1E3028);
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(-1, -2);
        hp.topMargin = dp(22); hp.bottomMargin = dp(10); page.addView(heading, hp);

        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        page.addView(list);
        setContentView(scroll);
        renderList();
    }

    private void handleShare(Intent intent) {
        if (intent == null || !Intent.ACTION_SEND.equals(intent.getAction())) return;
        Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (uri == null) {
            Toast.makeText(this, "No audio file was received. Try Share from the recording itself.", Toast.LENGTH_LONG).show();
            return;
        }
        importUri(uri);
    }

    private void importUri(Uri uri) {
        try {
            String original = queryName(uri);
            String safe = "call_" + System.currentTimeMillis() + extension(original);
            File dest = new File(getFilesDir(), safe);
            try (InputStream in = getContentResolver().openInputStream(uri);
                 OutputStream out = new FileOutputStream(dest)) {
                if (in == null) throw new IOException("Cannot open selected audio");
                byte[] buf = new byte[8192]; int n;
                while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            }
            showDetailsDialog(safe);
        } catch (Exception e) {
            Toast.makeText(this, "Import failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String queryName(Uri uri) {
        try (Cursor c = getContentResolver().query(uri, null, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                int i = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (i >= 0) return c.getString(i);
            }
        } catch (Exception ignored) {}
        return "recording.m4a";
    }

    private String extension(String name) {
        int i = name.lastIndexOf('.');
        return i >= 0 ? name.substring(i) : ".audio";
    }

    private void showDetailsDialog(String filename) {
        LinearLayout box = new LinearLayout(this);
        box.setPadding(dp(20), dp(8), dp(20), 0);
        box.setOrientation(LinearLayout.VERTICAL);
        EditText contact = new EditText(this); contact.setHint("Contact name (e.g. Ali)");
        EditText phone = new EditText(this); phone.setHint("Phone number (optional)");
        box.addView(contact); box.addView(phone);
        new AlertDialog.Builder(this).setTitle("Recording imported")
            .setMessage("Add the caller details so you can find this recording later.")
            .setView(box)
            .setPositiveButton("Save", (d,w) -> {
                recordings.add(0, new Recording(filename,
                    contact.getText().toString().trim().isEmpty() ? "Unknown contact" : contact.getText().toString().trim(),
                    phone.getText().toString().trim(),
                    DateFormat.getDateTimeInstance().format(new Date())));
                save(); renderList();
                Toast.makeText(this, "Recording saved in this app", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", (d,w) -> { new File(getFilesDir(), filename).delete(); })
            .show();
    }

    private void renderList() {
        if (list == null) return;
        list.removeAllViews();
        if (recordings.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No recordings yet.\nShare a call recording from Google Phone, or tap Import audio file.");
            empty.setTextSize(15); empty.setTextColor(0xFF66736D); empty.setPadding(dp(14), dp(18), dp(14), dp(18));
            list.addView(empty); return;
        }
        for (int i=0; i<recordings.size(); i++) {
            final int index=i;
            Recording r=recordings.get(i);
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(14), dp(12), dp(14), dp(12));
            card.setBackgroundColor(0xFFFFFFFF);
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2);
            cp.bottomMargin=dp(10); list.addView(card, cp);

            TextView name = new TextView(this);
            name.setText(r.contact); name.setTextSize(17); name.setTypeface(null,1); name.setTextColor(0xFF173D2D);
            card.addView(name);
            TextView meta = new TextView(this);
            meta.setText((r.phone.isEmpty() ? "Number not added" : r.phone) + "  •  " + r.date);
            meta.setTextSize(12); meta.setTextColor(0xFF69756F); card.addView(meta);

            LinearLayout actions = new LinearLayout(this);
            actions.setGravity(Gravity.END);
            Button play = new Button(this); play.setText("Play");
            play.setOnClickListener(v -> playRecording(r));
            Button delete = new Button(this); delete.setText("Delete");
            delete.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setMessage("Delete this recording?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (d,w) -> {
                    new File(getFilesDir(), r.file).delete();
                    recordings.remove(index); save(); renderList();
                }).show());
            actions.addView(play); actions.addView(delete); card.addView(actions);
        }
    }

    private void playRecording(Recording r) {
        try {
            if (player != null) { player.release(); player=null; }
            player = new MediaPlayer();
            player.setDataSource(new File(getFilesDir(), r.file).getAbsolutePath());
            player.setOnPreparedListener(mp -> { mp.start(); Toast.makeText(this, "Playing recording", Toast.LENGTH_SHORT).show(); });
            player.setOnCompletionListener(mp -> { mp.release(); player=null; });
            player.prepareAsync();
        } catch (Exception e) {
            Toast.makeText(this, "Cannot play this audio format: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void load() {
        try {
            JSONArray a = new JSONArray(getPreferences(0).getString("items", "[]"));
            for (int i=0;i<a.length();i++) {
                JSONObject o=a.getJSONObject(i);
                recordings.add(new Recording(o.getString("file"),o.getString("contact"),o.getString("phone"),o.getString("date")));
            }
        } catch (Exception ignored) {}
    }

    private void save() {
        JSONArray a = new JSONArray();
        try { for (Recording r: recordings) a.put(r.json()); }
        catch (Exception ignored) {}
        getPreferences(0).edit().putString("items",a.toString()).apply();
    }

    private int dp(int x) { return (int)(x * getResources().getDisplayMetrics().density + 0.5f); }

    @Override protected void onDestroy() {
        if (player != null) { player.release(); player=null; }
        super.onDestroy();
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 21 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            importUri(data.getData());
        }
    }
}
