package com.example.hua.coolweather;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.hua.coolweather.ui.MyLetterListView.OnTouchingLetterChangedListener;
import com.example.hua.coolweather.model.CityInfo;
import com.example.hua.coolweather.ui.MyLetterListView;
import com.example.hua.coolweather.until.LogUntil;
import com.example.hua.coolweather.until.PingYinUtil;
import com.example.hua.coolweather.db.DatabaseHelper;
import com.example.hua.coolweather.db.DBHelper;

public class ChooseCityActivity extends AppCompatActivity implements OnScrollListener {
    private BaseAdapter adapter;
    private ResultListAdapter resultListAdapter;
    private ListView personList;
    private ListView resultList;
    private TextView overlay; // 对话框首字母textview
    private MyLetterListView letterListView; // A-Z listview
    private HashMap<String, Integer> alphaIndexer;// 存放存在的汉语拼音首字母和与之对应的列表位置
    private Handler handler;
    private OverlayThread overlayThread; // 显示首字母对话框
    private ArrayList<CityInfo> allCity_lists; // 所有城市列表
    private ArrayList<CityInfo> city_hot;
    private ArrayList<CityInfo> city_result;
    private ArrayList<String> city_history;
    private TextView tv_noresult;

    private LocationClient mLocationClient;
    private WindowManager windowManager;
    private String currentCity; // 用于保存定位到的城市
    private int locateProcess = 1; // 记录当前定位的状态 正在定位-定位成功-定位失败
    private boolean isNeedFresh;
    private DatabaseHelper helper;
    /**
     * 判断是否从WeatherActivity 中跳转过来。
     */
    private boolean isFromWeatherActivity;

    //用于权限判断
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE = 1;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE = 2;
    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE = 3;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL = 4;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);
        LogUntil.w("coolweather","ChooseCityActivity onCreate");
        if (preferences.getBoolean("city_selected", false) && !isFromWeatherActivity) {
            Intent intent = new Intent(this, WeatherActivity.class);
            startActivity(intent);
            if(mLocationClient.isStarted()){
                mLocationClient.stop();
            }
            if(overlay.getParent()!=null) {
                windowManager.removeView(overlay);
            }
            finish();
        }
        setContentView(R.layout.main);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        EditText sh;//搜索输入框
        personList = (ListView) findViewById(R.id.list_view);
        allCity_lists = new ArrayList<>();
        city_hot = new ArrayList<>();
        city_result = new ArrayList<>();
        city_history = new ArrayList<>();
        resultList = (ListView) findViewById(R.id.search_result);
        sh = (EditText) findViewById(R.id.sh);
        tv_noresult = (TextView) findViewById(R.id.tv_noresult);
        helper = new DatabaseHelper(this);
        sh.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (s.toString() == null || "".equals(s.toString())) {
                    letterListView.setVisibility(View.VISIBLE);
                    personList.setVisibility(View.VISIBLE);
                    resultList.setVisibility(View.GONE);
                    tv_noresult.setVisibility(View.GONE);
                } else {
                    city_result.clear();
                    letterListView.setVisibility(View.GONE);
                    personList.setVisibility(View.GONE);
                    getResultCityList(s.toString());
                    if (city_result.size() <= 0) {
                        tv_noresult.setVisibility(View.VISIBLE);
                        resultList.setVisibility(View.GONE);
                    } else {
                        tv_noresult.setVisibility(View.GONE);
                        resultList.setVisibility(View.VISIBLE);
                        resultListAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        letterListView = (MyLetterListView) findViewById(R.id.MyLetterListView01);
        letterListView
                .setOnTouchingLetterChangedListener(new LetterListViewListener());
        alphaIndexer = new HashMap<>();
        handler = new Handler();
        overlayThread = new OverlayThread();
        isNeedFresh = true;
        personList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (position >= 4) {
                    toIntent(allCity_lists.get(position).getName());
                }
            }
        });
        locateProcess = 1;
        personList.setAdapter(adapter);
        personList.setOnScrollListener(this);
        resultListAdapter = new ResultListAdapter(this, city_result);
        resultList.setAdapter(resultListAdapter);
        resultList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                toIntent(city_result.get(position).getName());
            }
        });
        checkPermission();
        initOverlay();
        cityInit();
        hotCityInit();
        hisCityInit();
        setAdapter(allCity_lists, city_hot, city_history);

        mLocationClient = new LocationClient(this.getApplicationContext());
        MyLocationListener mMyLocationListener;
        mMyLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(mMyLocationListener);
        InitLocation();
        mLocationClient.start();
    }

    /**
     * 根据选定的城市，跳转到相应的天气预报界面
     *
     * @param countyCode 天气代码
     */
    private void toIntent(String countyCode) {
        Intent intent = new Intent(ChooseCityActivity.this, WeatherActivity.class);
        intent.putExtra("county_code", countyCode);
        startActivity(intent);
        if(mLocationClient.isStarted()){
            mLocationClient.stop();
        }
        if(overlay.getParent()!=null) {
            windowManager.removeView(overlay);
        }
        finish();
    }

    private void checkPermission() {
        //网络定位
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_COARSE);
        }
        //gps定位
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE);
        }
        //读取手机状态
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE},
                    MY_PERMISSIONS_REQUEST_READ_PHONE);
        }
        //读写存储卡
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        //处理权限请求
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_COARSE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "授权请求被拒绝了", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        //处理权限请求
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "授权请求被拒绝了", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        //处理权限请求
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_PHONE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "授权请求被拒绝了", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        //处理权限请求
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "授权请求被拒绝了", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void InsertCity(String name) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from recentcity where name = '"
                + name + "'", null);
        if (cursor.getCount() > 0) { //
            db.delete("recentcity", "name = ?", new String[]{name});
        }
        db.execSQL("insert into recentcity(name, date) values('" + name + "', "
                + System.currentTimeMillis() + ")");
        db.close();
    }

    private void InitLocation() {
        // 设置定位参数
        LocationClientOption mOption = new LocationClientOption();
        mOption = new LocationClientOption();
        mOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        mOption.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
        mOption.setScanSpan(3000);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        mOption.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        mOption.setIsNeedLocationDescribe(true);//可选，设置是否需要地址描述
        mOption.setNeedDeviceDirect(false);//可选，设置是否需要设备方向结果
        mOption.setLocationNotify(false);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        mOption.setIgnoreKillProcess(true);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        mOption.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        mOption.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        mOption.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集

        mOption.setIsNeedAltitude(false);//可选，默认false，设置定位时是否需要海拔信息，默认不需要，除基础定位版本都可用
        mLocationClient.setLocOption(mOption);
    }

    private void cityInit() {
        ArrayList<CityInfo> city_lists; //城市列表
        CityInfo cityInfo = new CityInfo("定位", "0"); // 当前定位城市
        allCity_lists.add(cityInfo);
        cityInfo = new CityInfo("最近", "1"); // 最近访问的城市
        allCity_lists.add(cityInfo);
        cityInfo = new CityInfo("热门", "2"); // 热门城市
        allCity_lists.add(cityInfo);
        cityInfo = new CityInfo("全部", "3"); // 全部城市
        allCity_lists.add(cityInfo);
        city_lists = getCityList();
        allCity_lists.addAll(city_lists);
    }

    /**
     * 热门城市
     */
    public void hotCityInit() {
        CityInfo cityInfo = new CityInfo("上海", "2");
        city_hot.add(cityInfo);
        cityInfo = new CityInfo("北京", "2");
        city_hot.add(cityInfo);
        cityInfo = new CityInfo("广州", "2");
        city_hot.add(cityInfo);
        cityInfo = new CityInfo("深圳", "2");
        city_hot.add(cityInfo);
        cityInfo = new CityInfo("武汉", "2");
        city_hot.add(cityInfo);
        cityInfo = new CityInfo("天津", "2");
        city_hot.add(cityInfo);
        cityInfo = new CityInfo("西安", "2");
        city_hot.add(cityInfo);
        cityInfo = new CityInfo("南京", "2");
        city_hot.add(cityInfo);
        cityInfo = new CityInfo("杭州", "2");
        city_hot.add(cityInfo);
        cityInfo = new CityInfo("成都", "2");
        city_hot.add(cityInfo);
        cityInfo = new CityInfo("重庆", "2");
        city_hot.add(cityInfo);
    }

    private void hisCityInit() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "select * from recentcity order by date desc limit 0, 3", null);
        while (cursor.moveToNext()) {
            city_history.add(cursor.getString(1));
        }
        cursor.close();
        db.close();
    }

    @SuppressWarnings("unchecked")
    private ArrayList<CityInfo> getCityList() {
        DBHelper dbHelper = new DBHelper(this);
        ArrayList<CityInfo> list = new ArrayList<CityInfo>();
        try {
            dbHelper.createDataBase();
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Cursor cursor = db.rawQuery("select * from city", null);
            CityInfo cityInfo;
            while (cursor.moveToNext()) {
                cityInfo = new CityInfo(cursor.getString(1), cursor.getString(2));
                list.add(cityInfo);
            }
            cursor.close();
            db.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.sort(list, comparator);
        return list;
    }

    @SuppressWarnings("unchecked")
    private void getResultCityList(String keyword) {
        DBHelper dbHelper = new DBHelper(this);
        try {
            dbHelper.createDataBase();
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Cursor cursor = db.rawQuery(
                    "select * from city where name like \"%" + keyword
                            + "%\" or pinyin like \"%" + keyword + "%\"", null);
            CityInfo cityInfo;
            Log.e("info", "length = " + cursor.getCount());
            while (cursor.moveToNext()) {
                cityInfo = new CityInfo(cursor.getString(1), cursor.getString(2));
                city_result.add(cityInfo);
            }
            cursor.close();
            db.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.sort(city_result, comparator);
    }

    /**
     * a-z排序
     */
    @SuppressWarnings("rawtypes")
    Comparator comparator = new Comparator<CityInfo>() {
        @Override
        public int compare(CityInfo lhs, CityInfo rhs) {
            String a = lhs.getPinyi().substring(0, 1);
            String b = rhs.getPinyi().substring(0, 1);
            int flag = a.compareTo(b);
            if (flag == 0) {
                return a.compareTo(b);
            } else {
                return flag;
            }
        }
    };

    private void setAdapter(List<CityInfo> list, List<CityInfo> hotList,
                            List<String> hisCity) {
        adapter = new ListAdapter(this, list, hotList, hisCity);
        personList.setAdapter(adapter);
    }

    /**
     * 实现实位回调监听
     */
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation arg0) {
            Log.e("info", "city = " + arg0.getAddrStr());
            if (!isNeedFresh) {
                return;
            }
            isNeedFresh = false;
            if (arg0.getCity() == null) {
                locateProcess = 3; // 定位失败
                personList.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                return;
            }
            currentCity = arg0.getCity().substring(0,
                    arg0.getCity().length() - 1);
            locateProcess = 2; // 定位成功
            personList.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }

    private class ResultListAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private ArrayList<CityInfo> results = new ArrayList<CityInfo>();

        public ResultListAdapter(Context context, ArrayList<CityInfo> results) {
            inflater = LayoutInflater.from(context);
            this.results = results;
        }

        @Override
        public int getCount() {
            return results.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_item, null);
                viewHolder = new ViewHolder();
                viewHolder.name = (TextView) convertView
                        .findViewById(R.id.name);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.name.setText(results.get(position).getName());
            return convertView;
        }

        class ViewHolder {
            TextView name;
        }
    }

    public class ListAdapter extends BaseAdapter {
        private Context context;
        private LayoutInflater inflater;
        private List<CityInfo> list;
        private List<CityInfo> hotList;
        private List<String> hisCity;
        final int VIEW_TYPE = 5;

        public ListAdapter(Context context, List<CityInfo> list,
                           List<CityInfo> hotList, List<String> hisCity) {
            this.inflater = LayoutInflater.from(context);
            this.list = list;
            this.context = context;
            this.hotList = hotList;
            this.hisCity = hisCity;
            alphaIndexer = new HashMap<String, Integer>();
            String[] sections = new String[list.size()]; //存放存在的汉语拼音首字母
            for (int i = 0; i < list.size(); i++) {
                // 当前汉语拼音首字母
                String currentStr = getAlpha(list.get(i).getPinyi());
                // 上一个汉语拼音首字母，如果不存在为" "
                String previewStr = (i - 1) >= 0 ? getAlpha(list.get(i - 1)
                        .getPinyi()) : " ";
                if (!previewStr.equals(currentStr)) {
                    String name = getAlpha(list.get(i).getPinyi());
                    alphaIndexer.put(name, i);
                    sections[i] = name;
                }
            }
        }

        @Override
        public int getViewTypeCount() {
            return VIEW_TYPE;
        }

        @Override
        public int getItemViewType(int position) {
            return position < 4 ? position : 4;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        ViewHolder holder;

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final TextView city;
            int viewType = getItemViewType(position);
            if (viewType == 0) { // 定位
                convertView = inflater.inflate(R.layout.frist_list_item, null);
                TextView locateHint = (TextView) convertView
                        .findViewById(R.id.locateHint);
                city = (TextView) convertView.findViewById(R.id.lng_city);
                city.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (locateProcess == 2) {
                            toIntent(city.getText().toString());
                        } else if (locateProcess == 3) {
                            locateProcess = 1;
                            personList.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                            mLocationClient.stop();
                            isNeedFresh = true;
                            InitLocation();
                            currentCity = "";
                            mLocationClient.start();
                        }
                    }
                });
                ProgressBar pbLocate = (ProgressBar) convertView
                        .findViewById(R.id.pbLocate);
                if (locateProcess == 1) { // 正在定位
                    locateHint.setText("正在定位");
                    city.setVisibility(View.GONE);
                    pbLocate.setVisibility(View.VISIBLE);
                } else if (locateProcess == 2) { // 定位成功
                    locateHint.setText("当前定位城市");
                    city.setVisibility(View.VISIBLE);
                    city.setText(currentCity);
                    mLocationClient.stop();
                    pbLocate.setVisibility(View.GONE);
                } else if (locateProcess == 3) {
                    locateHint.setText("未定位到城市,请选择");
                    city.setVisibility(View.VISIBLE);
                    city.setText("重新选择");
                    pbLocate.setVisibility(View.GONE);
                }
            } else if (viewType == 1) { // 最近访问城市
                convertView = inflater.inflate(R.layout.recent_city, null);
                GridView rencentCity = (GridView) convertView
                        .findViewById(R.id.recent_city);
                rencentCity
                        .setAdapter(new HitCityAdapter(context, this.hisCity));
                rencentCity.setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        toIntent(city_history.get(position));

                    }
                });
                TextView recentHint = (TextView) convertView
                        .findViewById(R.id.recentHint);
                recentHint.setText("最近访问的城市");
            } else if (viewType == 2) {
                convertView = inflater.inflate(R.layout.recent_city, null);
                GridView hotCity = (GridView) convertView
                        .findViewById(R.id.recent_city);
                hotCity.setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {

                        toIntent(city_hot.get(position).getName());

                    }
                });
                hotCity.setAdapter(new HotCityAdapter(context, this.hotList));
                TextView hotHint = (TextView) convertView
                        .findViewById(R.id.recentHint);
                hotHint.setText("热门城市");
            } else if (viewType == 3) {
                convertView = inflater.inflate(R.layout.total_item, null);
            } else {
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.list_item, null);
                    holder = new ViewHolder();
                    holder.alpha = (TextView) convertView
                            .findViewById(R.id.alpha);
                    holder.name = (TextView) convertView
                            .findViewById(R.id.name);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                if (position >= 1) {
                    holder.name.setText(list.get(position).getName());
                    String currentStr = getAlpha(list.get(position).getPinyi());
                    String previewStr = (position - 1) >= 0 ? getAlpha(list
                            .get(position - 1).getPinyi()) : " ";
                    if (!previewStr.equals(currentStr)) {
                        holder.alpha.setVisibility(View.VISIBLE);
                        holder.alpha.setText(currentStr);
                    } else {
                        holder.alpha.setVisibility(View.GONE);
                    }
                }
            }
            return convertView;
        }

        private class ViewHolder {
            TextView alpha; // 首字母标题
            TextView name; // 城市名字
        }
    }

    @Override
    protected void onStop() {
        if(mLocationClient.isStarted()){
            mLocationClient.stop();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if(overlay.getParent()!=null) {
            windowManager.removeView(overlay);
        }
        LogUntil.w("coolweather","onDestroy");
        super.onDestroy();
    }

    class HotCityAdapter extends BaseAdapter {
        private Context context;
        private LayoutInflater inflater;
        private List<CityInfo> hotCitys;

        public HotCityAdapter(Context context, List<CityInfo> hotCitys) {
            this.context = context;
            inflater = LayoutInflater.from(this.context);
            this.hotCitys = hotCitys;
        }

        @Override
        public int getCount() {
            return hotCitys.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = inflater.inflate(R.layout.item_city, null);
            TextView city = (TextView) convertView.findViewById(R.id.city);
            city.setText(hotCitys.get(position).getName());
            return convertView;
        }
    }

    class HitCityAdapter extends BaseAdapter {
        private Context context;
        private LayoutInflater inflater;
        private List<String> hotCitys;

        public HitCityAdapter(Context context, List<String> hotCitys) {
            this.context = context;
            inflater = LayoutInflater.from(this.context);
            this.hotCitys = hotCitys;
        }

        @Override
        public int getCount() {
            return hotCitys.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = inflater.inflate(R.layout.item_city, null);
            TextView city = (TextView) convertView.findViewById(R.id.city);
            city.setText(hotCitys.get(position));
            return convertView;
        }
    }

    private boolean mReady;

    // 初始化汉语拼音首字母弹出提示框
    private void initOverlay() {
        mReady = true;
        LayoutInflater inflater = LayoutInflater.from(this);
        overlay = (TextView) inflater.inflate(R.layout.overlay, null);
        overlay.setVisibility(View.INVISIBLE);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);
        windowManager = (WindowManager) this
                .getSystemService(Context.WINDOW_SERVICE);
        windowManager.addView(overlay, lp);
    }

    private boolean isScroll = false;

    private class LetterListViewListener implements
            OnTouchingLetterChangedListener {

        @Override
        public void onTouchingLetterChanged(final String s) {
            isScroll = false;
            if (alphaIndexer.get(s) != null) {
                int position = alphaIndexer.get(s);
                personList.setSelection(position);
                overlay.setText(s);
                overlay.setVisibility(View.VISIBLE);
                handler.removeCallbacks(overlayThread);
                // 延迟一秒后执行，让overlay为不可见
                handler.postDelayed(overlayThread, 1000);
            }
        }
    }

    // 设置overlay不可见
    private class OverlayThread implements Runnable {
        @Override
        public void run() {
            overlay.setVisibility(View.GONE);
        }
    }

    // 获得汉语拼音首字母
    private String getAlpha(String str) {
        if (str == null) {
            return "#";
        }
        if (str.trim().length() == 0) {
            return "#";
        }
        char c = str.trim().substring(0, 1).charAt(0);
        // 正则表达式，判断首字母是否是英文字母
        Pattern pattern = Pattern.compile("^[A-Za-z]+$");
        if (pattern.matcher(c + "").matches()) {
            return (c + "").toUpperCase();
        } else if (str.equals("0")) {
            return "定位";
        } else if (str.equals("1")) {
            return "最近";
        } else if (str.equals("2")) {
            return "热门";
        } else if (str.equals("3")) {
            return "全部";
        } else {
            return "#";
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_TOUCH_SCROLL
                || scrollState == SCROLL_STATE_FLING) {
            isScroll = true;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        if (!isScroll) {
            return;
        }

        if (mReady) {
            String text;
            String name = allCity_lists.get(firstVisibleItem).getName();
            String pinyin = allCity_lists.get(firstVisibleItem).getPinyi();
            if (firstVisibleItem < 4) {
                text = name;
            } else {
                text = PingYinUtil.converterToFirstSpell(pinyin)
                        .substring(0, 1).toUpperCase();
            }
            overlay.setText(text);
            overlay.setVisibility(View.VISIBLE);
            handler.removeCallbacks(overlayThread);
            // 延迟一秒后执行，让overlay为不可见
            handler.postDelayed(overlayThread, 1000);
        }
    }

    /**
     * 捕获back按键 根据当前状态来判断，应该反馈 哪一级列表 或者直接退出
     */
    @Override
    public void onBackPressed() {

        if (isFromWeatherActivity) {
            Intent intent = new Intent(this, WeatherActivity.class);
            startActivity(intent);
        }
        finish();
    }

}