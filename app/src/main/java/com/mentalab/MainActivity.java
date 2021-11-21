package com.mentalab;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.mentalab.MentalabConstants.SamplingRate;
import com.mentalab.exception.CommandFailedException;
import com.mentalab.exception.InvalidCommandException;
import com.mentalab.exception.InvalidDataException;
import com.mentalab.exception.NoBluetoothException;
import com.mentalab.exception.NoConnectionException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    try {
      Set<String> deviceList = MentalabCommands.scan();
      MentalabCommands.connect(deviceList.iterator().next());
      InputStream inputStream = MentalabCommands.getRawData();
      Map<String, Queue<Float>> map = MentalabCodec.decode(inputStream);

      //Thread.sleep(2000);

      // MentalabCommands.pushToLsl();
      MentalabCommands.setSamplingRate(SamplingRate.SR_500);
    } catch (NoBluetoothException exception) {
      exception.printStackTrace();
    } catch (InvalidDataException exception) {
      exception.printStackTrace();
    } catch (NoConnectionException exception) {
      exception.printStackTrace();
    } catch (CommandFailedException e) {
      e.printStackTrace();
    } catch (IOException exception) {
      exception.printStackTrace();
    } catch (InvalidCommandException e) {
      e.printStackTrace();
    }
  }
}
