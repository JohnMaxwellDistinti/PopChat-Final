package com.google.firebase.codelab.friendlychat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.content.Context.LOCATION_SERVICE;

public class MessagesFragment extends Fragment implements LocationListener{
    protected LocationManager locationManager;
    static final int MY_LOCATION_REQUEST_CODE = 1;
    protected LocationListener locationListener;
    protected Context context;
    final double VISIBILITY_RADIUS = 0.002;
    static ArrayList<Beacon> tempList = new ArrayList<>();
    final List<Beacon> beaconList = new ArrayList<>();
    ArrayAdapter<Beacon> arrayAdapter;
    TextView txtLat, txtLong;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
            }else{
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_LOCATION_REQUEST_CODE);
            }
        }

        Button button = view.findViewById(R.id.startMessage);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MessageActivity.class);
                startActivity(intent);
            }
        });
        arrayAdapter = new ArrayAdapter<>(
                view.getContext(), // reference to the current activity
                android.R.layout.simple_list_item_1, // layout for each row in the list view (item in the data source)
                beaconList // data source
        );
        ListView lv = view.findViewById(R.id.beaconList);
        lv.setAdapter(arrayAdapter);


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //set editing flag and start editing selected note
                enterChat(i);
            }
        });

        Beacon hemmingson = new Beacon("Hemmingson",
                "The hub of Gonzaga University's campus",
                47.667136,
                -117.399103);
        Beacon herak = new Beacon("Herak",
                "The primary building of Gonzaga University's School of Engineering",
                47.666745,
                -117.402179);
        Beacon paccar = new Beacon("Paccar",
                "The primary building of Gonzaga University's School of Applied Science",
                47.666367,
                -117.402161);
        Beacon jundt = new Beacon("Jundt",
                "The art museum building on Gonzaga University's campus",
                47.666363,
                -117.406914);
        Beacon arcOfSpokane = new Beacon("The Arc of Spokane",
                "A consignment store for the Spokane community",
                47.665083,
                -117.409781);
        Beacon Dooley = new Beacon("Dooley",
                "Gonzaga University residence hall",
                47.669187,
                -117.405437);
        tempList.add(hemmingson);
        tempList.add(herak);
        tempList.add(paccar);
        tempList.add(jundt);
        tempList.add(arcOfSpokane);
        tempList.add(Dooley);

        return view;
    }
    public void enterChat(int index){
        //Set intent values and flag for editing a present note
        Beacon beacon = beaconList.get(index);
        Intent i = new Intent(getActivity(), MessageActivity.class);
        i.putExtra("title", beacon.getTitle());
        i.putExtra("description", beacon.getDescription());
        i.putExtra("latitude", beacon.getLatitude());
        i.putExtra("longitude", beacon.getLongitude());
        startActivityForResult(i,0);
    }

    @Override
    public void onLocationChanged(Location location){
        //txtLat.setText("Latitude:" + location.getLatitude());
        //txtLong.setText("Longitude:" + location.getLongitude());
        //Log.d("Distance",Double.toString(calcualteUserBeaconDistance(location.getLatitude(), location.getLongitude(), tempList.get(0))));
        //Add beacons in range
        try {
            ProgressBar pb = getActivity().findViewById(R.id.loadingBar);
            pb.setVisibility(ProgressBar.GONE);
        }catch(Exception e){

        }
        for(int i = 0; i < tempList.size(); i++){
            Beacon tempBeacon = tempList.get(i);
            if(calcualteUserBeaconDistance(location.getLatitude(), location.getLongitude(), tempBeacon) <= VISIBILITY_RADIUS){
                if(!beaconList.contains(tempBeacon)) {
                    beaconList.add(tempBeacon);
                    arrayAdapter.notifyDataSetChanged();
                }
            }
        }
        //Clear out beacons out of range
        for(int i = 0; i<beaconList.size(); i++){
            Beacon tempBeacon = beaconList.get(i);
            if(calcualteUserBeaconDistance(location.getLatitude(), location.getLongitude(), tempBeacon) >= VISIBILITY_RADIUS){
                if(beaconList.contains(tempBeacon)) {
                    beaconList.remove(tempBeacon);
                    arrayAdapter.notifyDataSetChanged();
                }
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // this callback executes when the user has made a choice on the alert dialog
        // allow or deny
        if (requestCode == MY_LOCATION_REQUEST_CODE) {
            if (permissions.length == 1 &&
                    permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // we got the user's permission (finally)
            }
        }

    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude","disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude","enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude", "status");
    }

    public double calculateEuclidianDistance(double lat1, double long1, double lat2, double long2){
        double distance = Math.sqrt(((lat2 - lat1)*(lat2 - lat1))+((long2-long1)*(long2-long1)));
        return distance;
    }

    public double calcualteUserBeaconDistance(double userLat, double userLong, Beacon beacon){
        double beaconDistance = calculateEuclidianDistance(userLat,
                userLong,
                beacon.getLatitude(),
                beacon.getLongitude());
        return beaconDistance;
    }
    public double calculateBeaconBeaconDistance(Beacon beacon1, Beacon beacon2){
        double beaconDistance = calculateEuclidianDistance(
                beacon1.getLatitude(),
                beacon1.getLongitude(),
                beacon2.getLatitude(),
                beacon2.getLongitude());
        return beaconDistance;
    }
}