package com.example.bluetooth_testing;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class SoundPlayer
{
    private static SoundPool soundPool;
    private static int successSound ;
    private static int failSound;


    public SoundPlayer(Context context)
    {
        soundPool= new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        successSound= soundPool.load(context, R.raw.applause, 1);
        failSound= soundPool.load(context, R.raw.fail, 1);
    }

    public void playSuccessSound()
    {
        soundPool.play(successSound, 1,1,1,0,1);
    }
    public void playFailSound()
    {
        soundPool.play(failSound, 1,1,1,0,1);
    }

}
