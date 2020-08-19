package edu.osu.cws.evals.util;

import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.io.*;
import java.util.Date;
import java.util.Calendar;
import javax.xml.bind.DatatypeConverter;
import javax.net.ssl.HttpsURLConnection;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

public class EvalsOnbase {

  private static final String LINE_FEED = "\r\n";
  private static final String CHARSET = "UTF-8";

  private String clientId;
  private String clientSecret;

  private String bearerToken;
  private Date tokenExpirationDate;

  private String oauth2Url;
  private String onbaseDocsUrl;

  public EvalsOnbase(String clientId,
                     String clientSecret,
                     String oauth2Url,
                     String onbaseDocsUrl
  ) throws IOException, MalformedURLException, ParseException {
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.oauth2Url = oauth2Url;
    this.onbaseDocsUrl = onbaseDocsUrl;
  };

  private HttpsURLConnection openConnection(String urlString) throws IOException,
                                                                     MalformedURLException {
    URL url = new URL(urlString);
    HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
    return conn;
  }

  private void writeBody(HttpsURLConnection conn, String body) throws IOException {
    OutputStream op = conn.getOutputStream();
    op.write(body.getBytes(CHARSET));
    op.close();
  }

  private JSONObject readResponse(HttpsURLConnection conn) throws IOException, ParseException {
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

  private void setBearerToken() throws IOException, MalformedURLException, ParseException {
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
    int expiresIn = Integer.parseInt(jsonResponse.get("expires_in").toString());

    Date date = new Date();
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.add(Calendar.SECOND, expiresIn);
    tokenExpirationDate = cal.getTime();
  }

  private void checkBearerToken() throws IOException, MalformedURLException, ParseException {
    if (tokenExpirationDate == null || tokenExpirationDate.compareTo(new Date()) < 0) {
      setBearerToken();
    }
  }

  private void setAuthHeader(HttpsURLConnection conn) {
    conn.setRequestProperty("Authorization", "Bearer " + bearerToken);
  }

  public void postPDF(String pdfName) throws IOException, MalformedURLException, ParseException {
    String boundary = "---" + System.currentTimeMillis() + "---";

    checkBearerToken();
    HttpsURLConnection conn = openConnection(onbaseDocsUrl);
    conn.setRequestMethod("POST");
    conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
    setAuthHeader(conn);
    conn.setDoOutput(true);

    OutputStream outputStream = conn.getOutputStream();
    PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, CHARSET), true);

    JSONObject attributes = new JSONObject();
    attributes.put("DocumentType", "TEST - Sample Paper Form");
    attributes.put("Comment", pdfName);
    attributes.put("IndexKey", "999999999");

    writer.append("--" + boundary).append(LINE_FEED);
    writer.append("Content-Disposition: form-data; name=\"attributes\"").append(LINE_FEED);
    writer.append("Content-Type: text/plain; charset=" + CHARSET).append(LINE_FEED);
    writer.append(LINE_FEED);
    writer.append(attributes.toString()).append(LINE_FEED);
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

  public void getDoc() throws IOException, MalformedURLException, ParseException {
    checkBearerToken();
    HttpsURLConnection conn = openConnection(onbaseDocsUrl + "115542");
    setAuthHeader(conn);

    JSONObject response = readResponse(conn);
    System.out.println(response.toString());

    conn.disconnect();
  }
}
