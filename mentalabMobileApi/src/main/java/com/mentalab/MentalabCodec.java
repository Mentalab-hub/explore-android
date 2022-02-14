package com.mentalab;

import android.bluetooth.BluetoothDevice;
import android.util.Log;
import com.mentalab.commandtranslators.CommandTranslator;
import com.mentalab.exception.InvalidCommandException;
import com.mentalab.exception.InvalidDataException;
import com.mentalab.service.ExecutorServiceManager;
import com.mentalab.tasks.DecoderTask;
import com.mentalab.tasks.LslStreamerTask;
import com.mentalab.utils.MentalabConstants.Command;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.Future;

public class MentalabCodec {

  private static final String TAG = "Explore";
  private static Future<?> decoderTask;
  private static Map<String, Queue<Float>> decodedDataMap;

  /**
   * Decodes a device raw data stream
   *
   * <p>Incoming bytes from Bluetooth are converted to an immutable Map of Double Ended Queue of
   * Float numbers. ExG channels are saved as single precision floating point numbers (Float) in the
   * unit of mVolt. Launches one worker thread on first invocation. Currently the it provides the
   * following data queues from Explore device: Channel_1, Channel_2...Channel_N where N is the maximum
   * available numbers of channel of the device. Acc_X, Acc_Y, Acc_Z in the units of mg/LSB. Gyro_X,
   * Gyro_Y and Gyro_Z in mdps/LSB. MAG_X, Mag_Y, Mag_Z in mgauss/LSB. To get a specific instance of
   * the queue:
   * <pre>{@code
   * Map<String, Queue<Float>> map = MentalabCodec.decode(stream);
   * Queue<Float> accXMap = map.get("Acc_X").poll();
   * Queue<Float> channel2 = map.get("Channel2").poll();
   * }<pre>
   *
   * @throws InvalidDataException throws when invalid data is received
   * @stream InputStream of device bytes
   * @return Immutable Map of Queues of Numbers
   */
  public static Map<String, Queue<Float>> decode(InputStream stream) throws InvalidDataException {

    decoderTask = ExecutorServiceManager.getExecutorService().submit(new DecoderTask(stream));
    Log.d(TAG, "Started execution of decoder!!");
    decodedDataMap = DecoderTask.getDataMap();
    return decodedDataMap;
  }

  /**
   * Encodes a command
   *
   * @throws InvalidCommandException when the command is not recognized
   * @return byte[] encoded commands that can be sent to the device
   */
  static byte[] encodeCommand(Command command, int extraArguments) {

    CommandTranslator translator = command.createInstance(command, extraArguments);
    byte[] translatedBytes = translator.translateCommand(extraArguments);
    return translatedBytes;
  }

  public static int getAdsMask() {
    return Objects.requireNonNull(decodedDataMap.get("Ads_Mask").poll()).intValue();
  }

  public static float getSamplingRate() {
    return Objects.requireNonNull(decodedDataMap.get("Sampling_Rate").poll());
  }

  // TODO Decouple executor class from Codec class
  public static void pushToLsl(BluetoothDevice device) {
    ExecutorServiceManager.getExecutorService().submit(new LslStreamerTask(device));
  }

  static void stopDecoder() {
    decoderTask.cancel(true);
  }
}
