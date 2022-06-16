package main.java.core;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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

import main.java.renders.Render;
import main.java.renders.albumcover.AlbumCoverRender;
import main.java.renders.basic.BasicRender;
import main.java.renders.reflective.ReflectiveBlocksRender;

public class AudioVisualiser {

	Render render;
	HashMap<String, Line.Info> ins = new HashMap<>();
	TargetDataLine targetLine;
	
	//Audio lines
	TargetDataLine micLine;
	SourceDataLine speakerLine;

	public final static int blockLength = 1024;
	public float maxFrequency; //Maximum calculable frequency
	public float measurementDuration; //Time in seconds that each sample measures
	public float frequencyResolution;  //Frequency distance between each 'bucket'
	public final static int groupBlocks = 1; //Number of blocks to group togather in display
	public int zones;

	public double[] magnitudes;


	public AudioVisualiser() {
		render = BasicRender.initialise(this);
		//render = AlbumCoverRender.initialise(this);
		render = ReflectiveBlocksRender.initialise(this);
		runSpectro();
	}

	private void runSpectro() {
		//44100
		final AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100*2, 16, 1, 2, 44100, false);
		try {
			//Input stuff
			AudioInputStream audioStream = getFileInputStream("lonedigger.wav");
			//AudioInputStream audioStream = getMicrophoneInputStream(format);

			//Output stuff
			Info speakerInfo = new DataLine.Info(SourceDataLine.class, format);
			speakerLine = (SourceDataLine)AudioSystem.getLine(speakerInfo);
			speakerLine.open(format);
			speakerLine.start();

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
				speakerLine.write(buffer, 0, blockLength);

				//Decode bytes into doubles and use JTransforms to do fft on buffer
				DoubleFFT_1D fft = new DoubleFFT_1D(buffer.length/2);
				magnitudes = new double[buffer.length];
				System.arraycopy(decodeBuffer(buffer, format), 0, magnitudes, 0, buffer.length/2);
				fft.complexForward(magnitudes);

				//Dont average or cut here, renders should do their own manipulation of data into whatever form they need
			}

			//Release system resources
			speakerLine.drain();
			speakerLine.close();
			audioStream.close();
			if (targetLine!=null) {
				targetLine.drain();
				targetLine.close();
				targetLine = null;
			}
		}
		catch (IOException e) {e.printStackTrace();}
		catch (LineUnavailableException e) {e.printStackTrace();}
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

	public AudioInputStream getFileInputStream(String fileName) {
		try {
			return AudioSystem.getAudioInputStream(new File(fileName));
		} catch (UnsupportedAudioFileException | IOException e) {
			throw new Error("Error with getting audio from file.");
		}
	}

	public AudioInputStream getMicrophoneInputStream(AudioFormat format) {
		try {
			targetLine = (TargetDataLine) AudioSystem.getLine(new DataLine.Info(TargetDataLine.class, format));
			targetLine.open();
			System.out.println("TargetLine available: "+targetLine.available());
			targetLine.start();
			return new AudioInputStream(targetLine);
			
		} catch (LineUnavailableException e) {throw new Error("Error creating input stream from microphone");}
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

}
