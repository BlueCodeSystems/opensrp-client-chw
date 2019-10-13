package org.smartregister.brac.hnpp.utils;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.brac.hnpp.BuildConfig;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class HnppConstants extends CoreConstants {
    public static final String TEST_GU_ID = "test";
    public static final String MODULE_ID_TRAINING = "TRAINING";

    public static ArrayList<String> getClasterSpinnerArray() {

        return new ArrayList<>(getClasterNames().keySet());
    }
    public static String getClusterNameFromValue(String value){
        HashMap<String, String> keys = getClasterNames();
        for (String key: keys.keySet()){
            if(keys.get(key).equalsIgnoreCase(value)){
                return key;
            }
        }
        return "";
    }

    public static HashMap<String, String> getClasterNames() {
        LinkedHashMap<String,String> clusterArray = new LinkedHashMap<>();
        clusterArray.put("ক্লাস্টার ১", "1st_Cluster");
        clusterArray.put("ক্লাস্টার ২", "2nd_Cluster");
        clusterArray.put("ক্লাস্টার ৩", "3rd_Cluster");
        clusterArray.put("ক্লাস্টার ৪", "4th_Cluster");
        return clusterArray;
    }

    public static final class DrawerMenu {
        public static final String ELCO_CLIENT = "Elco Clients";
        public static final String ALL_MEMBER = "All member";
    }

    public static final class FORM_KEY {
        public static final String SS_INDEX = "ss_index";
        public static final String VILLAGE_INDEX = "village_index";
    }


    public static boolean isReleaseBuild(){
        AllSharedPreferences preferences = Utils.getAllSharedPreferences();
        String usingUrl = preferences.getPreference(AllConstants.DRISHTI_BASE_URL);
        if(!TextUtils.isEmpty(usingUrl) && usingUrl.equalsIgnoreCase(BuildConfig.opensrp_url_release)){
            return true;
        }
        return false;
    }
    public static String getSimPrintsProjectId(){

        return isReleaseBuild()?BuildConfig.SIMPRINT_PROJECT_ID_RELEASE:BuildConfig.SIMPRINT_PROJECT_ID_TRAINING;
    }
    public static final class KEY {
        public static final String TOTAL_MEMBER = "member_count";
        public static final String VILLAGE_NAME = "village_name";
        public static final String CLASTER = "claster";
        public static final String MODULE_ID = "module_id";
        public static final String RELATION_WITH_HOUSEHOLD = "relation_with_household_head";
        public static final String GU_ID = "gu_id";
        public static final String HOUSE_HOLD_ID = "house_hold_id";
        public static final String HOUSE_HOLD_NAME = "house_hold_name";
        public static final String SS_NAME = "ss_name";
        public static final String CHILD_MOTHER_NAME_REGISTERED = "mother_name";
        public static final String CHILD_MOTHER_NAME = "Mother_Guardian_First_Name_english";
        public static final String ID_AVAIL = "id_avail";
        public static final String NATIONAL_ID = "national_id";
        public static final String BIRTH_ID = "birth_id";
        public static final String IS_BITHDAY_KNOWN = "is_birthday_known";
        public static final String BLOOD_GROUP = "blood_group";
    }

    public static class IDENTIFIER {
        public static final String FAMILY_TEXT = "Family";

        public IDENTIFIER() {
        }
    }

    public static String getRelationWithHouseholdHead(String value){
        try {
            JSONObject choiceObject = new JSONObject(relationshipObject);
            for (int i = 0; i < choiceObject.names().length(); i++) {
                if (value.equalsIgnoreCase(choiceObject.getString(choiceObject.names().getString(i)))) {
                    value = choiceObject.names().getString(i);
                    return value;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return value;
    }
    public static String relationshipObject = "{" +
            "  \"খানা প্রধান\": \"Household Head\"," +
            "  \"মা\": \"Mother\"," +
            "  \"বাবা\": \"Father\"," +
            "  \"ছেলে\": \"Son\"," +
            "  \"মেয়ে\": \"Daughter\"," +
            "  \"স্ত্রী\": \"Wife\"," +
            "  \"স্বামী\": \"Husband\"," +
            "  \"নাতি\": \"Grandson\"," +
            "  \"নাতনী\": \"GrandDaughter\"," +
            "  \"ছেলের বউ\": \"SonsWife\"," +
            "  \"মেয়ের স্বামী\": \"DaughtersHusband\"," +
            "  \"শ্বশুর\": \"Father in law\"," +
            "  \"শাশুড়ি\": \"Mother in law\"," +
            "  \"দাদা\": \"Grandpa\"," +
            "  \"দাদি\": \"Grandma\"," +
            "  \"নানা\": \"Grandfather\"," +
            "  \"নানী\": \"Grandmother\"," +
            "  \"অন্যান্য\": \"Others\"" +
            "}";
}
