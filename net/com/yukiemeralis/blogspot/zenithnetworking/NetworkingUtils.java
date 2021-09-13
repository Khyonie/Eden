package com.yukiemeralis.blogspot.zenithnetworking;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import com.yukiemeralis.blogspot.zenith.utils.PrintUtils;
import com.yukiemeralis.blogspot.zenith.utils.PrintUtils.InfoType;

public class NetworkingUtils
{
	/**
	 * Downloads a resource from a URL.
	 * @param url The URL to download from
	 * @param targetFilepath The place to store the downloaded file
	 * @return The downloaded file
	 * @throws IOException If the connection is interrupted.
	 * @throws MalformedURLException If the resource does not exist.
	 */
	public static File downloadFileFromURL(String url, String targetFilepath) throws IOException, MalformedURLException
	{
		URL websiteTarget = new URL(url);
		File f = new File(targetFilepath);
		
		long bytes = 0;
		try (InputStream in = websiteTarget.openStream())
		{
			bytes = Files.copy(in, f.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		
		PrintUtils.log("Downloaded [" + bytes + "] byte(s) from URL \"{" + url + "}\".", InfoType.INFO);
		
		return f;
	}
	
	/**
	 * Downloads a resource from a URL inside a thread.
	 * @param url The URL to download from
	 * @param targetFilepath The place to store the downloaded file
	 * @param thread The thread to fire when the file has finished downloading
	 */
	public static void downloadFileFromURLThreaded(String url, String targetFilepath, Thread thread)
	{
		Thread dl_thread = new Thread()
		{
			@Override
			public void run()
			{
				try {
					URL websiteTarget = new URL(url);
					File f = new File(targetFilepath);
					
					long bytes = 0;
					try (InputStream in = websiteTarget.openStream())
					{
						bytes = Files.copy(in, f.toPath(), StandardCopyOption.REPLACE_EXISTING);
					}
					
					PrintUtils.log("Downloaded [" + bytes + "] byte(s) from URL {\"" + url + "}\".", InfoType.INFO);
					thread.start();
				} catch (IOException e) {
					thread.start();
					PrintUtils.printPrettyStacktrace(e);
				}	
			}
		};
		
		dl_thread.start();
	}
	
	public static String getFinalURLPortion(String url)
	{
		return url.split("/")[url.split("/").length - 1];
	}
}
