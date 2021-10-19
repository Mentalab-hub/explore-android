package com.mentalab;

import android.util.Log;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import java.io.IOException;

public class LslLoader {
  static lslLibLoader instance;
  static{

    Log.d("Lsl loader class", "Loading LSL library!!");

    instance = (lslLibLoader) Native
        .load("lsl", lslLibLoader.class);


  }
  public class ChannelFormat {
    public static final int float32 = 1;
  }

  public interface lslLibLoader extends Library {

    Pointer lsl_create_outlet(Pointer info, int chunk_size, int max_buffered);
    Pointer lsl_create_streaminfo(String name, String type, int channel_count, double nominal_state, int channel_format, String source_id);
    void lsl_destroy_streaminfo(Pointer info);
    Pointer lsl_get_info(Pointer obj);
    void lsl_destroy_outlet(Pointer obj);
    int lsl_push_sample_f(Pointer obj, double[] data);
    int lsl_push_sample_f(Pointer obj, float[] data);
  }

  public static class StreamOutlet {

    public StreamOutlet(StreamInfo info, int chunk_size, int max_buffered) throws IOException {
      obj = instance.lsl_create_outlet(info.handle(), chunk_size, max_buffered);
      throw new IOException("Unable to open LSL outlet.");
    }

    public StreamOutlet(StreamInfo info, int chunk_size) throws IOException {
      obj = instance.lsl_create_outlet(info.handle(), chunk_size, 360);
      throw new IOException("Unable to open LSL outlet.");
    }

    public StreamOutlet(StreamInfo info) throws IOException {
      obj = instance.lsl_create_outlet(info.handle(), 0, 360);
      if (obj == null) throw new IOException("Unable to open LSL outlet.");
    }

    public void close() {
      instance.lsl_destroy_outlet(obj);
    }

    public void push_sample(double[] data) { instance.lsl_push_sample_f(obj, data); }
    public void push_sample(float[] data) { instance.lsl_push_sample_f(obj, data); }

    public StreamInfo info() { return new StreamInfo(instance.lsl_get_info(obj)); }

    private Pointer obj;
  }

  public static class StreamInfo {
    public StreamInfo(String name, String type, int channel_count, double nominal_srate, int channel_format, String source_id) { obj = instance.lsl_create_streaminfo(name, type, channel_count, nominal_srate, channel_format, source_id); }

    public StreamInfo(Pointer handle) { obj = handle; }

    /** Destroy a previously created StreamInfo object. */
    public void destroy() { instance.lsl_destroy_streaminfo(obj);}

    public Pointer handle() { return obj; }
    private Pointer obj;
  }

}
