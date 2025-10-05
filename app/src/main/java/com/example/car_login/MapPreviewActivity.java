package your.pkg;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.Arrays;
import java.util.List;

public class MapPreviewActivity extends AppCompatActivity {

    private GoogleMap gmap;
    private LatLng pickup;
    private LatLng bestPickup;
    private LatLng dropoff;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_preview);

        pickup     = new LatLng(getD("pickup_lat", 32.733),  getD("pickup_lng", -97.114));
        bestPickup = new LatLng(getD("best_lat",   32.7355), getD("best_lng",  -97.1115));
        dropoff    = new LatLng(getD("drop_lat",   32.750),  getD("drop_lng",  -97.120));

        SupportMapFragment frag = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (frag != null) {
            frag.getMapAsync(map -> {
                gmap = map;
                renderPreview();
            });
        }

        FrameLayout card = findViewById(R.id.card);
        View.OnClickListener openWalking = v -> openWalkingTo(bestPickup.latitude, bestPickup.longitude);
        card.setOnClickListener(openWalking);
    }

    private double getD(String key, double def){
        try { return Double.parseDouble(String.valueOf(getIntent().getExtras().get(key))); }
        catch (Exception e) { return def; }
    }

    private void renderPreview() {
        if (gmap == null) return;

        gmap.getUiSettings().setMapToolbarEnabled(false);
        gmap.getUiSettings().setZoomControlsEnabled(false);
        gmap.getUiSettings().setCompassEnabled(false);
        gmap.setTrafficEnabled(false);
        gmap.setBuildingsEnabled(true);

        gmap.addMarker(new MarkerOptions()
                .position(pickup)
                .title("Here")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        gmap.addMarker(new MarkerOptions()
                .position(bestPickup)
                .title("Walk here (cheaper)")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        gmap.addMarker(new MarkerOptions()
                .position(dropoff)
                .title("Drop-off")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

        List<PatternItem> dotted = Arrays.asList(new Dot(), new Gap(14));
        gmap.addPolyline(new PolylineOptions()
                .add(pickup, bestPickup)
                .color(0xFF1F8EF1)
                .width(8f)
                .pattern(dotted));

        gmap.addPolyline(new PolylineOptions()
                .add(bestPickup, dropoff)
                .color(0xFF2EC5B6)
                .width(8f));

        LatLngBounds b = new LatLngBounds.Builder()
                .include(pickup).include(bestPickup).include(dropoff).build();
        gmap.moveCamera(CameraUpdateFactory.newLatLngBounds(b, 80));

        gmap.setOnMapClickListener(latLng -> openWalkingTo(bestPickup.latitude, bestPickup.longitude));
    }

    private void openWalkingTo(double lat, double lng){
        Uri app = Uri.parse("google.navigation:q=" + lat + "," + lng + "&mode=w");
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, app);
            i.setPackage("com.google.android.apps.maps");
            startActivity(i);
            return;
        } catch (Exception ignored){}

        Uri web = Uri.parse("https://www.google.com/maps/dir/?api=1"
                + "&destination=" + lat + "," + lng
                + "&travelmode=walking");
        startActivity(new Intent(Intent.ACTION_VIEW, web));
    }

    public static Intent newIntent(AppCompatActivity ctx,
                                   double pickupLat, double pickupLng,
                                   double dropLat, double dropLng,
                                   double bestLat, double bestLng) {
        Intent i = new Intent(ctx, MapPreviewActivity.class);
        i.putExtra("pickup_lat", pickupLat);
        i.putExtra("pickup_lng", pickupLng);
        i.putExtra("drop_lat",   dropLat);
        i.putExtra("drop_lng",   dropLng);
        i.putExtra("best_lat",   bestLat);
        i.putExtra("best_lng",   bestLng);
        return i;
    }
}
