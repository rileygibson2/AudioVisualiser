package main.java.core;


import java.io.File;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line.Info;
import javax.sound.sampled.SourceDataLine;

public class Test {
	 public static void main(String[] args) throws Exception {
	        
	        // Name string uses relative addressing, assumes the resource is  
	        // located in "audio" child folder of folder holding this class.
	        //URL url = Test.class.getClassLoader().getResource("/daybreak.wav");
	        //System.out.println(url);
	        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("daybreak.wav"));
	        
	        AudioFormat audioFormat = new AudioFormat(
	                Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
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
