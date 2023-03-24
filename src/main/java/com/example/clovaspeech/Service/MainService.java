package com.example.clovaspeech.Service;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.example.clovaspeech.Configuration.CloudConfig;
import com.google.gson.Gson;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class MainService {
    @Autowired
    CloudConfig config;
    public String Speech(String URL) {
        final SpeechClass clovaSpeechClient = new SpeechClass();
        com.example.clovaspeech.Service.SpeechClass.NestRequestEntity requestEntity = new SpeechClass.NestRequestEntity();;
        final String result = clovaSpeechClient.url(URL, requestEntity);
        JSONParser parser = new JSONParser();
        String resultText = null;
        try{
            org.json.simple.JSONObject object = (JSONObject)parser.parse(result);
            System.out.println(object.get("text")); //sim
            resultText = (String) object.get("text");
        }catch (org.json.simple.parser.ParseException e){
            e.printStackTrace();
        }
        return resultText;
    }
    public  String  SpeechToText(String FilePathName){
        String resultText = Speech(FilePathName);
        int resultParam = resultToFileSave(resultText);
        if(resultParam == 1){
            return "text 성공";
        }else {
            return "text 실패";
        }

    }

    // 음성 파일에서 추출한 텍스트 파일로 저장
    public int resultToFileSave(String result) {
        int resultParam = 0;
        try {
            System.out.println("실행중");
            String fileName = Long.valueOf(new Date().getTime()).toString();
            String filePathName = "/Users/jominsu/Desktop/Docs/" + "test" + fileName + ".txt";

            FileWriter fw = new FileWriter(filePathName);
            fw.write(result);
            fw.close();
            resultParam = 1;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultParam;
    }
    //upload 메소드
    public int UploadFile(String objectName, String filePath) {
        AmazonS3 s3 = config.amazonS3Client();
        AccessControlList ACL = s3.getObjectAcl("jominsutest","LocalTest.mp3");//권환부여가 아무설정없이넣으면 미공개로 처리되서 콘솔창에서 공개로설정해놓은 초기 ACL설정을 불러서 새로만들어서넣을때마다 ACL값을 넣어준다.
        int result;
        try{
            s3.putObject("jominsutest",objectName,new File(filePath));
            s3.setObjectAcl("jominsutest",objectName,ACL);//권환부여가 아무설정없이넣으면 미공개로 처리되서 콘솔창에서 공개로설정해놓은 초기 ACL설정을 불러서 새로만들어서넣을때마다 ACL값을 넣어준다.
            System.out.format("Object %s has been created.\n",objectName);
            result = 1;
        }catch (AmazonS3Exception e){
            e.printStackTrace();
            result = 2;
        }catch (SdkClientException e){
            e.printStackTrace();
            result = 3;
        }
        return result;
    }
    //bucketlist출력
    public List<Bucket> getBucketList(){
        AmazonS3 s3 = config.amazonS3Client();
        List<Bucket> buckets = new ArrayList<>();
        try {
            buckets = s3.listBuckets();
            System.out.println("Bucket List: ");
        } catch (AmazonS3Exception e) {
            e.printStackTrace();
        } catch(SdkClientException e) {
            e.printStackTrace();
        }
        return buckets;
    }

    //download 메소드
    public void downloadFile(){
        String bucketName = "sample-bucket";
        String objectName = "sample-object.txt";
        String downloadFilePath = "/tmp/sample-object.txt";
        AmazonS3 s3 = config.amazonS3Client();
        try {
            S3Object s3Object = s3.getObject(bucketName, objectName);
            S3ObjectInputStream s3ObjectInputStream = s3Object.getObjectContent();

            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFilePath));
            byte[] bytesArray = new byte[4096];
            int bytesRead = -1;
            while ((bytesRead = s3ObjectInputStream.read(bytesArray)) != -1) {
                outputStream.write(bytesArray, 0, bytesRead);
            }
            outputStream.close();
            s3ObjectInputStream.close();
            System.out.format("Object %s has been downloaded.\n", objectName);
        } catch (AmazonS3Exception e) {
            e.printStackTrace();
        } catch(SdkClientException e) {
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public String getURL(String FileName){
       String url = "https://kr.object.ncloudstorage.com/jominsutest/"+FileName;
       return url;
    }
}
