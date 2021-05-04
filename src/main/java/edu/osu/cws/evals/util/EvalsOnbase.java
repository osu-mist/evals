package edu.osu.cws.evals.util;

import edu.osu.cws.evals.models.Employee;
import edu.osu.cws.evals.models.Job;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.io.*;
import java.util.Date;
import java.util.Calendar;
import javax.xml.bind.DatatypeConverter;
import javax.net.ssl.HttpsURLConnection;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
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

  private String boundary;
  private String pdfDestination;

  private String classifiedDocType;
  private String rankedDocType;

  /**
    * Constructor for onbase API requests
    *
    * @param clientId oauth2 client ID
    * @param clientSecret oauth2 client secret
    * @param oauth2Url URL for getting oauth2 bearer token
    * @param onbaseDocsUrl URL for onbase docs API
    * @param pdfDestination path where PDFs are stored locally
    */
  public EvalsOnbase(String clientId,
                     String clientSecret,
                     String oauth2Url,
                     String onbaseDocsUrl,
                     String pdfDestination,
                     String classifiedDocType,
                     String rankedDocType) {
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.oauth2Url = oauth2Url;
    this.onbaseDocsUrl = onbaseDocsUrl;
    this.pdfDestination = pdfDestination;
    this.classifiedDocType = classifiedDocType;
    this.rankedDocType = rankedDocType;
  };

  /**
    * Opens an https connection with the URL parameter passed in
    *
    * @param urlString URL to open connection with
    * @return
    * @throws IOException
    * @throws MalformedURLException
    */
  private HttpsURLConnection openConnection(String urlString) throws IOException,
                                                                     MalformedURLException {
    URL url = new URL(urlString);
    HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
    return conn;
  }

  /**
    * Writes post body data over an https connection
    *
    * @param conn Opened https connection
    * @param body post body to send with request
    * @throws IOException
    */
  private void writeBody(HttpsURLConnection conn, String body) throws IOException {
    OutputStream op = conn.getOutputStream();
    op.write(body.getBytes(CHARSET));
    op.close();
  }

  /**
    * Reads response on an https connection after request has been sent
    *
    * @param conn Opened https connection
    * @return
    * @throws IOException
    * @throws ParseException
    */
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

  /**
    * Makes request to get an oauth2 bearer token
    *
    * @throws IOException
    * @throws MalformedURLException
    * @throws ParseException
    */
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

  /**
    * Checks if bearer token is invalid and retrieves a new one
    *
    * @throws IOException
    * @throws MalformedURLException
    * @throws ParseException
    */
  private void checkBearerToken() throws IOException, MalformedURLException, ParseException {
    if (tokenExpirationDate == null
        || bearerToken == null
        || tokenExpirationDate.compareTo(new Date()) < 0) {
      setBearerToken();
    }
  }

  /**
    * Sets bearer token on https connections authorization header
    *
    */
  private void setAuthHeader(HttpsURLConnection conn) {
    conn.setRequestProperty("Authorization", "Bearer " + bearerToken);
  }

  /**
    * Writes text attributes for multipart post requests
    *
    * @param writer PrintWriter for https connection
    * @param body String to write over connection
    * @throws IOException
    */
  private void writeAttribute(PrintWriter writer, String body) throws IOException {
    writer.append("--" + boundary).append(LINE_FEED);
    writer.append("Content-Disposition: form-data; name=\"attributes\"").append(LINE_FEED);
    writer.append("Content-Type: text/plain; charset=" + CHARSET).append(LINE_FEED);
    writer.append(LINE_FEED);
    writer.append(body).append(LINE_FEED);
    writer.flush();
  }

  /**
    * Writes files for multipart post requests
    *
    * @param writer PrintWriter for https connection
    * @param outputStream OutputStream to write file over
    * @param body String to write over connection
    * @throws IOException
    */
  private void writeFile(PrintWriter writer,
                         OutputStream outputStream,
                         String fileName
  ) throws IOException {
    File uploadFile = new File(pdfDestination + fileName);
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
  }

  /**
    * Create JSONObject for onbase keywords
    *
    * @param name Keyword name
    * @param value Keyword value
    * @return
    */
  private JSONObject createKeyword(String name, String value) {
      JSONObject keyword = new JSONObject();
      keyword.put("name", name);
      keyword.put("value", value);

      return keyword;
  }

  /**
    * Perform multipart form post for evals PDFs
    *
    * @param appointmentType Employees job appointment type
    * @return
    */
  private String getDocType(String appointmentType) {
      String docType;

      if (appointmentType.startsWith("Classified")) {
          docType = classifiedDocType;
      } else {
          docType = rankedDocType;
      }

      return docType;
  }

  /**
    * Perform multipart form post for evals PDFs
    *
    * @param pdfName name of PDF file to post
    * @throws IOException
    * @throws MalformedURLException
    * @throws ParseException
    */
  public void postPDF(String pdfName, Job job) throws IOException, MalformedURLException, ParseException {
    Employee employee = job.getEmployee();
    boundary = "---" + System.currentTimeMillis() + "---";

    checkBearerToken();

    // setup connection
    HttpsURLConnection conn = openConnection(onbaseDocsUrl);
    conn.setRequestMethod("POST");
    conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
    setAuthHeader(conn);
    conn.setDoOutput(true);

    // create writer to send multi form data
    OutputStream outputStream = conn.getOutputStream();
    PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, CHARSET), true);

    // create attributes text form
    JSONObject attributes = new JSONObject();
    attributes.put("DocumentType", getDocType(job.getAppointmentType()));
    attributes.put("FileType", "PDF");
    attributes.put("Comment", pdfName);
    attributes.put("IndexKey", employee.getOsuid());

    // add keywords attribute
    JSONArray keywords = new JSONArray();
    keywords.add(createKeyword("BIO - OSU ID", String.valueOf(employee.getOsuid())));
    keywords.add(createKeyword("BIO - Name Last", employee.getLastName()));
    keywords.add(createKeyword("BIO - Name First", employee.getFirstName()));
    keywords.add(createKeyword("Business Center Code", job.getBusinessCenterName()));
    keywords.add(createKeyword("HR - ECLS - Empl", job.getJobEcls()));
    attributes.put("keywords", keywords);

    // write attributes portion
    writeAttribute(writer, attributes.toString());

    // write pdf portion
    writeFile(writer, outputStream, pdfName);

    // finish and close writer
    writer.append(LINE_FEED).flush();
    writer.append("--" + boundary + "--").append(LINE_FEED);
    writer.close();

    JSONObject response = readResponse(conn);
    System.out.println(response.toString());

    conn.disconnect();
  }
}
