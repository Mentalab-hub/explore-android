package com.mentalab;

import android.bluetooth.BluetoothDevice;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import com.mentalab.exception.CommandFailedException;
import com.mentalab.exception.InvalidDataException;
import com.mentalab.exception.NoBluetoothException;
import com.mentalab.exception.NoConnectionException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

  @RequiresApi(api = VERSION_CODES.R)
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    try {
      Set<BluetoothDevice> deviceList = MentalabCommands.scan();
      MentalabCommands.connect(deviceList.iterator().next());

      // Map<String, Boolean> configMap = Map.of(DeviceConfigSwitches.Channels[7], false,
      // MentalabConstants.DeviceConfigSwitches.Channels[6], false);
      // MentalabCommands.setEnabled(configMap);
      // MentalabCommands.pushToLsl();
      // MentalabCommands.formatDeviceMemory();
      // Thread.sleep(2000);
      // MentalabCodec.stopDecoder();
    } catch (NoBluetoothException exception) {
      exception.printStackTrace();
    } catch (InvalidDataException exception) {
      exception.printStackTrace();
    } catch (NoConnectionException exception) {
      exception.printStackTrace();
    } catch (CommandFailedException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
