package com.ubicomp.grandmaframe;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Toast;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.view.WindowUtils;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class ScrollCardsActivity extends Activity implements Runnable{
    private static final String TAG = ScrollCardsActivity.class.getSimpleName();

    private boolean mVoiceMenuEnabled = true;
    private String mPicturePath;
    private CardScrollView mCardScroller;
    private CardScrollAdapter mAdapter;
    static final int CARD_BUILDER = 0;


    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        // Requests a voice menu on this activity. As for any other window feature,
        // be sure to request this before setContentView() is called
        getWindow().requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS);

        // Ensure screen stays on during demo.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mAdapter = new CardAdapter(createCards(this));
        mCardScroller = new CardScrollView(this);
        mCardScroller.setAdapter(mAdapter);
        setContentView(mCardScroller);
        setCardScrollerListener();
    }

    private List<CardBuilder> createCards(Context context) {
        ArrayList<CardBuilder> cards = new ArrayList<CardBuilder>();
        cards.add(CARD_BUILDER, new CardBuilder(context, CardBuilder.Layout.TEXT)
                .addImage(loadImage()));
        return cards;
    }
    private void setCardScrollerListener() {
        mCardScroller.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "Clicked view at position " + position + ", row-id " + id);
                int soundEffect = Sounds.TAP;
                // Toggles voice menu. Invalidates menu to flag change.
                mVoiceMenuEnabled = !mVoiceMenuEnabled;
                getWindow().invalidatePanelMenu(WindowUtils.FEATURE_VOICE_COMMANDS);


                Toast.makeText(ScrollCardsActivity.this, "Sending email...", Toast.LENGTH_LONG).show();
                Thread thread = new Thread(ScrollCardsActivity.this);
                thread.start();
//                (new File(mPicturePath)).delete();
//                Toast.makeText(ScrollCardsActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
//                finish();
                // Play sound.
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                am.playSoundEffect(soundEffect);
            }
        });
        setContentView(mCardScroller);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mCardScroller.activate();
    }

    @Override
    protected void onPause() {
        mCardScroller.deactivate();
        super.onPause();
    }

    public void run() {
        Mail m = new Mail(Constant.em, Constant.constant);
        String[] toArr = {Constant.frame};
        m.setTo(toArr);
        m.setFrom(Constant.em);
        m.setSubject("Picture taken with google glass");
        m.setBody("to grandma frame");
        try {
            m.addAttachment(mPicturePath);

            if(m.send()) {
                Toast.makeText(ScrollCardsActivity.this, "sent", Toast.LENGTH_SHORT).show();
//                (new File(mPicturePath)).delete();
            }
            else {
                Toast.makeText(ScrollCardsActivity.this, "failed", Toast.LENGTH_SHORT).show();
            }
        } catch(Exception e) {
            Log.e("MailApp", "Could not send email", e);
        }
    }
    private Bitmap loadImage(){
        File mPictureFilePath = new File("storage/emulated/0/DCIM/Camera");
        List<String> paths = new ArrayList<String>();
        File[] files = mPictureFilePath.listFiles();
        for (File file : files) {
            try {
                if (isJPEG(file))
                    paths.add(file.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.v(TAG, String.valueOf(paths.size()));
        }
        Bitmap myBitmap = BitmapFactory.decodeFile(paths.get(1));
        mPicturePath = paths.get(0);//TODO this is a return variable attach to card
        Bitmap scaled = Bitmap.createScaledBitmap(myBitmap, 640, 360, true);
        return scaled;
    }

    private static Boolean isJPEG(File filename) throws Exception {
        DataInputStream ins = new DataInputStream(new BufferedInputStream(new FileInputStream(filename)));
        try {
            if (ins.readInt() == 0xffd8ffe0) {
                return true;
            } else {
                return false;

            }
        } finally {
            ins.close();
        }
    }
}
