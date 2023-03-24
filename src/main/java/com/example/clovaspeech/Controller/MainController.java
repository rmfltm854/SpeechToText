package com.example.clovaspeech.Controller;

import com.amazonaws.services.s3.model.Bucket;
import com.example.clovaspeech.Service.MainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
public class MainController {
    @Autowired
    MainService service;
    @RequestMapping("/stt")
    public String sttService(@RequestParam("uploadFile")MultipartFile file,@RequestParam("language")String language){

        String result = "";
        try{

            // 1. 파일 저장 경로 설정 : 실제 서비스되는 위치 (프로젝트 외부에 저장)
            String uploadPath = "/Users/jominsu/Downloads/";

            // 2. 원본 파일 이름 알아오기
            String originalFileName = file.getOriginalFilename();
            System.out.println(originalFileName);
            // 3. filePathname
            String filePathName = uploadPath + originalFileName;
            System.out.println(originalFileName);
            System.out.println(filePathName);
            service.UploadFile(originalFileName,filePathName);
            String URL = service.getURL(originalFileName);//objectStorage object Link
            // 4.speechToText
            result = service.SpeechToText(URL);

        }catch(Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @RequestMapping("/List")//ObjectStorage에 저장되어있는 bucket리스트를 불러온다.
    public void GetList(){
        List<Bucket> bucketList = service.getBucketList();
        for (Bucket bucket : bucketList) {
            System.out.println("name=" + bucket.getName() + ", creation_date=" + bucket.getCreationDate() + ", owner=" + bucket.getOwner().getId());
        }
    }

    @RequestMapping("/UploadFile")//objectStorage에 파일을 올린다.
    public void UploadFile(String objectName, String filePath){
        System.out.println(objectName);
        System.out.println(filePath);
        service.UploadFile(objectName,filePath);
    }

    @RequestMapping("/GetFile")//파일을 가져온다.
    public void GetFile(){
        service.downloadFile();
    }

    @RequestMapping("/bucketTest")
    public void bucketTest() {
        service.Speech("/");
    }

}

