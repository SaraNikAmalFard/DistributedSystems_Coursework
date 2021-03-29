package com.example.grpc.client.grpcclient;

import com.example.grpc.client.grpcclient.service.FileStorageService;
import com.example.grpc.client.grpcclient.service.GRPCClientService;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.util.Arrays;
import java.util.stream.Collectors;

@RestController
public class MatrixMultiplicationController {

    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private GRPCClientService clientService;

    @PostMapping("/uploadFile")
    public String uploadFile(@RequestParam("file") MultipartFile file)
    {
        fileStorageService.storeFile(file);
        return "File successfully uploaded";
    }

    @PostMapping("/uploadMultipleFiles")
    public String uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        Arrays.asList(files).stream().map(file->uploadFile(file)).collect(Collectors.toList());
        if(clientService.checkFiles() == true)
        {
            //clientService.create2DArrayFromFile();
            clientService.create2DArrayFromFile();
            //System.out.println(clientService.matrixA);
            //System.out.println(clientService.matrixB);
            return "File successfully uploaded";
        }
        else
        {
            clientService.delete();
            return "The matrices or the file you uploaded was not accepted!";
        }
    }

    @GetMapping("/multiplyMatrix")
    public JSONArray multiplyMatrix(@RequestParam(value = "deadline" , defaultValue = "1") int deadline)
    {

        int[][] result = clientService.multiplyMatrixBlock(clientService.matrixA , clientService.matrixB , deadline);
        return  clientService.replyMatrixToJson(result);
    }
}


