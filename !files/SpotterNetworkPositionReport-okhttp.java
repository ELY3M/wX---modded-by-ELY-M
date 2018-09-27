package joshuatee.wx.radar;

import joshuatee.wx.MyApplication;
import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class SpotterNetworkPositionReport {
    private static String TAG = "joshuatee-SpotterNetworkPositionReport";
    public boolean CanReport;
    public String id;
    //OnSnTransmitListener mListener;
    private Context context;
    public String marker;
    public boolean success;

    public void sendToast(String text) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }


    //public interface OnSnTransmitListener {
    //    void sendToast(String str);
    //}

    //public void setOnSnTransmitListener(OnSnTransmitListener listen) {
    //    this.mListener = listen;
    //}

    //public SpotterNetworkPositionReport(Context rm) {
    //    this.mRm = rm;
    //}

    public boolean SendPosition(String key, Location position) {
        if (position == null) {
            Log.i(TAG, "No SN report sent - Null position packet");
        }
        if (key.length() < 2 || (position.getLatitude() == 0.0d && position.getLongitude() == 0.0d)) {
            Log.i(TAG, "No SN report sent - Invalid data: Key " + key + " Position " + position.getLatitude() + "/" + position.getLongitude());
            if (key.length() < 2) {
                sendToast("Please check your SpotterNetwork login credentials.");
            }
            return true;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String[] info = new String[9];
        info[0] = MyApplication.sn_key;
        info[1] = sdf.format(Long.valueOf(position.getTime()));
        info[2] = String.format(Locale.US, "%.6f", new Object[]{Double.valueOf(position.getLatitude())});
        info[3] = String.format(Locale.US, "%.6f", new Object[]{Double.valueOf(position.getLongitude())});
        info[4] = String.format(Locale.US, "%.0f", new Object[]{Double.valueOf(position.getAltitude() * 3.281d)});
        info[5] = String.format(Locale.US, "%.1f", new Object[]{Double.valueOf(((double) position.getSpeed()) * 2.23694d)});
        info[6] = String.format(Locale.US, "%.0f", new Object[]{Float.valueOf(position.getBearing())});
        info[7] = "1";
        if (position.getProvider().equals("gps")) {
            info[8] = "1";
        } else {
            info[8] = "0";
        }
        sendSnReport(info);
        return false;
    }

    /*
    *

   {
    "id": "APPLICATION-ID",
    "report_at": "YYYY-MM-DD HH:MM:SS",
    "lat": 39.7553101,
    "lon": -105.2330093,
    "elev": 0,
    "mph": 7.5,
    "dir": 328,
    "active": 1,
    "gps": 1
}

    *
    * */

    protected Void sendSnReport(String[] params) {
        OkHttpClient sh = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        String url = "https://www.spotternetwork.org/positions/update";
        JSONObject object = new JSONObject();
        try {
            object.put("id", params[0]);
            object.put("report_at", params[1]);
            object.put("lat", (double) Float.parseFloat(params[2]));
            object.put("lon", (double) Float.parseFloat(params[3]));
            object.put("elev", Math.round(Float.parseFloat(params[4])));
            object.put("mph", Math.round(Float.parseFloat(params[5])));
            object.put("dir", Math.round((float) Integer.parseInt(params[6])));
            object.put("active", Integer.parseInt(params[7]));
            object.put("gps", Integer.parseInt(params[8]));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i(TAG,"Sending SN Position " + params[2] + " " + params[3]);



        RequestBody body = RequestBody.create(JSON, object.toString());
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Response response = null;
        String jsonStr = null;
        try {
            response = sh.newCall(request).execute();
            jsonStr = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (jsonStr != null) {
            try {
                success = new JSONObject(jsonStr).getBoolean("success");
                if (!success) {
                    sendToast("SpotterNetwork was unable to process your position update";
                    }
            } catch (JSONException e) {
                Log.i(TAG,"SpotterNetwork did not process position update JSON ERROR");
            }
        } else {
            Log.i(TAG, "SpotterNetwork did not process position update NULL RETURNED");
        }
        return null;
    }
}
