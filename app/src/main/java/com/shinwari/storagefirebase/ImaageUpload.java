package com.shinwari.storagefirebase;

public class ImaageUpload {

    public  String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String uri;

    public ImaageUpload(String name, String uri) {
        this.name = name;
        this.uri = uri;
        ///////////new aachelments in the project
    }
}
