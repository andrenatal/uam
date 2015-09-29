package com.uam.anatal.uam;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.provider.SyncStateContract;
import android.speech.RecognizerIntent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog.Builder;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity  {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    List<Address> addresses = null;
    ArrayList<String> matches_text;
    SQLiteDatabase db;
    Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        db=openOrCreateDatabase("PlacesDB", Context.MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS place(place VARCHAR,lat VARCHAR,lng VARCHAR);");
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        geocoder = new Geocoder(this, Locale.getDefault());
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();

            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

    }

    protected synchronized void buildGoogleApiClient(String lugar) {
        try{
            geocoder = new Geocoder(this, Locale.getDefault());
            addresses = geocoder.getFromLocationName(lugar,1);

            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();


            LatLng posicao = new LatLng(address.getLatitude() , address.getLongitude());

            mMap.setMyLocationEnabled(true);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posicao, 13));

            mMap.addMarker(new MarkerOptions()
                    .title(lugar)
                    .snippet(address.getAddressLine(0))
                    .position(posicao));

            db.execSQL("INSERT INTO place VALUES('" + lugar + "','" +
                    address.getLatitude() + "','" +  address.getLongitude() + "');");


        } catch (Exception exc){

        }

    }

    public void list(View view){


        Cursor c=db.rawQuery("SELECT * FROM place", null);
        if(c.getCount()==0)
        {
            showMessage("Error", "No records found");
            return;
        }


        final String test[] = new String[c.getCount()];
        int itens = 0;
        while(c.moveToNext())
        { /*
            buffer.append("Rollno: "+c.getString(0)+"\n");
            buffer.append("Name: "+c.getString(1)+"\n");
            buffer.append("Marks: "+c.getString(2)+"\n\n"); */
            test[itens] =  c.getString(0);
            itens++;

        }


        ListView lv1 = new ListView(this);
        lv1.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, test));
        lv1.setClickable(false);
        Builder builder=new Builder(this);
        builder.setView(lv1);
        final AlertDialog alert = builder.create();
        alert.show();

        lv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // selected item
                //String selected = ((TextView) view.findViewById(R.id.your_textView_item_id)).getText().toString();

                try {
                    addresses = geocoder.getFromLocationName(test[position], 1);
                } catch (Exception exc) {
                    showMessage("Erro", "Algum erro ocorreu:" + exc.getMessage());
                    return;
                }
                Address address = addresses.get(0);
                ArrayList<String> addressFragments = new ArrayList<String>();


                LatLng posicao = new LatLng(address.getLatitude(), address.getLongitude());

                mMap.setMyLocationEnabled(true);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posicao, 13));

                mMap.addMarker(new MarkerOptions()
                        .title(test[position])
                        .snippet(address.getAddressLine(0))
                        .position(posicao));

                Toast toast = Toast.makeText(getApplicationContext(), test[position], Toast.LENGTH_SHORT);
                toast.show();
                alert.hide();
            }
        });

    }

    public void showMessage(String title,String message)
    {
        Builder builder=new Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

    public void about(View view) {

            AlertDialog.Builder helpBuilder = new AlertDialog.Builder(this);
            helpBuilder.setTitle("Voice Place Finder");
            helpBuilder.setMessage("Desenvolvido por André Luiz Pontes Natal\nRA: 20472356 ");
            helpBuilder.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            // Do nothing but close the dialog
                        }
                    });

            // Remember, create doesn't show the dialog
            AlertDialog helpDialog = helpBuilder.create();
            helpDialog.show();

    }

    public void speak(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, 1234);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1234 && resultCode == RESULT_OK) {

            matches_text = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            Toast.makeText(getApplicationContext(), matches_text.get(0),
                    Toast.LENGTH_LONG).show();

            buildGoogleApiClient(matches_text.get(0));



        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
