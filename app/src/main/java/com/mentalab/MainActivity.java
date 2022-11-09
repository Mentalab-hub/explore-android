package com.mentalab;

import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import com.mentalab.exception.CommandFailedException;
import com.mentalab.exception.InvalidCommandException;
import com.mentalab.exception.NoBluetoothException;
import com.mentalab.exception.NoConnectionException;
import com.mentalab.packets.Packet;
import com.mentalab.packets.sensors.exg.EEGPacket;
import com.mentalab.service.decode.MentalabCodec;
import com.mentalab.service.impedance.ImpedanceCalculator;
import com.mentalab.service.io.ContentServer;
import com.mentalab.service.io.Subscriber;
import com.mentalab.utils.constants.Topic;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

  @RequiresApi(api = VERSION_CODES.R)
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    //try {
    try {
      if (Build.VERSION.SDK_INT >= 30){
        if (!Environment.isExternalStorageManager()){
          Intent getpermission = new Intent();
          getpermission.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
          startActivity(getpermission);
        }
      }
      InputStream stream = new FileInputStream(
          Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/DATA000.BIN");
      MentalabCodec.getInstance().decodeInputStream(stream);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }


  /*    ExploreDevice device = MentalabCommands.connect("CA26");
      device.acquire();

      device.calculateImpedance();

      Subscriber<EEGPacket> subscriber =
          new Subscriber<EEGPacket>(Topic.IMPEDANCE) {
            @Override
            public void accept(Packet packet) {
              Log.d("HELLO__", "----packet time" + packet.getData());
            }
          };
      ContentServer.getInstance().registerSubscriber(subscriber);
      // device.formatMemory();
    } catch (NoBluetoothException
        | NoConnectionException
        | IOException
        | ExecutionException
        | InterruptedException e) {
      e.printStackTrace();
    } catch (InvalidCommandException e) {
      e.printStackTrace();
    } catch (CommandFailedException e) {
      e.printStackTrace();
    }*/
  }
}
