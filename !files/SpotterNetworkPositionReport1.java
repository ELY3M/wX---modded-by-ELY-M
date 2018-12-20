package joshuatee.wx.settings;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import com.koushikdutta.async.http.AsyncHttpPost;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;

public class SpotterNetworkPositionReport {
    private static String TAG = SpotterNetworkPositionReport.class.getSimpleName();
    public boolean CanReport;
    public String id;
    OnSnTransmitListener mListener;
    private Context mRm;
    public String marker;
    public boolean success;

    public interface OnSnTransmitListener {
        void sendToast(String str);
    }

    public void setOnSnTransmitListener(OnSnTransmitListener listen) {
        this.mListener = listen;
    }

    public SpotterNetworkPositionReport(Context rm) {
        this.mRm = rm;
    }

    public boolean SendPosition(String key, Location position) {
        if (position == null) {
            Log.i(TAG, "No SN report sent - Null position packet");
        }
        if (key.length() < 2 || (position.getLatitude() == 0.0d && position.getLongitude() == 0.0d)) {
            Log.i(TAG, "No SN report sent - Invalid data: Key " + key + " Position " + position.getLatitude() + "/" + position.getLongitude());
            if (this.mListener != null && key.length() < 2) {
                this.mListener.sendToast("Please check your SpotterNetwork login credentials.  Menu>Settings>SpotterNetwork.org");
            }
            return true;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String[] info = new String[9];
        info[0] = key;
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

    protected Void sendSnReport(String[] params) {
        HttpHandler sh = new HttpHandler();
        String url = "https://www.spotternetwork.org/positions/update";
        JSONObject object = new JSONObject();
        try {
            object.put(Name.MARK, params[0]);
            object.put("report_at", params[1]);
            object.put("lat", (double) Float.parseFloat(params[2]));
            object.put("lon", (double) Float.parseFloat(params[3]));
            object.put("elev", Math.round(Float.parseFloat(params[4])));
            object.put("mph", Math.round(Float.parseFloat(params[5])));
            object.put("dir", Math.round((float) Integer.parseInt(params[6])));
            object.put("active", Integer.parseInt(params[7]));
            object.put("gps", Integer.parseInt(params[8]));
        } catch (JSONException e) {
            Log.i(TAG, e);
        }
        Log.i(TAG,"Sending SN Position " + params[2] + " " + params[3]);
        String jsonStr = sh.makeServiceCall(url, object, AsyncHttpPost.METHOD);
        if (jsonStr != null) {
            try {
                this.success = new JSONObject(jsonStr).getBoolean("success");
                if (!this.success) {
                    if (this.mListener != null) {
                        this.mListener.sendToast("SpotterNetwork was unable to process your position update : ");
                    }
                }
            } catch (JSONException e2) {
                Log.i(TAG,"SpotterNetwork did not process position update JSON ERROR");
            }
        } else {
            Log.i(TAG, "SpotterNetwork did not process position update NULL RETURNED");
        }
        return null;
    }
}
