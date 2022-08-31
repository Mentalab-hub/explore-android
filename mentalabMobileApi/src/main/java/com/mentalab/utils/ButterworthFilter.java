package com.mentalab.utils;

import uk.me.berndporr.iirj.Butterworth;

public class ButterworthFilter {
  /**
   * The Butterworth class implements low-pass, high-pass, band-pass and band-stop filter using the
   * Butterworth polynomials. Has the flattest pass-band but a poor roll-off rate. Reference:
   * https://en.wikipedia.org/wiki/Butterworth_filter
   */
  private final double samplingFreq;

  private static final int notchFreq = 50;

  private final double nyquistFreq;
  private final int filterOrder = 5;

  /**
   * This constructor initialises the prerequisites required to use Butterworth filter.
   *
   * @param Fs Sampling frequency of input signal
   */
  public ButterworthFilter(double Fs) {
    this.samplingFreq = Fs;
    nyquistFreq = samplingFreq / 2;
  }

  /**
   * This method implements a low pass filter with given parameters, filters the signal and returns
   * it.
   *
   * @param signal Signal to be filtered
   * @param order Order of the filter
   * @param cutoffFreq The cutoff frequency for the filter in Hz
   * @return double[] Filtered signal
   */
  public double[] lowPassFilter(double[] signal, int order, double cutoffFreq) {
    double[] output = new double[signal.length];
    Butterworth lp = new Butterworth();
    lp.lowPass(order, this.samplingFreq, cutoffFreq);
    for (int i = 0; i < output.length; i++) {
      output[i] = lp.filter(signal[i]);
    }
    return output;
  }

  /**
   * This method implements a high pass filter with given parameters, filters the signal and returns
   * it.
   *
   * @param signal Signal to be filtered
   * @param order Order of the filter
   * @param cutoffFreq The cutoff frequency for the filter in Hz
   * @return double[] Filtered signal
   */
  public double[] highPassFilter(double[] signal, int order, double cutoffFreq) {
    double[] output = new double[signal.length];
    Butterworth hp = new Butterworth();
    hp.highPass(this.filterOrder, this.samplingFreq, cutoffFreq);
    for (int i = 0; i < output.length; i++) {
      output[i] = hp.filter(signal[i]);
    }
    return output;
  }

  /**
   * This method implements a band pass filter with given parameters, filters the signal and returns
   * it.
   *
   * @param signal Signal to be filtered
   * @throws java.lang.IllegalArgumentException The lower cutoff frequency is greater than the
   *     higher cutoff frequency
   * @return double[] Filtered signal
   */
  public double[] bandPassFilter(double[] signal, boolean isDemodulationFilter)
      throws IllegalArgumentException {

    double lowCutoff;
    double highCutoff;
    if (isDemodulationFilter) {
      lowCutoff = (samplingFreq / 4 - 1.5) / nyquistFreq;
      highCutoff = (samplingFreq / 4 + 1.5) / nyquistFreq;
    } else {
      lowCutoff = (samplingFreq / 4 + 2.5) / nyquistFreq;
      highCutoff = (samplingFreq / 4 + 5.5) / nyquistFreq;
    }
    if (lowCutoff >= highCutoff) {
      throw new IllegalArgumentException(
          "Lower Cutoff Frequency cannot be more than the Higher Cutoff Frequency");
    }
    double centreFreq = (highCutoff + lowCutoff) / 2.0;
    double width = Math.abs(highCutoff - lowCutoff);
    double[] output = new double[signal.length];
    Butterworth bp = new Butterworth();
    bp.bandPass(this.filterOrder, this.samplingFreq, centreFreq, width);
    for (int i = 0; i < output.length; i++) {
      output[i] = bp.filter(signal[i]);
    }
    return output;
  }

  /**
   * This method implements a band stop filter with given parameters, filters the signal and returns
   * it.
   *
   * @param signal Signal to be filtered
   * @throws java.lang.IllegalArgumentException The lower cutoff frequency is greater than the
   *     higher cutoff frequency
   * @return double[] Filtered signal
   */
  public double[] bandStopFilter(double[] signal) throws IllegalArgumentException {
    int order = 5;
    double nyquistFreq = samplingFreq / 2;
    double lowCutoff = (notchFreq - 2) / nyquistFreq;
    double highCutoff = (notchFreq + 2) / nyquistFreq;
    if (lowCutoff >= highCutoff) {
      throw new IllegalArgumentException(
          "Lower Cutoff Frequency cannot be more than the Higher Cutoff Frequency");
    }
    double centreFreq = (highCutoff + lowCutoff) / 2.0;
    double width = Math.abs(highCutoff - lowCutoff);
    double[] output = new double[signal.length];
    Butterworth bs = new Butterworth();
    bs.bandStop(order, this.samplingFreq, centreFreq, width);
    for (int i = 0; i < output.length; i++) {
      output[i] = bs.filter(signal[i]);
    }
    return output;
  }
}
