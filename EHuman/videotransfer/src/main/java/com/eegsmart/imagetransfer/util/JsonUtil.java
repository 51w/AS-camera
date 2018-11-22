package com.eegsmart.imagetransfer.util;

import android.graphics.Point;
import android.util.Log;

import com.eegsmart.imagetransfer.VTConstants;
import com.eegsmart.imagetransfer.model.JsonInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class JsonUtil {
    private static final String TAG = "JsonObjectIUtil";

    public static String creatJson(int cmd, int param) {
        JSONObject pJson = new JSONObject();
        try {
            pJson.put(VTConstants.CMD, cmd);
            pJson.put(VTConstants.PARAM, param);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "creatJson :" + pJson.toString() + pJson.toString().length());
        return pJson.toString();
    }

    public static String creatJson(int cmd, String param) {
        JSONObject pJson = new JSONObject();
        try {
            pJson.put(VTConstants.CMD, cmd);
            pJson.put(VTConstants.PARAM, param);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "creatJson :" + pJson.toString() + pJson.toString().length());
        return pJson.toString();
    }
    //{ "CMD": 11, "PARAM": {"num": xxx, "delay": xxx}}

    public static String creatTakePicJson(int cmd, int num, int delay) {
        JSONObject cJson = new JSONObject();
        JSONObject pJson = new JSONObject();
        try {
            cJson.put(VTConstants.CMD, cmd);
            pJson.put(VTConstants.NUM, num);
            pJson.put(VTConstants.DELAY, delay);
            cJson.put(VTConstants.PARAM, pJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "creatJson :" + pJson.toString() + pJson.toString().length());
        return cJson.toString();
    }

    public static String creatJson(int cmd, String ssid, String password) {
        JSONObject cJson = new JSONObject();
        JSONObject pJson = new JSONObject();
        try {
            cJson.put(VTConstants.CMD, cmd);
            pJson.put(VTConstants.WIFI_SSID, ssid);
            pJson.put(VTConstants.PHRASE, password);
            cJson.put(VTConstants.PARAM, pJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "creatJson :" + pJson.toString() + pJson.toString().length());
        return cJson.toString();
    }

    public static String creatJson(int cmd, int width, int height) {
        JSONObject cJson = new JSONObject();
        JSONObject pJson = new JSONObject();
        try {
            cJson.put(VTConstants.CMD, cmd);
            pJson.put(VTConstants.POSITION_X, width);
            pJson.put(VTConstants.POSITION_Y, height);
            cJson.put(VTConstants.PARAM, pJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "creatJson :" + pJson.toString() + pJson.toString().length());
        return cJson.toString();
    }

    public static String creatPreviewJson(int cmd, int width, int height) {
        JSONObject cJson = new JSONObject();
        JSONObject pJson = new JSONObject();
        try {
            cJson.put(VTConstants.CMD, cmd);
            pJson.put(VTConstants.PARAM_WIDTH, width);
            pJson.put(VTConstants.PARAM_HEIGHT, height);
            cJson.put(VTConstants.PARAM, pJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "creatJson :" + pJson.toString() + pJson.toString().length());
        return cJson.toString();
    }

    public static String creatFollowJson(int cmd, Point startPoint, Point endPoint) {
        JSONObject cJson = new JSONObject();
        JSONObject pJson = new JSONObject();
        try {
            cJson.put(VTConstants.CMD, cmd);
            pJson.put(VTConstants.POSITION_X0, startPoint.x);
            pJson.put(VTConstants.POSITION_Y0, startPoint.y);
            pJson.put(VTConstants.POSITION_X1, endPoint.x);
            pJson.put(VTConstants.POSITION_Y1, endPoint.y);
            cJson.put(VTConstants.PARAM, pJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "creatJson :" + pJson.toString() + pJson.toString().length());
        return cJson.toString();
    }

    public static String creatFormatJson(int cmd, int type) {
        JSONObject cJson = new JSONObject();
        JSONObject pJson = new JSONObject();
        try {
            cJson.put(VTConstants.CMD, cmd);
            pJson.put("type", type);
            cJson.put(VTConstants.PARAM, pJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "creatJson :" + pJson.toString() + pJson.toString().length());
        return cJson.toString();
    }

    /**
     * 1:
     */
    public static String creatWifiJson(String type, String ssid, String password) {
        JSONObject pJson = new JSONObject();
        try {
            pJson.put(type, 0);
            pJson.put("ssid", ssid);
            pJson.put("password", password);

            // pJson.put("value", number);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "creatJson :" + pJson.toString() + pJson.toString().length());

        return pJson.toString();
    }

    /*
     * {"TimeSet":0,
     * "year":2016,
     *  "month":6,
     *     "day":26,
     *    "hour":8,
     *     "min":18,
     *     "sec":18}
     */
    public static String creatTimeJson(int type) {
        JSONObject pJson = new JSONObject();
        JSONObject timeJson = new JSONObject();
        final Calendar calendar = Calendar.getInstance();
        try {
            pJson.put(VTConstants.CMD, type);
            timeJson.put(VTConstants.YEAR, calendar.get(Calendar.YEAR));
            timeJson.put(VTConstants.MONTH, calendar.get(Calendar.MONTH) + 1);
            timeJson.put(VTConstants.DAY, calendar.get(Calendar.DAY_OF_MONTH));
            timeJson.put(VTConstants.HOUR, calendar.get(Calendar.HOUR_OF_DAY));
            timeJson.put(VTConstants.MINUTE, calendar.get(Calendar.MINUTE));
            timeJson.put(VTConstants.SECOND, calendar.get(Calendar.SECOND));
            pJson.put(VTConstants.PARAM, timeJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "creatJson :" + pJson.toString() + pJson.toString().length());

        return pJson.toString();
    }

    public static boolean getJson(String result) {
        if (result == null) {
            return false;
        }
        try {
            JSONObject jsonObject = new JSONObject(result);
            if (jsonObject.get(VTConstants.CMD).equals(0)) {
                return true;
            } else {
                return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static JsonInfo getArrayJson(String result) {
        if (result == null) {
            return null;
        }
        JsonInfo jsonInfo = null;
        try {
            JSONObject jsonObject = new JSONObject(result);
            Object object = jsonObject.get(VTConstants.RESULT);
            if (object.equals(-1)) {
                return null;
            }/* else if (object.equals(1)) {
                return new JsonInfo(VTConstants.RESULT,
						jsonObject.getString(VTConstants.RESULT));
			}*/
            jsonInfo = new JsonInfo(jsonObject.getString(VTConstants.CMD),
                    jsonObject.getString(VTConstants.RESULT));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonInfo;
    }

    public static JsonInfo getResultJson(String result) {
        if (result == null) {
            return null;
        }
        JsonInfo jsonInfo = null;
        try {
            JSONObject jsonObject = new JSONObject(result);
            jsonInfo = new JsonInfo(jsonObject.getString(VTConstants.CMD),
                    jsonObject.getInt(VTConstants.RESULT));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonInfo;
    }

    public static List<String> getThumbJson(String result, String type) {
        Log.d(TAG, "getTypeJson:" + result == null ? "yes" : "no:" + result);
        if (result == null) {
            return null;
        } else {
            Log.d(TAG, "JSONArray:" + result);
            List<String> filenames = new ArrayList<String>();
            try {
                JSONArray jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    filenames.add(jsonObject.getString(type));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "e.printStackTrace():" + e.toString());
            }
            Log.d(TAG, "filenames:" + filenames.toString());
            return filenames;
        }
    }


    public static String filtToSprit(String result) {
        Log.d(TAG, "replace before: " + result);
        result = result.replace("\\", "");
        result = result.replace(" ", "");
        result = result.replace("\"{", "{");
        result = result.replace("}\"", "}");
        Log.d(TAG, "replace after1: " + result);
        return result;

    }

    public static String creatJson(String type, String date) {
        JSONObject pJson = new JSONObject();
        try {
            pJson.put(type, date);
            // pJson.put("value", number);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "creatJson :" + pJson.toString() + pJson.toString().length());

        return pJson.toString();
    }

    public static String creatJson(String type, String ssid, String password) {
        JSONObject pJson = new JSONObject();
        JSONObject wifiJson = new JSONObject();
        try {
            pJson.put(VTConstants.CMD, type);
            wifiJson.put(VTConstants.WIFI_SSID, ssid);
            wifiJson.put(VTConstants.PHRASE, password);
            pJson.put(VTConstants.PARAM, wifiJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "creatJson :" + pJson.toString() + pJson.toString().length());

        return pJson.toString();
    }
}
