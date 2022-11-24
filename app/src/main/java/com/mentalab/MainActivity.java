package com.mentalab;

import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import com.mentalab.exception.CommandFailedException;
import com.mentalab.exception.InvalidCommandException;
import com.mentalab.exception.NoBluetoothException;
import com.mentalab.exception.NoConnectionException;
import com.mentalab.packets.Packet;
import com.mentalab.packets.sensors.exg.EEGPacket;
import com.mentalab.service.io.ContentServer;
import com.mentalab.service.io.Subscriber;
import com.mentalab.utils.constants.Topic;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

  @RequiresApi(api = VERSION_CODES.R)
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    try {
      final ExploreDevice connect = MentalabCommands.connect("855E");
      connect.acquire();
      connect.calculateImpedance();
      Subscriber<EEGPacket> sub =
          new Subscriber<EEGPacket>(Topic.IMPEDANCE) {
            @Override
            public void accept(Packet packet) {
              Log.d("DEBUG__ZZ", packet.getData().toString());
            }
          };
      ContentServer.getInstance().registerSubscriber(sub);

      // To get last connected device after sending any command/connection drop: use
      // getLastConnectedDevice() method of MentalabCodec
    }
    // catch (NoBluetoothException | NoConnectionException | IOException | ExecutionException |
    // InterruptedException e) {
    catch (NoBluetoothException
        | NoConnectionException
        | IOException
        | ExecutionException
        | InterruptedException
        | InvalidCommandException
        | CommandFailedException e) {
      e.printStackTrace();
    }
  }
}
