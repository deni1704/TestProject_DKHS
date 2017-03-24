package org.ffmpeg.android;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.ffmpeg.android.ShellUtils.ShellCallback;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import com.example.timerexam.R;

public class FfmpegController {

	public Context context;
	
	private String mFfmpegBin;
	
	private final static String TAG = "FFMPEG";

	public FfmpegController(Context context) throws IOException {
		this.context = context;
		installBinaries(context, false);
	}
	
	public void installBinaries(Context context, boolean overwrite)
	{
		mFfmpegBin = installBinary(context, R.raw.ffmpeg, "ffmpeg", overwrite);
	}
	
	public String getBinaryPath ()
	{
		return mFfmpegBin;
	}
	
	private static String installBinary(Context ctx, int resId, String filename, boolean upgrade) {
		try {
			File f = new File(ctx.getDir("bin", 0), filename);
			if (upgrade) {
				if(f.exists()) f.delete();
			}

			if(!f.exists()) {
				copyRawFile(ctx, resId, f, "0755");
			}

			return f.getCanonicalPath();
		} catch (Exception e) {
			Log.e(TAG, "installBinary failed: " + e.getLocalizedMessage());
			return null;
		}
	}
	
	/**
	 * Copies a raw resource file, given its ID to the given location
	 * @param ctx context
	 * @param resid resource id
	 * @param file destination file
	 * @param mode file permissions (E.g.: "755")
	 * @throws IOException on error
	 * @throws InterruptedException when interrupted
	 */
	private static void copyRawFile(Context ctx, int resid, File file, String mode) throws IOException, InterruptedException
	{
		final String abspath = file.getAbsolutePath();
		// Write the iptables binary
		final FileOutputStream out = new FileOutputStream(file);
		final InputStream is = ctx.getResources().openRawResource(resid);
		byte buf[] = new byte[1024];
		int len;
		while ((len = is.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		out.close();
		is.close();
		// Change the permissions
		Runtime.getRuntime().exec("chmod "+mode+" "+abspath).waitFor();
	}

	
	
	private void execFFMPEG (List<String> cmd, ShellCallback sc, File fileExec) throws IOException, InterruptedException {
	
		enablePermissions();
		
		execProcess(cmd, sc, fileExec);
	}
	
	private void enablePermissions () throws IOException
	{
		Runtime.getRuntime().exec("chmod 700 " + mFfmpegBin);
    	
	}
	
	private void execFFMPEG (List<String> cmd, ShellCallback sc) throws IOException, InterruptedException {
		execFFMPEG (cmd, sc, new File(mFfmpegBin).getParentFile());
	}
	
	private int execProcess(List<String> cmds, ShellCallback sc, File fileExec) throws IOException, InterruptedException {		
        
		//ensure that the arguments are in the correct Locale format
		for (String cmd :cmds)
		{
			cmd = String.format(Locale.US, "%s", cmd);
		}
		
		ProcessBuilder pb = new ProcessBuilder(cmds);
		pb.directory(fileExec);
		
		StringBuffer cmdlog = new StringBuffer();

		for (String cmd : cmds)
		{
			cmdlog.append(cmd);
			cmdlog.append(' ');
		}
		
		sc.shellOut(cmdlog.toString());
		
		//pb.redirectErrorStream(true);
		
		Process process = pb.start();    
    

		// any error message?
		StreamGobbler errorGobbler = new StreamGobbler(
				process.getErrorStream(), "ERROR", sc);

    	 // any output?
        StreamGobbler outputGobbler = new 
            StreamGobbler(process.getInputStream(), "OUTPUT", sc);

        errorGobbler.start();
        outputGobbler.start();

        int exitVal = process.waitFor();
        
        sc.processComplete(exitVal);
        
        return exitVal;
		
	}
	
	public Clip trim (String inputPath, float startTime, float endTime, String outPath, boolean copyCodec, ShellCallback sc) throws Exception
	{
		ArrayList<String> cmd = new ArrayList<String>();

		Clip mediaOut = new Clip();
		
		String mediaPath = inputPath;
		
		cmd = new ArrayList<String>();

		cmd.add(mFfmpegBin);
		cmd.add("-y");
		if(startTime != Float.MIN_VALUE) {
			cmd.add("-ss");
			cmd.add(String.format("%.04f", startTime));
		}
		cmd.add("-i");
		cmd.add(mediaPath);
		if(endTime != Float.MAX_VALUE) {
			cmd.add("-t");
			cmd.add(String.format("%.04f", endTime - startTime));
		}

		if(copyCodec) {
			cmd.add("-c");
			cmd.add("copy");

			cmd.add("-avoid_negative_ts");
			cmd.add("1");
		}

		mediaOut.path = outPath;
		cmd.add(mediaOut.path);
		execFFMPEG(cmd, sc);
		
		return mediaOut;
	}

	public Clip extractAudio(String inputPath, String outPath, boolean force2mp3, ShellCallback sc) throws Exception {
		ArrayList<String> cmd = new ArrayList<String>();
		cmd = new ArrayList<String>();
		String mediaPath = inputPath;
		Clip mediaOut = new Clip();

		cmd.add(mFfmpegBin);
		cmd.add("-y");
		cmd.add("-i");
		cmd.add(mediaPath);


		if(force2mp3) {
			cmd.add("-b:a");
			cmd.add("192K");
		} else {
			cmd.add("-acodec");
			cmd.add("copy");
		}

		cmd.add("-vn");

		mediaOut.path = outPath;
		cmd.add(mediaOut.path);

		execFFMPEG(cmd, sc);

		return mediaOut;
	}

	public Clip trimFlacAudio(String inputPath, String outPath, float startTime, float endTime, int sampleRates, int bitRates, int channels, ShellCallback sc) throws Exception {
		ArrayList<String> cmd = new ArrayList<String>();
		cmd = new ArrayList<String>();
		String mediaPath = inputPath;
		Clip mediaOut = new Clip();

		cmd.add(mFfmpegBin);
		cmd.add("-y");
		if(startTime != Float.MIN_VALUE) {
			cmd.add("-ss");
			cmd.add(String.format("%.04f", startTime));
		}
		cmd.add("-i");
		cmd.add(mediaPath);
		if(endTime != Float.MAX_VALUE) {
			cmd.add("-t");
			cmd.add(String.format("%.04f", endTime - startTime));
		}

		cmd.add("-acodec");
		cmd.add("flac");
		cmd.add("-bits_per_raw_sample");
		cmd.add(String.format("%d", bitRates));
		cmd.add("-ar");
		cmd.add(String.format("%d", sampleRates));
		cmd.add("-ac");
		cmd.add(String.format("%d", channels));

		cmd.add("-vn");
		mediaOut.path = outPath;
		cmd.add(mediaOut.path);

		execFFMPEG(cmd, sc);

		return mediaOut;
	}

	public void silenceDetect(String inputPath, float noiseTolerance, float silenceDuration, ShellCallback sc) throws Exception {
		ArrayList<String> cmd = new ArrayList<String>();
		cmd = new ArrayList<String>();
		String mediaPath = inputPath;

		cmd.add(mFfmpegBin);
		cmd.add("-i");
		cmd.add(mediaPath);

		cmd.add("-af");
		cmd.add("silencedetect=noise=" + String.format("%.02f", noiseTolerance) + "dB:duration=" +  String.format("%.04f", silenceDuration));
		cmd.add("-f");
		cmd.add("null");
		cmd.add("-");

		execFFMPEG(cmd, sc);
	}

	public void wma2mp3(String inputPath, String outputPath, ShellCallback sc) throws Exception {
		ArrayList<String> cmd = new ArrayList<String>();

		cmd.add(mFfmpegBin);
		cmd.add("-i");
		cmd.add(inputPath);
		cmd.add(outputPath);

		execFFMPEG(cmd, sc);
	}

	public void volumeDetect(String inputPath, float startTime, float duration, ShellCallback sc) throws Exception {
		ArrayList<String> cmd = new ArrayList<String>();
		cmd = new ArrayList<String>();
		String mediaPath = inputPath;

		cmd.add(mFfmpegBin);
		cmd.add("-i");
		cmd.add(mediaPath);

		cmd.add("-ss");
		cmd.add(String.format("%.04f", startTime));
		if(duration != Float.MAX_VALUE) {
			cmd.add("-t");
			cmd.add(String.format("%.04f", duration));
		}

		cmd.add("-af");
		cmd.add("volumedetect");
		cmd.add("-f");
		cmd.add("null");
		cmd.add("-");

		execFFMPEG(cmd, sc);
	}

	public Clip concatVideos(String listPath, String outPath, ShellCallback sc) throws Exception {
		ArrayList<String> cmd = new ArrayList<String>();
		cmd = new ArrayList<String>();
		Clip mediaOut = new Clip();

		cmd.add(mFfmpegBin);
		cmd.add("-y");
		cmd.add("-safe");
		cmd.add("0");
		cmd.add("-f");

		cmd.add("concat");
		cmd.add("-i");
		cmd.add(listPath);

		cmd.add("-c");
		cmd.add("copy");

		mediaOut.path = outPath;
		cmd.add(mediaOut.path);

		execFFMPEG(cmd, sc);

		return mediaOut;
	}

	public Clip getVideoFrame(String inputPath, float timestamp, String outPath, ShellCallback sc) throws Exception {
		ArrayList<String> cmd = new ArrayList<String>();
		cmd = new ArrayList<String>();
		String mediaPath = inputPath;
		Clip mediaOut = new Clip();

		cmd.add(mFfmpegBin);
		cmd.add("-y");
		cmd.add("-i");
		cmd.add(mediaPath);

		cmd.add("-ss");
		cmd.add(String.format("%.02f", timestamp));
		cmd.add("-vframes");
		cmd.add("1");

		mediaOut.path = outPath;
		cmd.add(mediaOut.path);

		execFFMPEG(cmd, sc);

		return mediaOut;
	}

	public Clip getInfo (Clip in) throws IOException, InterruptedException
	{
		ArrayList<String> cmd = new ArrayList<String>();

		cmd = new ArrayList<String>();

		cmd.add(mFfmpegBin);
		cmd.add("-y");
		cmd.add("-i");

		cmd.add(new File(in.path).getCanonicalPath());

		InfoParser ip = new InfoParser(in);
		execFFMPEG(cmd,ip, null);
/*
		try{Thread.sleep(200);}
		catch (Exception e){}
*/

		return in;

	}

	private class InfoParser implements ShellCallback {
		
		private Clip mMedia;
		private int retValue;
		
		public InfoParser (Clip media)
		{
			mMedia = media;
		}

		@Override
		public void shellOut(String shellLine) {
			if (shellLine.contains("Duration:"))
			{
				
//		  Duration: 00:01:01.75, start: 0.000000, bitrate: 8184 kb/s

				String[] timecode = shellLine.split(",")[0].split(":");

				
				double duration = 0;
				
				duration = Double.parseDouble(timecode[1].trim())*60*60; //hours
				duration += Double.parseDouble(timecode[2].trim())*60; //minutes
				duration += Double.parseDouble(timecode[3].trim()); //seconds
				
				mMedia.duration = duration;
				
			
			}
			
			//   Stream #0:0(eng): Video: h264 (High) (avc1 / 0x31637661), yuv420p, 1920x1080, 16939 kb/s, 30.02 fps, 30 tbr, 90k tbn, 180k tbc
			else if (shellLine.contains(": Video:"))
			{
				String[] line = shellLine.split(":");
				String[] videoInfo = line[3].split(",");
				
				mMedia.videoCodec = videoInfo[0];
			}
			
			//Stream #0:1(eng): Audio: aac (mp4a / 0x6134706D), 48000 Hz, stereo, s16, 121 kb/s
			else if (shellLine.contains(": Audio:"))
			{
				String[] line = shellLine.split(":");
				String[] audioInfo = line[3].split(",");
				
				mMedia.audioCodec = audioInfo[0];
				
			}
			    
	
	//
    //Stream #0.0(und): Video: h264 (Baseline), yuv420p, 1280x720, 8052 kb/s, 29.97 fps, 90k tbr, 90k tbn, 180k tbc
    //Stream #0.1(und): Audio: mp2, 22050 Hz, 2 channels, s16, 127 kb/s
    
		}

		@Override
		public void processComplete(int exitValue) {
			retValue = exitValue;

		}
	}
	
	private class StreamGobbler extends Thread
	{
	    InputStream is;
	    String type;
	    ShellCallback sc;
	    
	    StreamGobbler(InputStream is, String type, ShellCallback sc)
	    {
	        this.is = is;
	        this.type = type;
	        this.sc = sc;
	    }
	    
	    public void run()
	    {
	        try
	        {
	            InputStreamReader isr = new InputStreamReader(is);
	            BufferedReader br = new BufferedReader(isr);
	            String line=null;
	            while ( (line = br.readLine()) != null)
	            	if (sc != null)
	            		sc.shellOut(line);
	                
	            } catch (IOException ioe)
	              {
	             //   Log.e(TAG,"error reading shell slog",ioe);
	            	ioe.printStackTrace();
	              }
	    }
	}
	
	public static Bitmap getVideoFrame(String videoPath,long frameTime) throws Exception {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        
        try {
            retriever.setDataSource(videoPath);      
            return retriever.getFrameAtTime(frameTime, MediaMetadataRetriever.OPTION_CLOSEST);
       
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
            }
        }
    }
}
