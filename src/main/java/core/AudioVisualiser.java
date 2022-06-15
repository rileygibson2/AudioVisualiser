package main.java.core;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.Line.Info;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.jtransforms.fft.DoubleFFT_1D;

public class AudioVisualiser {

	Render render;
	HashMap<String, Line.Info> ins = new HashMap<>();
	TargetDataLine targetLine;

	public final static int blockLength = 1024;
	public float maxFrequency; //Maximum calculable frequency
	public float measurementDuration; //Time in seconds that each sample measures
	public float frequencyResolution;  //Frequency distance between each 'bucket'
	public final static int groupBlocks = 1; //Number of blocks to group togather in display
	public int zones;

	public double[] magnitudes;
	//public double[] magnitudes = {10, 21, 30, 29, 14, 3, 36, 45, 41, 2, 12, 28, 18, 31, 30, 42, 38, 27, 17, 20, 9}; //Magnitudes used for ui testing

	public final int buckets = 21; //Number of spectrum buckets
	public final int maxAmp = 45; //Range of possible amplitudes for each bucket

	public AudioVisualiser() {
		//for (int i=0; i<magnitudes.length; i++) render.visualMags[i] = magnitudes[i];
		render = BasicRender.initialise(this);
		render = SpectrumRender.initialise(this);
		initialiseSpectro();
	}

	private void initialiseSpectro() {
		final AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 1, 2, 44100, false);
		try {
			//Input stuff
			AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File("lonedigger.wav"));

			//Output stuff
			Info speakerInfo = new DataLine.Info(SourceDataLine.class, format);
			SourceDataLine sourceLine;
			sourceLine = (SourceDataLine)AudioSystem.getLine(speakerInfo);
			sourceLine.open(format);
			sourceLine.start();

			//Find information
			maxFrequency = format.getSampleRate()/2;
			measurementDuration = blockLength/format.getSampleRate();
			frequencyResolution = format.getSampleRate()/blockLength;
			zones = (int) (maxFrequency/frequencyResolution)/groupBlocks;

			//Read file
			final byte[] buffer = new byte[blockLength];
			int bytesRead = 0;

			while ((bytesRead = audioStream.read(buffer)) != -1)  {
				//Write to speakers
				sourceLine.write(buffer, 0, blockLength);

				//Decode bytes into doubles and use JTransforms to do fft on buffer
				DoubleFFT_1D fft = new DoubleFFT_1D(buffer.length/2);
				magnitudes = new double[buffer.length];
				System.arraycopy(decodeBuffer(buffer, format), 0, magnitudes, 0, buffer.length/2);
				fft.complexForward(magnitudes);

				//Cut unwanted frequencys and average into set number of buckets
				magnitudes = cutandAverageMags(0, 200);
			}

			//Release system resources
			sourceLine.drain();
			sourceLine.close();
			audioStream.close();
		}
		catch (UnsupportedAudioFileException e) {e.printStackTrace();}
		catch (IOException e) {e.printStackTrace();}
		catch (LineUnavailableException e) {e.printStackTrace();}
	}

	private double[] cutandAverageMags(int mincut, int maxcut) {
		if (mincut+maxcut>magnitudes.length||mincut<0||mincut>maxcut) throw new Error("Windowing error during cutting and averaging");
		double[] averaged = new double[buckets];
		double sum = 0;
		int count = 0;
		int bucketCut = (maxcut-mincut)/buckets;
		int bucketCount = 1;

		for (int i=mincut; i<maxcut; i++) {
			if (i==mincut+(bucketCount*bucketCut)) {
				if (bucketCount>=buckets) break;
				
				if (sum>maxAmp) averaged[bucketCount-1] = maxAmp;
				else averaged[bucketCount-1] = sum;
				sum = 0;
				count = 0;
				bucketCount++;
			}

			if (magnitudes[i]>0) sum += magnitudes[i];
			count++;
		}
		return averaged;
	}

	static double[] decodeBuffer(byte[] buffer, AudioFormat format) {
		int  bits = format.getSampleSizeInBits();
		double max = Math.pow(2, bits - 1);

		ByteBuffer bb = ByteBuffer.wrap(buffer);
		bb.order(format.isBigEndian() ?
				ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);

		double[] samples = new double[buffer.length * 8 / bits];
		for(int i = 0; i < samples.length; ++i) {
			switch(bits) {
			case 8:  samples[i] = ( bb.get()      / max ); break;
			case 16: samples[i] = ( bb.getShort() / max ); break;
			case 32: samples[i] = ( bb.getInt()   / max ); break;
			case 64: samples[i] = ( bb.getLong()  / max ); break;
			}
		}

		return samples;
	}

	private void initialise() {
		AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
		//format = new AudioFormat(8000.0f,8,1,true,false);

		try {
			targetLine = getTargetDataLine("Built-in Microphone");
			targetLine.open();

			//Microphone
			TargetDataLine targetLine = (TargetDataLine) AudioSystem.getLine(new DataLine.Info(TargetDataLine.class, format));
			targetLine.open();

			System.out.println(targetLine.available());

			Thread monitorThread = new Thread() {
				@Override
				public void run() {
					targetLine.start();
					File outputFile = new File("recording.wav");

					AudioInputStream stream = new AudioInputStream(targetLine);
					int written = 0;
					try {written = AudioSystem.write(stream, AudioFileFormat.Type.WAVE, outputFile);}
					catch (IOException e) {throw new Error("Problem writing to file");}
					try {stream.close();} catch (IOException e) {e.printStackTrace();}
					System.out.println("written bytes = "+written);
				}
			};

			System.out.println("Start Monitor");
			monitorThread.start();

			Thread.sleep(3000);

			System.out.println(targetLine.available());

			targetLine.stop();
			targetLine.close();
			System.out.println("End Monitor");

		}
		catch(InterruptedException ie) { ie.printStackTrace();}
		catch (LineUnavailableException e1) {System.out.println("Problem opening line");}

	}

	public TargetDataLine getTargetDataLine(String name) {
		Line.Info lineInfo = null;
		for (Map.Entry<String, Line.Info> m : ins.entrySet()) {
			if (m.getKey().equals(name)) lineInfo = m.getValue();
		}
		if (lineInfo==null) throw new Error("Line not found");

		try {return (TargetDataLine) AudioSystem.getLine(lineInfo);}
		catch (LineUnavailableException ex) {ex.printStackTrace(); return null;}
	}


	public void enumerateMicrophones() {
		ins = new HashMap<String, Line.Info>();
		Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();

		for (Mixer.Info info : mixerInfos){
			Mixer m = AudioSystem.getMixer(info);
			Line.Info[] lineInfos = m.getTargetLineInfo();

			if (lineInfos.length>=1 && lineInfos[0].getLineClass().equals(TargetDataLine.class)) {
				//Only adds to hashmap if it is audio input device
				ins.put(info.getName(), lineInfos[0]);//Please enjoy my pun
			}
		}
	}

	public void printMicrophones() {
		//Enumerates all available microphones
		for (Map.Entry<String, Line.Info> m : ins.entrySet()) {
			System.out.println("Line Name: " + m.getKey());//The name of the AudioDevice
			System.out.println("Line Description: " + m.getValue().toString()+"\n");//The type of audio device
		}
	}

	public static void main(String[] args) {
		new AudioVisualiser();
	}

	public static int random(double min, double max){
		return (int) ((Math.random()*((max-min)+1))+min);
	}

	/*final DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
			final TargetDataLine targetLine = (TargetDataLine) AudioSystem.getLine(info);
			targetLine.open();
			targetLine.start();
			final AudioInputStream audioStream = new AudioInputStream(targetLine);*/

}
