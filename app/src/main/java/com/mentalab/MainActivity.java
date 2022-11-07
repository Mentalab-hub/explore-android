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
      ExploreDevice device = MentalabCommands.connect("CA26");
      device.acquire();
      device.calculateImpedance();

      Subscriber<EEGPacket> subscriber = new Subscriber<EEGPacket>(Topic.IMPEDANCE) {
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
    }
  }
}
