package com.mentalab;

import android.net.Uri;

public class UriTopicBean {

    private Uri uri;
    private MentalabConstants.Topic topic;


    public UriTopicBean(Uri uri, MentalabConstants.Topic topic) {
        this.uri = uri;
        this.topic = topic;
    }


    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public MentalabConstants.Topic getTopic() {
        return topic;
    }

    public void setTopic(MentalabConstants.Topic topic) {
        this.topic = topic;
    }
}
