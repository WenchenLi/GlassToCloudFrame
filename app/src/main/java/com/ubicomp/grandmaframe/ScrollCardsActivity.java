package com.ubicomp.grandmaframe;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

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
    private List<String> mPicturesPath;
    private String mPicturePath;
    private CardScrollView mCardScroller;
    private CardScrollAdapter mAdapter;
    private int mPicture=0;


    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        // Requests a voice menu on this activity. As for any other window feature,
        // be sure to request this before setContentView() is called
        getWindow().requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS);

        mAdapter = new CardAdapter(createCards(this));
        mCardScroller = new CardScrollView(this);
        mCardScroller.setAdapter(mAdapter);
        setContentView(mCardScroller);
        setCardScrollerListener();
    }

    private List<CardBuilder> createCards(Context context) {
        ArrayList<CardBuilder> cards = new ArrayList<CardBuilder>();
        mPicturesPath = loadImages();


        BitmapFactory.Options opts=new BitmapFactory.Options();
        opts.inJustDecodeBounds=true;
        for (String path : mPicturesPath){
            Bitmap myBitmap = BitmapFactory.decodeFile(path);

            Bitmap.createScaledBitmap(myBitmap, 640, 360, true);
            cards.add(new CardBuilder(context, CardBuilder.Layout.CAPTION)
                .addImage(Bitmap.createScaledBitmap(myBitmap, 640, 360, true)));
        }
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

                mPicturePath= mPicturesPath.get(position);
                Log.v(TAG+":onItemClick",mPicturePath);

//                Toast.makeText(ScrollCardsActivity.this, "Sending email...", Toast.LENGTH_LONG).show();
//                Thread thread = new Thread(ScrollCardsActivity.this);
//                thread.start();
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

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
            getMenuInflater().inflate(R.menu.voice_menu, menu);
            return true;
        }
        // Good practice to pass through, for options menu.
        return super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
            // Dynamically decides between enabling/disabling voice menu.
            return mVoiceMenuEnabled;
        }
        // Good practice to pass through, for options menu.
        return super.onPreparePanel(featureId, view, menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
            switch (item.getItemId()) {
                case R.id.send_picture:
                    mPicturePath=mPicturesPath.get(mCardScroller.getSelectedItemPosition());
                    Log.v(TAG+":onMenuItemSelected",mPicturePath);
                    Thread thread = new Thread(ScrollCardsActivity.this);
                    thread.start();
                    try{
                        mCardScroller.setSelection(mCardScroller.getSelectedItemPosition()-1);
                    }catch (NullPointerException e){
                        mCardScroller.setSelection(mCardScroller.getSelectedItemPosition()+1);

                    }
                    break;
                case R.id.delete_picture:
                    String path = mPicturesPath.get(mCardScroller.getSelectedItemPosition());
                    Log.v(TAG,"delete file"+path);
                    File file = new File(path);
                    boolean deleted = file.delete();
                    AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    if(deleted){
                        int soundEffect = Sounds.SUCCESS;

                        am.playSoundEffect(soundEffect);
                    }
                    else{
                        int soundEffect = Sounds.ERROR;
                        am.playSoundEffect(soundEffect);
                    }
                    break;
                case R.id.menu_coder1:   mPicture = 1; break;
                case R.id.menu_coder2:   mPicture = 2; break;
                case R.id.menu_coder3:   mPicture = 3; break;
                case R.id.menu_coder4:   mPicture = 4; break;
                case R.id.menu_coder5:   mPicture = 5; break;

                default: return true;  // No change.
            }
            mCardScroller.setAdapter(new CardAdapter(createCards(this)));
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
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
                int soundEffect = Sounds.SUCCESS;
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                am.playSoundEffect(soundEffect);
                String path = mPicturesPath.get(mCardScroller.getSelectedItemPosition());
                Log.v(TAG,"delete file"+path);
                File file = new File(path);
                boolean deleted = file.delete();

//                if(deleted) {
//                    try {
//                        mCardScroller.setSelection(mCardScroller.getSelectedItemPosition() + 1);
//                    } catch (NullPointerException e) {
//                        mCardScroller.setSelection(mCardScroller.getSelectedItemPosition() - 1);
//                    }
//                }
//                (new File(mPicturePath)).delete();
            }
            else {
                int soundEffect = Sounds.ERROR;
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                am.playSoundEffect(soundEffect);
            }
        } catch(Exception e) {
            Log.e("MailApp", "Could not send email", e);
        }
    }
    private List<String> loadImages(){
        File mPictureFilePath = new File("storage/emulated/0/DCIM/Camera");
        List<String> paths = new ArrayList<String>();
        File[] files = mPictureFilePath.listFiles();
        for (File file : files) {//TODO this is not a good way to do it, should pass to adapter
            try {
                if (isJPEG(file))
                    paths.add(file.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
//            Log.v(TAG, String.valueOf(paths.size()));
        }
        return paths;
//        Bitmap myBitmap = BitmapFactory.decodeFile(paths.get(1));
//        mPicturePath = paths.get(40);//TODO this is a return variable attach to card
//        Bitmap scaled = Bitmap.createScaledBitmap(myBitmap, 640, 360, true);
//        return scaled;
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
