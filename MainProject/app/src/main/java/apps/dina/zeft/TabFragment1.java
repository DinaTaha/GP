package apps.dina.zeft;

/**
 * Created by Dina on 12/6/2016.
 */


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.widget.TextView;
import android.widget.Button;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.location.LocationManager;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;
import static android.content.Context.SENSOR_SERVICE;

public class TabFragment1 extends Fragment implements SensorEventListener {
    Button btnShowLocation;
    private TextView tv;
    private TextView tv_2;
    private TextView tv_3;
    private TextView tv_4;


    long lastTime;
    SensorManager sm;
    GPSTracker gps;
    MyService gps2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_fragment_1, container, false);
       /* Log.v("Debug", "Activity started..");
        Intent myIntent=new Intent(this,MyService.class);
        getActivity().startService(myIntent); */

       // Log.v("oncreate","app is started");

        tv = (TextView) view.findViewById(R.id.ElapsedTimeText);
        tv_2 = (TextView) view.findViewById(R.id.StrokeRateText);
        tv_3 = (TextView) view.findViewById(R.id.SpeedText);
        tv_4 = (TextView) view.findViewById(R.id.DistanceText);

        btnShowLocation = (Button) view.findViewById(R.id.Start);
        btnShowLocation.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // create class object
                gps = new GPSTracker(getActivity());
                gps2 = new MyService();


                // check if GPS enabled
                if(gps.canGetLocation()){
                    tv_2.setText(String.valueOf(gps.getLatitude())+" "+String.valueOf(gps.getLongitude()));
                    //tv_3.setText(String.valueOf(gps2.getspeed()));
                    //tv_4.setText(String.valueOf(gps2.getRealDistance()));

                }
                else{
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    gps.showSettingsAlert();
                }
            }
        });
        lastTime = System.currentTimeMillis();
        sm = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        return view;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(sensorEvent);
        }
        /*else if(sensorEvent.sensor.getType() == Sensor.TYPE_){
            //call another function
        }*/
    }

    private void getAccelerometer(SensorEvent event) {

        float[] value = event.values;

        float x = value[0];
        float y = value[1];
        float z = value[2];

        float accelationSquareRoot = (x*x + y*y + z*z)
                / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);

        long actualTime = System.currentTimeMillis();

        if(accelationSquareRoot >= 1) {

            if(actualTime-lastTime < 100) {

                return;
            }

            final long time=actualTime-lastTime;
            // Perform your Action Here..

            // btn.setOnClickListener(new View.OnClickListener() {
            // @Override
            // public void onClick(View v) {

            String ConvertedTime = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(time),
                    TimeUnit.MILLISECONDS.toMinutes(time) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time)),
                    TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time)));
            tv.setText(String.valueOf(ConvertedTime));


            // }
            //});

        }
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        sm.unregisterListener(this);
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        sm.registerListener(this, sm.getDefaultSensor
                (Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }
}

// display clock
class AndroidTextClock extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_fragment_1);
    }
}

//location
class GPSTracker extends Service implements LocationListener {

    private final Context mContext;

    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    // flag for GPS status
    boolean canGetLocation = false;

    Location location; // location
    double latitude; // latitude
    double longitude; // longitude
    double Speed;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    // Declaring a Location Manager
    protected LocationManager locationManager;

    public GPSTracker(Context context) {
        this.mContext = context;
        getLocation();
    }

    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     * */
    public void stopUsingGPS(){
        if(locationManager != null){
            locationManager.removeUpdates(GPSTracker.this);
        }
    }

    /**
     * Function to get latitude
     * */
    public double getLatitude(){
        if(location != null){
            latitude = location.getLatitude();
        }

        // return latitude
        return latitude;
    }

    /**
     * Function to get longitude
     * */
    public double getLongitude(){
        if(location != null){
            longitude = location.getLongitude();
        }

        // return longitude
        return longitude;
    }

   /* public double getSpeed(){
        if(location != null){
            Speed = location.getSpeed();
        }

        // return latitude
        return Speed;
    }
*/

    /**
     * Function to check GPS/wifi enabled
     * @return boolean
     * */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     * */
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }


    @Override
    public void onLocationChanged(Location location) { }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

}

//Speed, Location & Distance

class MyService extends Service
{
    private LocationManager locManager;
    private LocationListener locListener = new myLocationListener();
    static final Double EARTH_RADIUS = 6371.00;
    public double distance;
    public double speed;
    private boolean gps_enabled = false;
    private boolean network_enabled = false;
    //public TextView tv_3;
    //public TextView tv_4;

    private Handler handler = new Handler();
    Thread t;

    @Override
    public IBinder onBind(Intent intent) {return null;}
    @Override
    public void onCreate() {}
    @Override
    public void onDestroy() {}
    @Override
    public void onStart(Intent intent, int startid) {}
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        Toast.makeText(getBaseContext(), "Service Started", Toast.LENGTH_SHORT).show();

        final Runnable r = new Runnable()
        {   public void run()
        {
            Log.v("Debug", "Hello");
            location();
            handler.postDelayed(this, 5000);
        }
        };
        handler.postDelayed(r, 5000);
        return START_STICKY;
    }

    public void location(){
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        try{
            gps_enabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }
        catch(Exception ex){}
        try{
            network_enabled = locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }
        catch(Exception ex){}
        Log.v("Debug", "in on create.. 2");
        if (gps_enabled) {
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locListener);
            Log.v("Debug", "Enabled..");
        }
        if (network_enabled) {
            locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,locListener);
            Log.v("Debug", "Disabled..");
        }
        Log.v("Debug", "in on create..3");
    }

    private class myLocationListener implements LocationListener
    {
        double lat_old=0.0;
        double lon_old=0.0;
        double OldTime=System.currentTimeMillis();
        double lat_new;
        double lon_new;

        //double time=10;
       // double speed=0.0;
        //final double FirstDistance =CalculationByDistance(lat_new, lon_new, lat_old, lat_old);
       // double RealDistance;

        //Button btnShowLocation;

        @Override
        public void onLocationChanged( Location location) {
            Log.v("Debug", "in onLocation changed..");
            if(location!=null){
                locManager.removeUpdates(locListener);
                //String Speed = "Device Speed: " +location.getSpeed();

                lat_new=location.getLongitude();
                lon_new =location.getLatitude();

                String longitude = "Longitude: " +location.getLongitude();
                String latitude = "Latitude: " +location.getLatitude();
                distance =distance_on_geoid(lat_old, lon_old, lat_new, lon_new);
                double NewTime=System.currentTimeMillis();
                double Time= (NewTime-OldTime)/1000;
               // double LastDistance;

            //    RealDistance = FirstDistance + distance;
                speed = distance/Time;
                //LastDistance=+distance;
                //RealDistance=FirstDistance + distance;
                Log.e("distance = ",distance+"");

               // tv_3.setText(String.valueOf(speed));
                //tv_4.setText(String.valueOf(distance));


                Toast.makeText(getApplicationContext(), longitude+"\n"+latitude+"\nDistance is: "
                        +distance+"\nSpeed is: "+speed , Toast.LENGTH_SHORT).show();

                lat_old=lat_new;
                lon_old=lon_new;
                OldTime=NewTime;
            }
        }



        @Override
        public void onProviderDisabled(String provider) {}
        @Override
        public void onProviderEnabled(String provider) {}
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }



    double distance_on_geoid(double lat1, double lon1, double lat2, double lon2) {

        // Convert degrees to radians
        double M_PI=3.14159265358979300000;
        lat1 = lat1 * M_PI / 180.0;
        lon1 = lon1 * M_PI / 180.0;

        lat2 = lat2 * M_PI / 180.0;
        lon2 = lon2 * M_PI / 180.0;

        // radius of earth in metres
        double r = 6378100;

        // P
        double rho1 = r * Math.cos(lat1);
        double z1 = r * Math.sin(lat1);
        double x1 = rho1 * Math.cos(lon1);
        double y1 = rho1 * Math.sin(lon1);

        // Q
        double rho2 = r * Math.cos(lat2);
        double z2 = r * Math.sin(lat2);
        double x2 = rho2 * Math.cos(lon2);
        double y2 = rho2 * Math.sin(lon2);

        // Dot product
        double dot = (x1 * x2 + y1 * y2 + z1 * z2);
        double cos_theta = dot / (r * r);

        double theta = Math.acos(cos_theta);

        // Distance in Metres
        return r * theta;
    }
}

