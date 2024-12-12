package com.example.battery.http;

/**
 * Created by Saturday on 2023/10/24.
 */

public interface ServerUrls {

    String DOMAIN = "https://qaapp.atlbattery.com";
    String USER_PROFESSIONAL = DOMAIN + "/IoT/GetUserProfessional";
    String PHONE_USAGE = DOMAIN + "/IoT/GetPhoneUsage";
    String PHONE_BRAND = DOMAIN + "/IoT/GetPhoneBrand";
    String SUBMIT_PHONE_DATA = DOMAIN + "/IoT/SubmitPhoneData";
    String REPORT_URL = DOMAIN + "/IoT/BatteryInfo";
    String SETTING_URL = DOMAIN + "/IoT/GetSettingInfo";
}
