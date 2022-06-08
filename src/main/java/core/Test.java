package main.java.core;


import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line.Info;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class Test {
	public static void main(String[] args) throws Exception {
		AudioFormat audioFormat = new AudioFormat(
				Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
		//AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("daybreak.wav"));

		TargetDataLine targetLine = (TargetDataLine) AudioSystem.getLine(new DataLine.Info(TargetDataLine.class, audioFormat));
		targetLine.open();
		targetLine.start();
		AudioInputStream audioInputStream = new AudioInputStream(targetLine);


		Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
		SourceDataLine sourceDataLine = (SourceDataLine)AudioSystem.getLine(info);
		sourceDataLine.open(audioFormat);

		int toRead = 1024;

		int bytesRead = 0;
		byte[] buffer = new byte[toRead];
		sourceDataLine.start();

		while((bytesRead = audioInputStream.read(buffer)) != -1)  {
			System.out.println(bytesRead);
			sourceDataLine.write(buffer, 0, bytesRead);                                 
		}   
		sourceDataLine.drain();
		// release resources
		sourceDataLine.close();
		audioInputStream.close();
	}

}

/*boolean flip = false;
	        	for (int i=0; i<buffer.length; i++) {
	        		if (i%2==0) flip = !flip;
	        		if (flip) buffer[i] = 0;
	        	}*/
