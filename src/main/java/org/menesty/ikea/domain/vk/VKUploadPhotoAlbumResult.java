package org.menesty.ikea.domain.vk;

/**
 * Created by Menesty on
 * 7/14/16.
 * 17:13.
 */
public class VKUploadPhotoAlbumResult {
  private int server;
  private String photo;
  private long gid;
  private String hash;

  public int getServer() {
    return server;
  }

  public void setServer(int server) {
    this.server = server;
  }

  public String getPhoto() {
    return photo;
  }

  public void setPhoto(String photo) {
    this.photo = photo;
  }

  public long getGid() {
    return gid;
  }

  public void setGid(long gid) {
    this.gid = gid;
  }

  public String getHash() {
    return hash;
  }

  public void setHash(String hash) {
    this.hash = hash;
  }
}
