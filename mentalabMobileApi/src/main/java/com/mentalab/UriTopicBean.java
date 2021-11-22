package com.mentalab;

import android.net.Uri;

public class UriTopicBean {

    private Uri uri;
    private MentalabEnums.Topics topic;


    public UriTopicBean(Uri uri, MentalabEnums.Topics topic) {
        this.uri = uri;
        this.topic = topic;
    }


    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public MentalabEnums.Topics getTopic() {
        return topic;
    }

    public void setTopic(MentalabEnums.Topics topic) {
        this.topic = topic;
    }
}
