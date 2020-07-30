package com.example.blog.model;

public class Blog {
    private String title,description,image_url,name,posted;
    public Blog(){

    }
    public Blog(String title, String description, String image_url,String name,String posted) {
        this.title = title;
        this.description = description;
        this.image_url = image_url;
        this.name=name;
    this.posted=posted;

    }

    public String getPosted() {
        return posted;
    }

    public void setPosted(String posted) {
        this.posted = posted;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }
}
