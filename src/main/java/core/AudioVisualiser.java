package main.java.core;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

public class AudioVisualiser {

	//Render c;

	HashMap<String, Line.Info> ins = new HashMap<>();
	TargetDataLine targetLine;

	public AudioVisualiser() {
		//Render.initialise();
		//c = new Render();
		enumerateMicrophones();

		initialise();
		printMicrophones();
	}

	//Microphone
	//DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
	//if (!AudioSystem.isLineSupported(info)) System.out.println("Not supported");
	//TargetDataLine targetLine = (TargetDataLine) AudioSystem.getLine(info);

	public void initialise() {
		AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
		format = new AudioFormat(8000.0f,8,1,true,false);
		
		try {
			//targetLine = getTargetDataLine("Built-in Microphone");
			//targetLine.open();

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
}
