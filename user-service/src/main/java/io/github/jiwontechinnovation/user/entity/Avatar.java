package io.github.jiwontechinnovation.user.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "avatars")
public class Avatar {
    @Id
    @Column(unique = true, nullable = false)
    private String id; // e.g., "robot", "human_1"

    @Column(nullable = false)
    private String name; // e.g., "Basic Robot"

    @Column(name = "s3_url", nullable = false)
    private String s3Url; // e.g., "https://example-bucket.s3.amazonaws.com/avatars/robot.png"

    protected Avatar() {
    }

    public Avatar(String id, String name, String s3Url) {
        this.id = id;
        this.name = name;
        this.s3Url = s3Url;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getS3Url() {
        return s3Url;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setS3Url(String s3Url) {
        this.s3Url = s3Url;
    }
}
