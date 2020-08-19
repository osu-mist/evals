package edu.osu.cws.evals.util;

import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.io.*;
import javax.xml.bind.DatatypeConverter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class EvalsOnbase {

  private static final String LINE_FEED = "\r\n";
  private static final String CHARSET = "UTF-8";

  private String clientId;
  private String clientSecret;

  private String bearerToken;
  private String tokenExpirationDate;

  private String oauth2Url;
  private String onbaseDocsUrl;

  public EvalsOnbase(String clientId, String clientSecret, String oauth2Url, String onbaseDocsUrl) throws Exception {
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.oauth2Url = oauth2Url;
    this.onbaseDocsUrl = onbaseDocsUrl;
    setBearerToken();
  };

  private HttpsURLConnection openConnection(String urlString) throws Exception {
    URL url = new URL(urlString);
    HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
    return conn;
  }

  private void writeBody(HttpsURLConnection conn, String body) throws Exception {
    OutputStream op = conn.getOutputStream();
    op.write(body.getBytes(CHARSET));
    op.close();
  }

  private JSONObject readResponse(HttpsURLConnection conn) throws Exception {
    int status = conn.getResponseCode();

    InputStream is;
    if (status == HttpsURLConnection.HTTP_OK || status == HttpsURLConnection.HTTP_CREATED) {
      is = conn.getInputStream();
    } else {
      is = conn.getErrorStream();
    }
    InputStreamReader isr = new InputStreamReader(is);
    BufferedReader br = new BufferedReader(isr);
    String inputLine;
    String response = "";
    while ((inputLine = br.readLine()) != null) {
      response += inputLine;
    }
    br.close();

    JSONParser parser = new JSONParser();
    JSONObject jsonResponse = (JSONObject) parser.parse(response);
    return jsonResponse;
  }

  private void setBearerToken() throws Exception {
    HttpsURLConnection conn = openConnection(oauth2Url);
    conn.setRequestMethod("POST");
    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
    conn.setDoOutput(true);

    String body = "grant_type=client_credentials";
    body += "&client_id=" + clientId;
    body += "&client_secret=" + clientSecret;

    writeBody(conn, body);

    JSONObject jsonResponse = readResponse(conn);

    bearerToken = jsonResponse.get("access_token").toString();
    tokenExpirationDate = jsonResponse.get("expires_in").toString();
  }

  private void setAuthHeader(HttpsURLConnection conn) throws Exception {
    conn.setRequestProperty("Authorization", "Bearer " + bearerToken);
  }

  public void postPDF() throws Exception {
    String boundary = "---" + System.currentTimeMillis() + "---";

    HttpsURLConnection conn = openConnection(onbaseDocsUrl);
    conn.setRequestMethod("POST");
    conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
    setAuthHeader(conn);
    conn.setDoOutput(true);

    OutputStream outputStream = conn.getOutputStream();
    PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, CHARSET), true);

    String attributes = "{ \"DocumentType\": \"TEST - Sample Paper Form\",\"Comment\": \"This is a sample document.\",\"IndexKey\": \"999999999\"}";
    writer.append("--" + boundary).append(LINE_FEED);
    writer.append("Content-Disposition: form-data; name=\"attributes\"").append(LINE_FEED);
    writer.append("Content-Type: text/plain; charset=" + CHARSET).append(LINE_FEED);
    writer.append(LINE_FEED);
    writer.append(attributes).append(LINE_FEED);
    writer.flush();

    String fileName = "onbase-test.pdf";
    String filePath = "/opt/evals/pdf/" + fileName;
    File uploadFile = new File(filePath);
    writer.append("--" + boundary).append(LINE_FEED);
    writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"")
          .append(LINE_FEED);
    writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(fileName))
          .append(LINE_FEED);
    writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
    writer.append(LINE_FEED);
    writer.flush();

    FileInputStream inputStream = new FileInputStream(uploadFile);
    byte[] buffer = new byte[4096];
    int bytesRead = -1;
    while ((bytesRead = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
    }
    outputStream.flush();
    inputStream.close();
    writer.append(LINE_FEED);
    writer.flush();

    writer.append(LINE_FEED).flush();
    writer.append("--" + boundary + "--").append(LINE_FEED);
    writer.close();

    JSONObject response = readResponse(conn);
    System.out.println(response.toString());

    conn.disconnect();
  }

  public void getDoc() throws Exception {
    HttpsURLConnection conn = openConnection(onbaseDocsUrl + "115542");
    setAuthHeader(conn);

    JSONObject response = readResponse(conn);
    System.out.println(response.toString());

    conn.disconnect();
  }
}
