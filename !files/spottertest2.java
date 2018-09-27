import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class snreport {
  public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
  
 // create your json here
   JSONObject jsonObject = new JSONObject();
   try {
       jsonObject.put("username", "yourEmail@com");
       jsonObject.put("password", "yourPassword");
       jsonObject.put("anyKey", "anyValue");

   } catch (JSONException e) {
       e.printStackTrace();
   }

  OkHttpClient client = new OkHttpClient();
  MediaType JSON = MediaType.parse("application/json; charset=utf-8");
  // put your json here
  RequestBody body = RequestBody.create(JSON, jsonObject.toString());
  Request request = new Request.Builder()
                    .url("https://yourUrl/")
                    .post(body)
                    .build();

  Response response = null;
  try {
      response = client.newCall(request).execute();
      String resStr = response.body().string();
  } catch (IOException e) {
      e.printStackTrace();
  }
  
}