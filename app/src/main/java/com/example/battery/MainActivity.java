package com.example.battery;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.battery.enums.ReportConfigEnum;
import com.example.battery.http.OkHttpUtil;
import com.example.battery.http.ServerUrls;
import com.example.battery.model.BatteryInfo;
import com.example.battery.model.BatteryReportModel;
import com.example.battery.model.ReportSetting;
import com.example.battery.util.BatteryUtil;
import com.example.battery.util.DateTimeUtil;
import com.example.battery.util.ReportCacheUtil;
import com.example.battery.util.SystemInfoUtil;
import com.google.gson.Gson;
import com.gyf.cactus.Cactus;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements Thread.UncaughtExceptionHandler {
    private BatteryManager bm = null;
    private static final int REFRESH_TIME = 1;
    private static boolean runtime = false;
    private TextView mStatus_BatteryInfo;
    private TextView mPlugged_BatteryInfo;
    private TextView mLevel_BatteryInfo;
    private TextView mScale_BatteryInfo;
    private TextView mVoltage_BatteryInfo;
    private TextView mTemperature_BatteryInfo;
    private TextView mTechnology_BatteryInfo;
    private TextView mHealth_BatteryInfo;
    private TextView mRuntime_BatteryInfo;
    private TextView mCurrent_BatteryInfo;
    private TextView mUid_PhoneInfo;
    private TextView mModel_PhoneInfo;
    private TextView mMemUsage_BatteryInfo;
    private TextView mLatitude_BatteryInfo;
    private TextView mLongitude_BatteryInfo;
    private TextView mExceptionLog;
    private TimeThread timeThread;
    private TextView mUpdateUserInfo;

    private ReportSetting reportSetting;
    private BatteryReportModel reportModel;
    private BatteryInfo currBatteryInfo;
    private ScheduledExecutorService scheduledExecutorService;
    private volatile boolean networkEnabled = true;
    private long lastReportTime;

    private ReportConfigEnum preConfigEnum;
    private ScheduledFuture<?> preScheduleFuture;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//            isStoragePermissionGranted();
        bm = (BatteryManager) getSystemService(Context.BATTERY_SERVICE);
        InitView();
        reportSetting();
        initPhoneInfo();
        initModelData();
        reportSetting = new ReportSetting();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mBroadcastReceiver, filter);

        registerCactus();
    }

    /**
     * 注册保活插件
     * 这边可以设置背景音乐，可以设置单点像素，可以设置通知，通过这些方式来进行
     * 进程的保活
     */
    private void registerCactus(){
        Cactus.getInstance()
                .isDebug(false)
                .setChannelId("battery")
                .setContent("Battery is running")
                .setTitle("Battery")
                .setChannelName("Battery")
                .register(this);
    }

    /**
     * 初始化配置
     */
    private void reportSetting() {
        try {
            OkHttpUtil.get(ServerUrls.SETTING_URL, null, new Callback() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onFailure(Call call, IOException e) {
                    if (null != reportSetting) {
                        startScheduleReport();
                    }
                    logException(e);
                }

                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    reportSetting = new Gson().fromJson(response.body().string(), ReportSetting.class);
                    Log.i("[SATURDAY]", "report setting ：" + new Gson().toJson(reportSetting));
                    logException("配置信息：" + new Gson().toJson(reportSetting));
                    if (null != reportSetting) {
//                        reportSetting.setCollectFrequency(1);
//                        reportSetting.setUploadFrequency(3);
                        startScheduleReport();
                    }
                }
            });
        } catch (IOException e) {
            logException(e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private synchronized void startScheduleReport() {
        if (null != scheduledExecutorService) return;
        Log.i("[SATURDAY]", "start to scheduler");
        scheduledExecutorService = Executors.newScheduledThreadPool(1);

        // 默认第一种策略上报数据
        updateReportTask(3);
    }

    private synchronized void updateReportTask(int batteryPct) {
        if (scheduledExecutorService == null) {
            return;
        }
        Log.i("[SATURDAY-20240604]", "CURRENT batteryPc = "+batteryPct);
        ReportConfigEnum currReportConfigEnum;
        if (batteryPct > ReportConfigEnum.LEVEL_NORMAL.getMinVoltage()) {
            currReportConfigEnum = ReportConfigEnum.LEVEL_NORMAL;
        } else if (batteryPct >  ReportConfigEnum.LEVEL_1.getMinVoltage()) {
            currReportConfigEnum = ReportConfigEnum.LEVEL_1;
        } else {
            currReportConfigEnum = ReportConfigEnum.LEVEL_2;
        }
        // 确认是否需要切换上报任务
        if (this.preConfigEnum != null) {
            if (currReportConfigEnum != this.preConfigEnum) {
                // 需要变更任务
                preScheduleFuture.cancel(true);
                Log.i("[SATURDAY-20240604]", "preScheduleFuture canceled, " + preScheduleFuture.isCancelled());
                preScheduleFuture = createExecutorService(currReportConfigEnum.getCollectFrequency(), currReportConfigEnum.getUploadFrequency());
                Log.i("[SATURDAY-20240604]", "start new task, " + preScheduleFuture.hashCode());
            } else {
                // 不需要更新任务
                return;
            }
        } else {
            // 最开始，启动任务
            preScheduleFuture = createExecutorService(currReportConfigEnum.getCollectFrequency(), currReportConfigEnum.getUploadFrequency());
            Log.i("[SATURDAY-20240604]", "start new task, " + preScheduleFuture.hashCode());
        }
        this.preConfigEnum = currReportConfigEnum;

    }

    private ScheduledFuture<?> createExecutorService(int collectFrequency, int uploadFrequency) {
        if (collectFrequency == -1) {
            collectFrequency = reportSetting.getCollectFrequency();
            uploadFrequency = reportSetting.getUploadFrequency();
        }
        int finalUploadFrequency = uploadFrequency;
        int finalCollectFrequency = collectFrequency;
        Log.i("[SATURDAY-20240604]",  "collectorFrequency = " + collectFrequency + ", uploadFrequency = " + uploadFrequency);
        return scheduledExecutorService.scheduleAtFixedRate(() -> {
            // 定时收集数据
            currBatteryInfo.setTs(DateTimeUtil.format(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss"));
            currBatteryInfo.setMemoryUsage(SystemInfoUtil.getMemoryUsage(getApplicationContext()));
            try {
                BatteryInfo cloneBatteryInfo = currBatteryInfo.clone();
                reportModel.getBatteryInfoList().add(cloneBatteryInfo);
                if (reportModel.getBatteryInfoList().size() > reportSetting.getMaxOffLineQty()) {
                    // 保证上报数据的长度
                    reportModel.getBatteryInfoList().remove(0);
                }
                if (!networkEnabled && reportSetting.isUploadOffLine() && reportModel.getBatteryInfoList().size() > finalUploadFrequency / finalCollectFrequency) {
                    // 这边需要缓存对应的上报数据
                    ReportCacheUtil.save(getApplicationContext(), reportModel);
                }
            } catch (CloneNotSupportedException e) {
                logException(e);
            }

            if (networkEnabled && reportModel.getBatteryInfoList().size() >= finalUploadFrequency / finalCollectFrequency) {
                report(reportModel, null);
            }
        }, 0, collectFrequency, TimeUnit.SECONDS);
    }

    /**
     * 上报数据
     *
     * @param reportModel 上报数据模型
     * @param callback    上报回调，使用异步上报数据
     */
    private void report(BatteryReportModel reportModel, Callback callback) {
        try {
            // 防止重复上报
            if (lastReportTime != 0 && (System.currentTimeMillis() - lastReportTime) < 5000)
                return;
            lastReportTime = System.currentTimeMillis();
            reportModel.setActivationTime("2023-01-01T00:00:00");
            Log.i("[SATURDAY]", "report time: " + lastReportTime);
            // 开始上报数据
            OkHttpUtil.post(ServerUrls.REPORT_URL, reportModel, null != callback ? callback : new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    // 上报失败
                    logException(e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    // 上报成功，清空电池数据列表，重新收集
                    reportModel.getBatteryInfoList().clear();
                    logException("上报成功：" + response.body().string());
                }
            });
        } catch (Exception e) {
            logException(e);
        }
    }

    /**
     * 上报磁盘中缓存的数据，如果有的话
     */
    private void reportCacheData() {
        // 防止离线数据的重复上报
        reportModel.getBatteryInfoList().clear();
        if (!reportSetting.isUploadOffLine()) return;

        // 需要确认是否有上报数据
        BatteryReportModel reportModel = ReportCacheUtil.getReportModel(getApplicationContext());
        if (null != reportModel && reportModel.getBatteryInfoList().size() > 0) {
            // 需要上报之前的数据。
            report(reportModel, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    logException(e);
                }

                @Override
                public void onResponse(Call call, Response response) {
                    reportModel.getBatteryInfoList().clear();
                    // 将缓存置空
                    ReportCacheUtil.save(getApplicationContext(), reportModel);
                }
            });
        }
    }

    private void InitView() {
        mStatus_BatteryInfo = (TextView) findViewById(R.id.status_BatteryInfo);
        mPlugged_BatteryInfo = (TextView) findViewById(R.id.plugged_BatteryInfo);
        mLevel_BatteryInfo = (TextView) findViewById(R.id.level_BatteryInfo);
        mScale_BatteryInfo = (TextView) findViewById(R.id.scale_BatteryInfo);
        mVoltage_BatteryInfo = (TextView) findViewById(R.id.voltage_BatteryInfo);
        mTemperature_BatteryInfo = (TextView) findViewById(R.id.temperature_BatteryInfo);
        mTechnology_BatteryInfo = (TextView) findViewById(R.id.technology_BatteryInfo);
        mHealth_BatteryInfo = (TextView) findViewById(R.id.health_BatteryInfo);
        mRuntime_BatteryInfo = (TextView) findViewById(R.id.runtime_BatteryInfo);
        mCurrent_BatteryInfo = (TextView) findViewById(R.id.current_BatteryInfo);
        mUid_PhoneInfo = findViewById(R.id.uid_phoneInfo);
        mModel_PhoneInfo = findViewById(R.id.model_phoneInfo);
        mMemUsage_BatteryInfo = findViewById(R.id.memUsage_BatteryInfo);
        mLatitude_BatteryInfo = findViewById(R.id.latitude_BatteryInfo);
        mLongitude_BatteryInfo = findViewById(R.id.longitude_BatteryInfo);
        mExceptionLog = findViewById(R.id.exception_log);
        mUpdateUserInfo = findViewById(R.id.btn_update_userInfo);
    }

    private void initPhoneInfo() {
        mUid_PhoneInfo.setText(getAndroidID());
        mModel_PhoneInfo.setText(android.os.Build.MODEL);
    }

    private void initModelData() {
        reportModel = new BatteryReportModel();
        reportModel.setPhoneUid(getAndroidID());
        reportModel.setPhoneModel(mModel_PhoneInfo.getText().toString());
        currBatteryInfo = new BatteryInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startTimeThread(timeThread);
        monitorLocation();
        mUpdateUserInfo.setOnClickListener(v->{
            Intent intent = new Intent(this, UserInfoActivity.class);
            intent.putExtra("UPDATE", true);
            startActivity(intent);
        });
    }

    @Override
    protected void onPause() {
//            stopTimeThread(timeThread);
//            unregisterReceiver(mBroadcastReceiver);
        super.onPause();
    }

    /**
     * @param milliseconds 将ms转化为hh:mm:ss 格式时间
     * @return
     */
    private String msToTime(long milliseconds) {
        return DateUtils.formatElapsedTime(milliseconds / 1000);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                int status = intent.getIntExtra("status", 0);
                int health = intent.getIntExtra("health", 0);
                int level = intent.getIntExtra("level", 0);
                int scale = intent.getIntExtra("scale", 0);
                int plugged = intent.getIntExtra("plugged", 0);
                int voltage = intent.getIntExtra("voltage", 0);
                int temperature = intent.getIntExtra("temperature", 0);
                String technology = intent.getStringExtra("technology");
                mLevel_BatteryInfo.setText(level + "");
                mScale_BatteryInfo.setText(BatteryUtil.getBatteryCapacity(getApplicationContext()) + "mAh");
                mVoltage_BatteryInfo.setText(voltage + "mV");
                mTemperature_BatteryInfo.setText(((float) temperature / 10) + "℃");
                mTechnology_BatteryInfo.setText(technology + "");
                mRuntime_BatteryInfo.setText(msToTime(SystemClock.elapsedRealtime()));
                mCurrent_BatteryInfo.setText(getCurrentBatteryCurrentByBS() + "mA");
                mMemUsage_BatteryInfo.setText(new BigDecimal(SystemInfoUtil.getMemoryUsage(getApplicationContext())).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_EVEN) + "%");
                switch (status) {
                    case BatteryManager.BATTERY_STATUS_UNKNOWN:
                        mStatus_BatteryInfo.setText(getResources().getString(R.string.BatteryInfo_status_UNKNOWN));
                        break;
                    case BatteryManager.BATTERY_STATUS_CHARGING:
                        mStatus_BatteryInfo.setText(getResources().getString(R.string.BatteryInfo_status_CHARGING));
                        break;
                    case BatteryManager.BATTERY_STATUS_DISCHARGING:
                        mStatus_BatteryInfo.setText(getResources().getString(R.string.BatteryInfo_status_DISCHARGING));
                        break;
                    case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                        mStatus_BatteryInfo.setText(getResources().getString(R.string.BatteryInfo_status_NOT_CHARGING));
                        break;
                    case BatteryManager.BATTERY_STATUS_FULL:
                        mStatus_BatteryInfo.setText(getResources().getString(R.string.BatteryInfo_status_FULL));
                        break;
                }
                switch (health) {
                    case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                        mHealth_BatteryInfo.setText(getResources().getString(R.string.BatteryInfo_health_UNKOWN));
                        break;
                    case BatteryManager.BATTERY_HEALTH_GOOD:
                        mHealth_BatteryInfo.setText(getResources().getString(R.string.BatteryInfo_health_GOOD));
                        break;
                    case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                        mHealth_BatteryInfo.setText(getResources().getString(R.string.BatteryInfo_health_OVERHEAT));
                        break;
                    case BatteryManager.BATTERY_HEALTH_DEAD:
                        mHealth_BatteryInfo.setText(getResources().getString(R.string.BatteryInfo_health_DEAD));
                        break;
                    case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                        mHealth_BatteryInfo.setText(getResources().getString(R.string.BatteryInfo_health_VOLTAGE));
                        break;
                    case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                        mHealth_BatteryInfo.setText(getResources().getString(R.string.BatteryInfo_health_UNSPECIFIED_FAILURE));
                        break;
                }

                switch (plugged) {
                    case BatteryManager.BATTERY_PLUGGED_AC:
                        mPlugged_BatteryInfo.setText(getResources().getString(R.string.BatteryInfo_plugged_AC));
                        break;
                    case BatteryManager.BATTERY_PLUGGED_USB:
                        mPlugged_BatteryInfo.setText(getResources().getString(R.string.BatteryInfo_plugged_USB));
                        break;
                    default:
                        mPlugged_BatteryInfo.setText("");
                }
                // 更新上报任务
                updateReportTask((int)((level / (float)scale) * 100));
                refreshCurrentBatteryInfo(level, scale, voltage, temperature);
            } else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                // 获取连接管理器对象
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                //获取额外的网络信息
                NetworkInfo info = connectivityManager.getActiveNetworkInfo();
                if (info != null) {
                    if (NetworkInfo.State.CONNECTED == info.getState() && info.isAvailable()) {
                        if (info.getType() == ConnectivityManager.TYPE_WIFI
                                || info.getType() == ConnectivityManager.TYPE_MOBILE) {
                        }
                        networkEnabled = true;
                        // 需要上报缓存数据
                        reportCacheData();
                        // 确认是否启动即时
                        if (scheduledExecutorService == null) {
                            // 需要启动定时上报
                            reportSetting();
                        }
                        Log.i("[SATURDAY]", "network ok");
                        logException("网络已连接");
                    } else {
                        Log.i("[SATURDAY]", "network error");
                        networkEnabled = false;
                        logException("网络断开");
                    }
                } else {
                    Log.i("[SATURDAY]", "network error");
                    networkEnabled = false;
                    logException("网络断开");
                }
            }
        }
    };

    private void refreshCurrentBatteryInfo(int level, int scale, int voltage, int temperature) {
        currBatteryInfo.setBatteryStatus(mStatus_BatteryInfo.getText().toString());
        currBatteryInfo.setBatteryType(mTechnology_BatteryInfo.getText().toString());
        currBatteryInfo.setCharging(mPlugged_BatteryInfo.getText().toString());
        currBatteryInfo.setBatteryHealth(mHealth_BatteryInfo.getText().toString());
        currBatteryInfo.setCurrent(getCurrentBatteryCurrentByBS());
        currBatteryInfo.setCapacity(Double.parseDouble(BatteryUtil.getBatteryCapacity(getApplicationContext())));
        currBatteryInfo.setVoltage(voltage);
        currBatteryInfo.setTemperature(temperature / 10.0);
        currBatteryInfo.setBatteryLevel(level);
    }

    Handler eventHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH_TIME:
                    mRuntime_BatteryInfo.setText(msToTime(SystemClock.elapsedRealtime()));//获取系统启动时间
                    mCurrent_BatteryInfo.setText(getCurrentBatteryCurrentByBS() + "mA");
                    break;
                default:
                    break;
            }

            super.handleMessage(msg);
        }

    };

    /**
     * 打开线程timeThread
     *
     * @param timeThread
     */
    private void startTimeThread(Thread timeThread) {
        if (timeThread == null) {
            runtime = true;
            timeThread = new TimeThread();
            timeThread.start();
        }
    }

    /**
     * 停止线程
     *
     * @param timeThread
     */
    private void stopTimeThread(Thread timeThread) {
        if (timeThread != null) {
            Thread.currentThread().interrupt();
            timeThread.interrupt();
            runtime = false;
        }

    }

    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
        // TODO 这边需要将对应的异常缓存起来，下次启动的时候，显示到页面中欧给
        logException(throwable);
    }

    class TimeThread extends Thread {
        @Override
        public void run() {
            while (runtime) {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Message msg = new Message();
                msg.what = REFRESH_TIME;
                eventHandler.sendMessage(msg);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                monitorLocation();
            }
        }
    }
    private int samplingCount = 0;
    private int samplingMACount = 0;
    private int isMA = -1;

    private double getCurrentBatteryCurrentByBS() {
        double current = Math.abs(bm.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW));
        if (samplingCount < 20){
            samplingCount++;
            if (Double.valueOf(current).intValue() < 2000){
                samplingMACount++;
            }
        }else{
            if (isMA == -1){
                isMA = samplingMACount > 10 ? 1: 0;
            }
        }
        if (isMA == 0 || (isMA == -1 && Double.valueOf(current).intValue() >= 2000)) {
            current /= 1000;
        }
        return current;
    }

    private String getAndroidID() {
        return Settings.System.getString(
                getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID
        );
    }

    // 监控位置坐标变化
    private void monitorLocation() {
        // 获取当前位置管理器
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // 启动位置请求
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            // 申请权限
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 100);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currBatteryInfo.setLatitude(String.valueOf(location.getLatitude()));
                currBatteryInfo.setLongitude(String.valueOf(location.getLongitude()));
                mLatitude_BatteryInfo.setText(currBatteryInfo.getLatitude());
                mLongitude_BatteryInfo.setText(currBatteryInfo.getLongitude());
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        if (null != scheduledExecutorService) {
            scheduledExecutorService.shutdownNow();
        }
        unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }

    private void logException(Throwable e) {
        runOnUiThread(() -> mExceptionLog.append("\n" + e.toString()));
    }

    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private void logException(String msg) {

        runOnUiThread(() -> mExceptionLog.append("\n\n[" + format.format(new Date()) + "]" + msg));
    }
}

