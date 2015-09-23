package com.ubicomp.grandmaframe;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wenchen on 9/22/15.
 */
public class Util {
    static  List<String> loadImages(){
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
//            Log.v(TAG, String.valueOf(paths.size()));
        }
        return paths;
//        Bitmap myBitmap = BitmapFactory.decodeFile(paths.get(1));
//        mPicturePath = paths.get(40);//TODO this is a return variable attach to card
//        Bitmap scaled = Bitmap.createScaledBitmap(myBitmap, 640, 360, true);
//        return scaled;
    }

     static Boolean isJPEG(File filename) throws Exception {
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
