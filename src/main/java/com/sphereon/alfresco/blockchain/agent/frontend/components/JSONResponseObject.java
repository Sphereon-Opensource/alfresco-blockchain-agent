package com.sphereon.alfresco.blockchain.agent.frontend.components;

import org.apache.commons.beanutils.BeanUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author Created by Sander on 20-1-2015.
 */
public abstract class JSONResponseObject
{
    public JSONResponseObject(JSONArray jsonArray, int parmIndex) throws Exception
    {
        if (jsonArray.length() > parmIndex)
        {
            JSONObject jsonObject = (JSONObject) jsonArray.get(parmIndex);
            Map<String, Object> map = getMap(jsonObject, this);
            BeanUtils.populate(this, map);
        }
    }


    protected Map<String, Object> getMap(JSONObject jsonObject, Object thiz) throws JSONException
    {
        Map<String, Object> map = new HashMap<>();
        for (Iterator<String> keys = jsonObject.keys(); keys.hasNext(); )
        {
            String key = keys.next();
            String jkey = Character.toLowerCase(key.charAt(0)) + key.substring(1);
            Object item = jsonObject.get(key);
            if (item instanceof JSONObject)
            {
                try
                {
                    Field f = thiz.getClass().getDeclaredField(jkey);
                    if (f != null)
                    {
                        Object instance = f.getType().newInstance();
                        Map<String, Object> itemMap = getMap((JSONObject) item, instance);
                        BeanUtils.populate(instance, itemMap);
                        map.put(jkey, instance);
                    }
                } catch (Exception ignore)
                {
                    ignore.printStackTrace();
                }
            } else if (item instanceof JSONArray) {
                JSONArray itemArray = (JSONArray) item;
                List list = new ArrayList();
                for (int i = 0; i < itemArray.length(); i++) {
                    Object arrayItem = itemArray.get(i);
                    if (arrayItem instanceof JSONObject) {
                        try {
                            Field f = thiz.getClass().getDeclaredField(jkey);
                            if (f != null) {
                                Object instance = null;
                                Type genericType = f.getGenericType();
                                if (genericType instanceof ParameterizedType) {
                                    ParameterizedType parameterizedType = (ParameterizedType) genericType;
                                    if (parameterizedType.getActualTypeArguments() != null && parameterizedType.getActualTypeArguments().length > 0) {
                                        instance = ((Class) parameterizedType.getActualTypeArguments()[0]).newInstance();
                                    }
                                }
                                if (instance == null) {
                                    instance = f.getType().newInstance();
                                }
                                Map<String, Object> itemMap = getMap((JSONObject) arrayItem, instance);
                                BeanUtils.populate(instance, itemMap);
                                list.add(instance);
                            }
                        } catch (Exception ignore) {
                            ignore.printStackTrace();
                        }
                    } else {
                        list.add(arrayItem);
                    }
                }
                map.put(jkey, list);
            } else {
                map.put(jkey, item);
            }
        }
        return map;
    }


    public JSONObject toJSONObject() throws JSONException
    {
        JSONObject sourceObject = new JSONObject(this);
        Map<String, Object> map = new HashMap<>();
        for (Iterator<String> keys = sourceObject.keys(); keys.hasNext(); )
        {
            String key = keys.next();
            String jkey = Character.toUpperCase(key.charAt(0)) + key.substring(1);
            map.put(jkey, sourceObject.get(key));
        }
        return new JSONObject(map);
    }
}
