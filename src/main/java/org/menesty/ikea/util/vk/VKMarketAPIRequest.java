package org.menesty.ikea.util.vk;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.message.BasicNameValuePair;
import org.menesty.ikea.ApplicationPreference;
import org.menesty.ikea.domain.vk.*;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Menesty on
 * 7/12/16.
 * 14:11.
 */
public class VKMarketAPIRequest extends VKAPIRequest {
  private static final String API_URL = "https://api.vk.com/method/";

  public VKMarketAPIRequest(ApplicationPreference applicationPreference) throws URISyntaxException {
    super(applicationPreference);
  }

  public VKData<VKMarketItem> get() throws Exception {
    return apiGet("market.get", new TypeReference<VKResponse<VKData<VKMarketItem>>>() {
        },
        new BasicNameValuePair("owner_id", String.valueOf(getGroupId())),
        new BasicNameValuePair("extended", "1"),
        new BasicNameValuePair("count", "200")).getResponse();
  }

  public VKData<VKMarketItem> get(Long albumId) throws Exception {
    return apiGet("market.get", new TypeReference<VKResponse<VKData<VKMarketItem>>>() {
        },
        new BasicNameValuePair("owner_id", String.valueOf(getGroupId())),
        new BasicNameValuePair("extended", "1"),
        new BasicNameValuePair("album_id", albumId.toString()),
        new BasicNameValuePair("count", "200")).getResponse();
  }


  public VKData<VKMarketAlbum> getAlbums() throws Exception {
    return apiGet("market.getAlbums", new TypeReference<VKResponse<VKData<VKMarketAlbum>>>() {
        },
        new BasicNameValuePair("owner_id", String.valueOf(getGroupId())),
        new BasicNameValuePair("count", "100")).getResponse();

  }

  public VKData<VKCategory> getCategories() throws Exception {
    return apiGet("market.getCategories", new TypeReference<VKResponse<VKData<VKCategory>>>() {
    }).getResponse();
  }

  private <T> T apiGet(String method, TypeReference<T> typeReference, NameValuePair... params) throws Exception {
    return apiGet(method, typeReference, Arrays.asList(params));
  }

  private <T> T apiGet(String method, TypeReference<T> typeReference, List<NameValuePair> params) throws Exception {
    if (getAccessToken() == null) {
      authorize();
    }

    String content = getContent(new HttpGet(getApiUrl(method, params)));
    //System.out.println(content);
    return objectMapper.readValue(content, typeReference);
  }

  private URI getApiUrl(String method, List<NameValuePair> params) throws URISyntaxException {
    String url = API_URL + method + "?v=" + API_VERSION + "&access_token=" + getAccessToken().getToken();

    if (!params.isEmpty()) {
      url += "&" + URLEncodedUtils.format(params, "utf-8");
    }

    //System.out.println(url);
    return new URI(url);
  }

  private URI postApiUrl(String method) throws URISyntaxException {
    String url = API_URL + method + "?v=" + API_VERSION + "&access_token=" + getAccessToken().getToken();

    //System.out.println(url);
    return new URI(url);
  }

  public static void main(String... arg) throws Exception {
    String bla = "[{\\\"photo\\\":\\\"2b7c1ac827:w\\\",\\\"sizes\\\":[[\\\"s\\\",\\\"630621157\\\",\\\"3e357\\\",\\\"SrPhyc5cwxg\\\",75,75],[\\\"m\\\",\\\"630621157\\\",\\\"3e358\\\",\\\"wLWlWp6HRxs\\\",130,130],[\\\"x\\\",\\\"630621157\\\",\\\"3e359\\\",\\\"H_rGUtGlTvo\\\",604,604],[\\\"y\\\",\\\"630621157\\\",\\\"3e35a\\\",\\\"6CTAoKW8HQA\\\",807,807],[\\\"z\\\",\\\"630621157\\\",\\\"3e35b\\\",\\\"ZRbRSI8-gvY\\\",1080,1080],[\\\"w\\\",\\\"630621157\\\",\\\"3e35c\\\",\\\"3sX4YKFJnA8\\\",2000,2000],[\\\"o\\\",\\\"630621157\\\",\\\"3e35d\\\",\\\"c9nIUfqM-nI\\\",130,130],[\\\"p\\\",\\\"630621157\\\",\\\"3e35e\\\",\\\"TFHZW7ypC4k\\\",200,200],[\\\"q\\\",\\\"630621157\\\",\\\"3e35f\\\",\\\"qFwvQS-AefE\\\",320,320],[\\\"r\\\",\\\"630621157\\\",\\\"3e360\\\",\\\"5--dYlIsuKQ\\\",510,510]],\\\"kid\\\":\\\"8fc401f8c9cb67e8e45ad401323e7fcb\\\",\\\"debug\\\":\\\"xswmwxwywzwwwowpwqwrw\\\"}]";

    System.out.println(bla.replace("\\", ""));
  }


  public Long uploadPhotoAlbum(File file) throws Exception {
    VKUploadServer uploadServer = apiGet("photos.getMarketAlbumUploadServer", new TypeReference<VKResponse<VKUploadServer>>() {
    }, new BasicNameValuePair("group_id", String.valueOf(getGroupId() * -1))).getResponse();


    HttpPost post = new HttpPost(uploadServer.getUploadServerUrl());

    FileBody fileBody = new FileBody(file);

    HttpEntity entity = MultipartEntityBuilder.create()
        .addPart("file", fileBody)
        .build();

    post.setEntity(entity);

    String response = post(post);
    //System.out.println(response);

    VKUploadPhotoAlbumResult result = objectMapper.readValue(response, VKUploadPhotoAlbumResult.class);

    List<NameValuePair> params = new ArrayList<>();

    params.add(new BasicNameValuePair("group_id", String.valueOf(getGroupId() * -1)));
    params.add(new BasicNameValuePair("photo", result.getPhoto()));
    params.add(new BasicNameValuePair("server", String.valueOf(result.getServer())));
    params.add(new BasicNameValuePair("hash", String.valueOf(result.getHash())));

    response = getContent(new HttpGet(getApiUrl("photos.saveMarketAlbumPhoto", params)));

    VKResponse<List<VKPhotoUploadResult>> data = objectMapper.readValue(response, new TypeReference<VKResponse<List<VKPhotoUploadResult>>>() {
    });

    if (!data.getResponse().isEmpty()) {
      return data.getResponse().get(0).getId();
    }

    return null;
  }

  public Long uploadPhotoProduct(String artNumber, boolean main) throws Exception {
    VKUploadServer uploadServer = apiGet("photos.getMarketUploadServer", new TypeReference<VKResponse<VKUploadServer>>() {
        }, new BasicNameValuePair("group_id", String.valueOf(getGroupId() * -1)),
        new BasicNameValuePair("main_photo", main ? "1" : "0")).getResponse();


    final File tempFile = File.createTempFile(artNumber, ".jpg");
    tempFile.deleteOnExit();

    FileUtils.copyURLToFile(new URI(photoServer.getScheme(), photoServer.getHost(), "/image/zoom/" + artNumber, null).toURL(), tempFile);

    HttpPost post = new HttpPost(uploadServer.getUploadServerUrl());

    FileBody fileBody = new FileBody(tempFile);
    HttpEntity entity = MultipartEntityBuilder.create()
        .addPart("file", fileBody)
        .build();

    post.setEntity(entity);

    String response = post(post);
    //System.out.println(response);

    VKUploadPhotoProductResult result = objectMapper.readValue(response, VKUploadPhotoProductResult.class);

    List<NameValuePair> params = new ArrayList<>();

    params.add(new BasicNameValuePair("group_id", String.valueOf(getGroupId() * -1)));
    params.add(new BasicNameValuePair("photo", result.getPhoto().replace("\\", "")));
    params.add(new BasicNameValuePair("server", String.valueOf(result.getServer())));
    params.add(new BasicNameValuePair("hash", String.valueOf(result.getHash())));

    if (main) {
      params.add(new BasicNameValuePair("crop_data", String.valueOf(result.getCropData())));
      params.add(new BasicNameValuePair("crop_hash", result.getCropHash()));
    }

    response = getContent(new HttpGet(getApiUrl("photos.saveMarketPhoto", params)));
    //System.out.println(response);

    VKResponse<List<VKPhotoUploadResult>> data = objectMapper.readValue(response, new TypeReference<VKResponse<List<VKPhotoUploadResult>>>() {
    });

    if (!data.getResponse().isEmpty()) {
      return data.getResponse().get(0).getId();
    }
    return null;
  }


  public VKData<VKPhotoAlbum> getPhotosAlbums() throws Exception {
    return apiGet("photos.getAlbums", new TypeReference<VKResponse<VKData<VKPhotoAlbum>>>() {
        }, new BasicNameValuePair("owner_id", String.valueOf(getGroupId())), new BasicNameValuePair("need_system", "1"),
        new BasicNameValuePair("need_covers", "1")).getResponse();
  }

  public VKData<VKPhoto> getPhotosByAlbum(long albumId) throws Exception {
    return apiGet("photos.get", new TypeReference<VKResponse<VKData<VKPhoto>>>() {
    }, new BasicNameValuePair("owner_id", String.valueOf(getGroupId())), new BasicNameValuePair("album_id", albumId + ""))
        .getResponse();
  }

  public long addAlbum(VKMarketAlbum album) throws Exception {
    List<NameValuePair> params = new ArrayList<>();

    params.add(new BasicNameValuePair("owner_id", String.valueOf(getGroupId())));
    params.add(new BasicNameValuePair("title", album.getTitle()));
    params.add(new BasicNameValuePair("main_album", "0"));

    if (album.getPhotoId() != null) {
      params.add(new BasicNameValuePair("photo_id", album.getPhotoId().toString()));
    }

    return apiGet("market.addAlbum", new TypeReference<VKResponse<VKMarketAlbumAddResult>>() {
    }, params)
        .getResponse().getAlbumId();
  }

  public boolean editAlbum(VKMarketAlbum album) throws Exception {
    List<NameValuePair> params = new ArrayList<>();

    params.add(new BasicNameValuePair("owner_id", String.valueOf(getGroupId())));
    params.add(new BasicNameValuePair("title", album.getTitle()));
    params.add(new BasicNameValuePair("main_album", "0"));
    params.add(new BasicNameValuePair("album_id", album.getId().toString()));

    if (album.getPhotoId() != null) {
      params.add(new BasicNameValuePair("photo_id", album.getPhotoId().toString()));
    }

    return apiGet("market.editAlbum", new TypeReference<VKResponse<Long>>() {
    }, params)
        .getResponse() == 1;
  }

  public Boolean deleteAlbum(VKMarketAlbum selected) throws Exception {
    return apiGet("market.deleteAlbum", new TypeReference<VKResponse<Long>>() {
    }, new BasicNameValuePair("owner_id", String.valueOf(getGroupId())), new BasicNameValuePair("album_id", selected.getId().toString())).getResponse() == 1;
  }

  public void editProduct(VKMarketProduct product) throws Exception {

    List<NameValuePair> params = new ArrayList<>();

    params.add(new BasicNameValuePair("owner_id", String.valueOf(getGroupId())));
    params.add(new BasicNameValuePair("item_id", product.getId().toString()));
    params.add(new BasicNameValuePair("name", product.getName()));
    params.add(new BasicNameValuePair("description", product.getDescription()));
    params.add(new BasicNameValuePair("category_id", product.getCategoryId().toString()));
    params.add(new BasicNameValuePair("price", product.getPrice().toString()));
    params.add(new BasicNameValuePair("deleted", product.isDeleted() ? "1" : "0"));
    params.add(new BasicNameValuePair("main_photo_id", product.getMainPhotoId().toString()));
    params.add(new BasicNameValuePair("photo_ids", product.getPhotoIds() != null ? product.getPhotoIds() : ""));

    String response = postContent(new HttpPost(postApiUrl("market.edit")), params);
    //System.out.println(response);
  }

  public Long addProduct(VKMarketProduct product) throws Exception {
    List<NameValuePair> params = new ArrayList<>();

    params.add(new BasicNameValuePair("owner_id", String.valueOf(getGroupId())));
    params.add(new BasicNameValuePair("name", product.getName()));
    params.add(new BasicNameValuePair("description", product.getDescription()));
    params.add(new BasicNameValuePair("category_id", product.getCategoryId().toString()));
    params.add(new BasicNameValuePair("price", product.getPrice().toString()));
    params.add(new BasicNameValuePair("deleted", product.isDeleted() ? "1" : "0"));
    params.add(new BasicNameValuePair("main_photo_id", product.getMainPhotoId().toString()));
    params.add(new BasicNameValuePair("photo_ids", product.getPhotoIds() != null ? product.getPhotoIds() : ""));

    String response = postContent(new HttpPost(postApiUrl("market.add")), params);
    VKResponse<VKMarketProductAddResult> id = objectMapper.readValue(response, new TypeReference<VKResponse<VKMarketProductAddResult>>() {
    });

    return id.getResponse().getItemId();
  }

  public void addToAlbum(Long itemId, Long albumId) throws Exception {
    apiGet("market.addToAlbum", new TypeReference<VKResponse<Long>>() {
        }, new BasicNameValuePair("owner_id", String.valueOf(getGroupId())),
        new BasicNameValuePair("item_id", itemId.toString()),
        new BasicNameValuePair("album_ids", albumId.toString()));
  }
}
