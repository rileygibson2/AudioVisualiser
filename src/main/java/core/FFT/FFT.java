package main.java.core.FFT;

import java.io.File;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

import com.tagtraum.jipes.audio.AudioSignalSource;
import com.tagtraum.jipes.math.FFTFactory;

public class FFT {

	private static final float NORMALIZATION_FACTOR_2_BYTES = Short.MAX_VALUE + 1.0f;


	public static void main(final String[] args) throws Exception {
		// use only 1 channel, to make this easier
		final AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 1, 2, 44100, false);
		final DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
		final TargetDataLine targetLine = (TargetDataLine) AudioSystem.getLine(info);
		targetLine.open();
		targetLine.start();
		//final AudioInputStream audioStream = new AudioInputStream(targetLine);

		final AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File("daybreak.wav"));
		
		
		final byte[] buf = new byte[1024]; // <--- increase this for higher frequency resolution
		final int numberOfSamples = buf.length / format.getFrameSize();
		
		int count = 0;
		while (count<90) {
			count++;
			audioStream.read(buf);
			if (count<90) continue;
			
			System.out.println(audioStream.available());
			
			// the stream represents each sample as two bytes -> decode
			final float[] samples = decode(buf, format);
			final Complex[] transformed = fftTransform(buildComplex(samples));
			final float[] magnitudes = toMagnitudes(transformed);

			// do something with magnitudes...
			for (float b : magnitudes) System.out.print(b+", ");
			System.out.println("\n"+buf.length);
		}
	}

	public static Complex[] fftTransform(Complex[] x) {
		int n = x.length;

		// base case
		if (n == 1) return new Complex[] { x[0] };

		// radix 2 Cooley-Tukey FFT
		if (n % 2 != 0) {
			throw new IllegalArgumentException("n is not a power of 2");
		}

		// compute FFT of even terms
		Complex[] even = new Complex[n/2];
		for (int k = 0; k < n/2; k++) {
			even[k] = x[2*k];
		}
		Complex[] evenFFT = fftTransform(even);

		// compute FFT of odd terms
		Complex[] odd  = even;  // reuse the array (to avoid n log n space)
		for (int k = 0; k < n/2; k++) {
			odd[k] = x[2*k + 1];
		}
		Complex[] oddFFT = fftTransform(odd);

		// combine
		Complex[] y = new Complex[n];
		for (int k = 0; k < n/2; k++) {
			double kth = -2 * k * Math.PI / n;
			Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
			y[k]       = evenFFT[k].plus (wk.times(oddFFT[k]));
			y[k + n/2] = evenFFT[k].minus(wk.times(oddFFT[k]));
		}
		return y;
	}

	public static Complex[] buildComplex(float[] data) {
		Complex[] result = new Complex[data.length];

		for (int i=0; i<data.length; i++) {
			result[i] = new Complex(data[i], 0);
		}

		return result;
	}

	private static float[] decode(final byte[] buf, final AudioFormat format) {
		final float[] fbuf = new float[buf.length / format.getFrameSize()];
		for (int pos = 0; pos < buf.length; pos += format.getFrameSize()) {
			final int sample = format.isBigEndian()
					? byteToIntBigEndian(buf, pos, format.getFrameSize())
							: byteToIntLittleEndian(buf, pos, format.getFrameSize());
			// normalize to [0,1] (not strictly necessary, but makes things easier)
			fbuf[pos / format.getFrameSize()] = sample / NORMALIZATION_FACTOR_2_BYTES;
		}
		return fbuf;
	}

	private static double[] toMagnitudes(final float[] realPart, final float[] imaginaryPart) {
		final double[] powers = new double[realPart.length / 2];
		for (int i = 0; i < powers.length; i++) {
			powers[i] = Math.sqrt(realPart[i] * realPart[i] + imaginaryPart[i] * imaginaryPart[i]);
		}
		return powers;
	}

	private static float[] toMagnitudes(Complex[] data) {
		final float[] powers = new float[data.length / 2];
		for (int i = 0; i < powers.length; i++) {
			powers[i] = (float) Math.sqrt(data[i].re() * data[i].re() + data[i].im() * data[i].im());
		}
		return powers;
	}

	private static int byteToIntLittleEndian(final byte[] buf, final int offset, final int bytesPerSample) {
		int sample = 0;
		for (int byteIndex = 0; byteIndex < bytesPerSample; byteIndex++) {
			final int aByte = buf[offset + byteIndex] & 0xff;
			sample += aByte << 8 * (byteIndex);
		}
		return sample;
	}

	private static int byteToIntBigEndian(final byte[] buf, final int offset, final int bytesPerSample) {
		int sample = 0;
		for (int byteIndex = 0; byteIndex < bytesPerSample; byteIndex++) {
			final int aByte = buf[offset + byteIndex] & 0xff;
			sample += aByte << (8 * (bytesPerSample - byteIndex - 1));
		}
		return sample;
	}
}
