package com.hhz.hhztestboot.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by admin on 2017/4/14.
 */
public class GsonUtil {
    private static final Gson DEFAULT_GSON = new GsonBuilder().registerTypeAdapter(Date.class, (JsonDeserializer<Date>) (json, typeOfT, context) -> {
        String dateStr = json.getAsString();
        Date parseDate;
        try {
            parseDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateStr);
        } catch (ParseException e) {
            throw new RuntimeException("data parse exp", e);
        }
        return parseDate;
    }).create();

    private static final Gson ES_GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();


    public static String toJsonStr(Object obj) {
        return DEFAULT_GSON.toJson(obj);
    }


    public static Gson getDefaultGson() {
        return DEFAULT_GSON;
    }

    public static Gson getEsGson() {
        return ES_GSON;
    }
}