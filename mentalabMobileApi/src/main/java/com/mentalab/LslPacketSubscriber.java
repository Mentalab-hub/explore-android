package com.mentalab;

import android.util.Log;
import com.mentalab.LslLoader.StreamInfo;
import java.io.IOException;
import java.util.ArrayList;

public class LslPacketSubscriber {

  private static final String TAG = "EXPLORE_LSL_DEV";
  static LslLoader.StreamOutlet lslStreamOutletExg;
  private LslLoader.StreamInfo lslStreamInfoExg;
  static LslLoader.StreamOutlet lslStreamOutletOrn;
  private LslLoader.StreamInfo lslStreamInfoOrn;
  static LslLoader lslLoader = new LslLoader();

  public LslPacketSubscriber() throws IOException {

    lslStreamInfoExg =
        new StreamInfo("Explore_ExG", "ExG", 8, 250, LslLoader.ChannelFormat.float32, "ExG");
    if (lslStreamInfoExg == null) {
      throw new IOException("Stream Info is Null!!");
    }
    lslStreamOutletExg = new LslLoader.StreamOutlet(lslStreamInfoExg);

    lslStreamInfoOrn =
        new StreamInfo("Explore_Orn", "Orn", 9, 20, LslLoader.ChannelFormat.float32, "Orn");
    if (lslStreamInfoOrn == null) {
      throw new IOException("Stream Info is Null!!");
    }
    lslStreamOutletOrn = new LslLoader.StreamOutlet(lslStreamInfoOrn);

    Log.d(TAG, "Subscribing!!");
    PubSubManager.getInstance().subscribe("ExG", this::packetCallbackExG);
    PubSubManager.getInstance().subscribe("Orn", this::packetCallbackOrn);
  }

  public void packetCallbackExG(Packet packet) {
    Log.d("TAG", "packetCallbackExG");
    lslStreamOutletExg.push_sample(convertArraylistToFloatArray(packet));
  }

  public void packetCallbackOrn(Packet packet) {
    Log.d("TAG", "packetCallbackOrn");
    lslStreamOutletOrn.push_sample(convertArraylistToFloatArray(packet));
  }

  float[] convertArraylistToFloatArray(Packet packet) {
    ArrayList<Float> packetVoltageValues = new ArrayList<Float>();
    if (packet instanceof DataPacket) {
      packetVoltageValues = ((DataPacket) packet).getVoltageValues();
      float[] floatArray = new float[packetVoltageValues.size()];
      Object[] array = packetVoltageValues.toArray();
      for (int index = 0; index < packetVoltageValues.size(); index++) {
        floatArray[index] = ((Float) packetVoltageValues.get(index)).floatValue();
      }
      return floatArray;
    } else if (packet instanceof Orientation) {
      packetVoltageValues = ((Orientation) packet).listValues;
      float[] floatArray = new float[packetVoltageValues.size()];
      Object[] array = packetVoltageValues.toArray();
      for (int index = 0; index < packetVoltageValues.size(); index++) {
        floatArray[index] = ((Float) packetVoltageValues.get(index)).floatValue();
      }
      return floatArray;
    }
    return null;
  }
}
