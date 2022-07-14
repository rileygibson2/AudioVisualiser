package main.java.core;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import main.java.renders.bars.BarsRender;
import main.java.renders.basic.BasicRender;
import main.java.renders.circle.CircleRender;
import main.java.renders.reflective.ReflectiveRender;
import main.java.renders.streaks.StreaksRender;

public class Controller {

	public RenderPanel rP;
	protected List<Render> renders;
	public Render currentRender;
	private HashMap<String, Line.Info> ins = new HashMap<>();
	protected boolean capture;

	//Audio lines
	private TargetDataLine micLine;
	private SourceDataLine speakerLine;

	private final static int blockLength = 1024;

	public double[] magnitudes; //Real, uncut or averaged or modified magnitudes

	public Controller() {
		rP = RenderPanel.initialise(this, 1450, 900);
		
		renders = new ArrayList<Render>();
		renders.add(new BasicRender(this));
		renders.add(new AlbumCoverRender(this));
		renders.add(new ReflectiveRender(this));
		renders.add(new BarsRender(this));
		renders.add(new CircleRender(this));
		renders.add(new StreaksRender(this));
		
		currentRender = null;

		ControllerGUI.initialise(this);
		this.capture = true;
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

			//Read file
			final byte[] buffer = new byte[blockLength];
			while ((audioStream.read(buffer)) != -1)  {
				speakerLine.write(buffer, 0, blockLength); //Write to speakers
				if (capture) {
					//Decode bytes into doubles and use JTransforms to do fft on buffer
					DoubleFFT_1D fft = new DoubleFFT_1D(buffer.length/2);
					magnitudes = new double[buffer.length];
					System.arraycopy(decodeBuffer(buffer, format), 0, magnitudes, 0, buffer.length/2);
					fft.complexForward(magnitudes);
				}
				else magnitudes = new double[buffer.length]; //Set to 0 to mitigate frozen visualisers
			}

			//Release system resources
			speakerLine.drain();
			speakerLine.close();
			audioStream.close();
			if (micLine!=null) {
				micLine.drain();
				micLine.close();
				micLine = null;
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
			micLine = (TargetDataLine) AudioSystem.getLine(new DataLine.Info(TargetDataLine.class, format));
			micLine.open();
			System.out.println("TargetLine available: "+micLine.available());
			micLine.start();
			return new AudioInputStream(micLine);

		} catch (LineUnavailableException e) {throw new Error("Error creating input stream from microphone");}
	}

	/*private void initialise() {
		AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
		//format = new AudioFormat(8000.0f,8,1,true,false);

		try {
			micLine = getTargetDataLine("Built-in Microphone");
			micLine.open();

			//Microphone
			TargetDataLine micLine = (TargetDataLine) AudioSystem.getLine(new DataLine.Info(TargetDataLine.class, format));
			micLine.open();

			System.out.println(micLine.available());

			Thread monitorThread = new Thread() {
				@Override
				public void run() {
					micLine.start();
					File outputFile = new File("recording.wav");

					AudioInputStream stream = new AudioInputStream(micLine);
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

			System.out.println(micLine.available());

			micLine.stop();
			micLine.close();
			System.out.println("End Monitor");

		}
		catch(InterruptedException ie) { ie.printStackTrace();}
		catch (LineUnavailableException e1) {System.out.println("Problem opening line");}

	}*/

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
		new Controller();
	}

	public static int random(double min, double max){
		return (int) ((Math.random()*((max-min)+1))+min);
	}
	
	public static double randomD(double min, double max){
		return ((Math.random()*((max-min)+1))+min);
	}
}
