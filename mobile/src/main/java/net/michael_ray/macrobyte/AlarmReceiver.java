package net.michael_ray.macrobyte;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int array_id = intent.getIntExtra("marker_id",-1);
        Log.d("MacroByte", "Timer expired for: "+array_id);
        if (array_id>=0 && array_id<((MacroByte) context.getApplicationContext()).markers.size()) {
            ((MacroByte) context.getApplicationContext()).markers.get(array_id).remove();
            ((MacroByte) context.getApplicationContext()).markers.remove(array_id);
        }
    }

}