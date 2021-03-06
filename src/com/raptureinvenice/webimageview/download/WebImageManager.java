/*
	Copyright (c) 2011 Rapture In Venice

	Permission is hereby granted, free of charge, to any person obtaining a copy
	of this software and associated documentation files (the "Software"), to deal
	in the Software without restriction, including without limitation the rights
	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	copies of the Software, and to permit persons to whom the Software is
	furnished to do so, subject to the following conditions:

	The above copyright notice and this permission notice shall be included in
	all copies or substantial portions of the Software.

	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
	THE SOFTWARE.
*/

package com.raptureinvenice.webimageview.download;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.graphics.Bitmap;

import com.raptureinvenice.webimageview.download.WebImageManagerRetriever.OnWebImageLoadListener;
import com.raptureinvenice.webimageview.image.WebImageView;

public class WebImageManager implements OnWebImageLoadListener {
	private static WebImageManager mInstance = null;
	
	// TODO: pool retrievers
	
	// views waiting for an image to load in
	private Map<String, WebImageManagerRetriever> mRetrievers;
	private Map<WebImageView, String> viewsAwaitingImage;
	
	public static WebImageManager getInstance() {
		if (mInstance == null) {
			mInstance = new WebImageManager();
		}
		
		return mInstance;
	}
	
	private WebImageManager() {
		mRetrievers = new HashMap<String, WebImageManagerRetriever>();
		viewsAwaitingImage = new HashMap<WebImageView, String>();
	}

	public void downloadURL(Context context, String urlString, final WebImageView view, int diskCacheTimeoutInSeconds) {
		WebImageManagerRetriever retriever = mRetrievers.get(urlString);

		viewsAwaitingImage.put(view, urlString);

		if (mRetrievers.get(urlString) == null) {
			retriever = new WebImageManagerRetriever(context, urlString, diskCacheTimeoutInSeconds, this);
			mRetrievers.put(urlString, retriever);
			retriever.execute();
		}
	}

    public void reportImageLoad(String urlString, Bitmap bitmap) {
    	Map<WebImageView, String> waiters = new HashMap<WebImageView, String>(viewsAwaitingImage);
    	for (Entry<WebImageView, String> entry : waiters.entrySet()) {
    		if (entry.getValue().equals(urlString)) {
    			entry.getKey().setImageBitmap(bitmap);
    			viewsAwaitingImage.remove(entry.getKey());
    		}
		}

        mRetrievers.remove(urlString);
    }

	public void cancelForWebImageView(WebImageView view) {
		viewsAwaitingImage.remove(view);
	}

    @Override
    public void onWebImageLoad(String url, Bitmap bitmap) {
        reportImageLoad(url, bitmap);
    }

    @Override
    public void onWebImageError() {
    }
}
