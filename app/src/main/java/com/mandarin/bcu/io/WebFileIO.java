package com.mandarin.bcu.io;

public class WebFileIO {

}
/*
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Consumer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpBackOffIOExceptionHandler;
import com.google.api.client.http.HttpBackOffUnsuccessfulResponseHandler;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.ExponentialBackOff;

import main.Opts;

public class WebFileIO {

	private static final int CHUNK_SIZE = 131072;

	private static HttpTransport transport;

	public static boolean download(String url, File file, Consumer<Progress> c) {
		Writer.check(file);
		try {
			if (transport == null)
				transport = GoogleNetHttpTransport.newTrustedTransport();

			OutputStream out = new FileOutputStream(file);
			GenericUrl gurl = new GenericUrl(url);

			MediaHttpDownloader downloader = new MediaHttpDownloader(transport, new Handler());
			downloader.setChunkSize(CHUNK_SIZE);
			downloader.setProgressListener(new Progress(c));
			downloader.download(gurl, out);

			out.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			Opts.dloadErr(url);
			return false;
		}
	}

	public static boolean upload(File file, String url) throws IOException {
		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost post = new HttpPost(url);
		FileBody fileBody = new FileBody(file, ContentType.DEFAULT_BINARY);
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		builder.addPart("catFile", fileBody);

		HttpEntity reqEntity = builder.build();
		post.setEntity(reqEntity);
		HttpResponse response = client.execute(post);

		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode == 200)
			return true;
		System.err.println("statusCode: " + statusCode);
		HttpEntity respEntity = response.getEntity();
		String responseString = EntityUtils.toString(respEntity, "UTF-8");
		System.err.println("response body: ");
		System.err.println(responseString);
		return false;
	}

}

class Handler implements HttpRequestInitializer {

	@Override
	public void initialize(HttpRequest request) throws IOException {
		request.setUnsuccessfulResponseHandler(new HttpBackOffUnsuccessfulResponseHandler(new ExponentialBackOff()));
		request.setIOExceptionHandler(new HttpBackOffIOExceptionHandler(new ExponentialBackOff()));
	}

}
*/