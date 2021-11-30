package com.mentalab;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.mentalab.exception.CommandFailedException;
import com.mentalab.exception.InvalidDataException;
import com.mentalab.exception.NoBluetoothException;
import com.mentalab.exception.NoConnectionException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); // todo: change logging
    private final ActivityResultLauncher<String> permissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    // todo: actually do something
                    return; // permission not granted, do nothing
                }
            });


    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateOrRequestPermissions();

        try {
            connectToADevice();
        } catch (NoConnectionException | NoBluetoothException | IOException | CommandFailedException e) {
            LOGGER.log(Level.SEVERE, "Unable to connect to a device. Exiting.", e);
            this.finishAffinity();
            return;
        }

        final Uri location = getLocation();
        try (final InputStream rawData = MentalabCommands.getRawData()) {
            MentalabCodec.decode(rawData);
            TimeUnit.SECONDS.sleep(1);

            final RecordSubscriber subscriber = new RecordSubscriber
                    .Builder(location, "test.csv", getApplicationContext())
                    .build();

            subscriber.setAdcMask(MentalabCodec.getAdsMask());
            subscriber.setSamplingRate(MentalabCodec.getSamplingRate());

            MentalabCommands.record(subscriber); //todo: this logic needs to change to MentalabCommands.record()
            TimeUnit.SECONDS.sleep(10);
            System.out.println("Done!");
        } catch (NoBluetoothException | IOException | InvalidDataException | InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Error! Exiting system.", e);
            MentalabCommands.closeSockets();
            this.finishAffinity();
        }
    }

//    private boolean createExploreFolder() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            final String folderName = "Explore";
//
//            final ContentValues contentValues = new ContentValues();
//            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, folderName);
//            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, );
//            return getApplicationContext()
//                    .getContentResolver()
//                    .insert(MediaStore., contentValues);
//        }
//    }


    private static void connectToADevice() throws NoBluetoothException, NoConnectionException, IOException, CommandFailedException {
        final Set<String> deviceList = MentalabCommands.scan();
        if (deviceList == null) {
            throw new NoConnectionException("Could not find any Bluetooth devices. Exiting.");
        }
        MentalabCommands.connect(deviceList.iterator().next());
    }


    private Uri getLocation() {
        Uri downloads;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            downloads = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            downloads = Uri.parse((android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_DOWNLOADS).toString()));
        }
        return downloads;
    }


    private void updateOrRequestPermissions() {
        boolean hasReadPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        boolean hasWritePermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        boolean isNewAndroid = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;

        boolean writePermissionGranted = hasWritePermission || isNewAndroid;

        Set<String> permissionsToRequest = new HashSet<>();
        if (!writePermissionGranted) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!hasReadPermission) {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if (!permissionsToRequest.isEmpty()) {
            for (String permission : permissionsToRequest) {
                permissionsLauncher.launch(permission);
            }
        }
    }
}
