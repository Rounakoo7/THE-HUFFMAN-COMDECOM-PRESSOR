package com.rounak.backend.service;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.util.PriorityQueue;
import java.util.HashMap;
import java.io.*;

class HuffmanNode {
    char data;
    int frequency;
    HuffmanNode left, right;

    HuffmanNode(char data, int frequency) {
        this.data = data;
        this.frequency = frequency;
        left = right = null;
    }
}

@Service
public class HuffmanCodingService {
    public void generateCodeMap(HuffmanNode root, StringBuilder code, HashMap<Character, String> codeMap) {
        if (root == null)
            return;
        if (root.data != '$') {
            if (code.isEmpty()) {
                code.append("0");
            }
            codeMap.put(root.data, code.toString());
        }
        if (root.left != null) {
            generateCodeMap(root.left, code.append('0'), codeMap);
            code.deleteCharAt(code.length() - 1);
        }
        if (root.right != null) {
            generateCodeMap(root.right, code.append('1'), codeMap);
            code.deleteCharAt(code.length() - 1);
        }
    }
    public ResponseEntity<Resource> encodeFile(MultipartFile file) throws Exception{
        if(!file.getContentType().equals("text/plain")){
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
        StringBuilder data = new StringBuilder();
        InputStream inputStream = file.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line;
        if((line = bufferedReader.readLine()) != null){
            data.append(line);
        }
        while ((line = bufferedReader.readLine()) != null){
            data.append('\n');
            data.append(line);
        }
        inputStream.close();
        inputStreamReader.close();
        bufferedReader.close();
        HashMap<Character, Integer> frequencyMap = new HashMap<>();
        PriorityQueue<HuffmanNode> priorityQueue = new PriorityQueue<>((a, b) -> a.frequency - b.frequency);
        HashMap<Character, String> codeMap = new HashMap<>();
        for (char c : data.toString().toCharArray()) {
            frequencyMap.put(c, frequencyMap.getOrDefault(c, 0) + 1);
        }
        for (char c : frequencyMap.keySet()) {
            priorityQueue.add(new HuffmanNode(c, frequencyMap.get(c)));
        }
        while (priorityQueue.size() > 1) {
            HuffmanNode left = priorityQueue.poll();
            HuffmanNode right = priorityQueue.poll();
            HuffmanNode newNode = new HuffmanNode('$', left.frequency + right.frequency);
            newNode.left = left;
            newNode.right = right;
            priorityQueue.add(newNode);
        }
        HuffmanNode root = priorityQueue.poll();
        generateCodeMap(root, new StringBuilder(), codeMap);
        StringBuilder encodedData = new StringBuilder();
        for (char c : data.toString().toCharArray()) {
            encodedData.append(codeMap.get(c));
        }
        int extraBits = encodedData.length() % 8;
        if(extraBits != 0){
            extraBits = 8 - extraBits;
        }
        int temp = extraBits;
        while(temp != 0){
            encodedData.append('0');
            temp--;
        }
        byte[] encodedDataBytes = new byte[encodedData.length() / 8];
        int idx = 0;
        for (int i = 0; i < encodedData.length(); i += 8) {
            String encodedByte;
            encodedByte = encodedData.substring(i, i + 8);
            encodedDataBytes[idx] = (byte) Integer.parseInt(encodedByte, 2);
            idx++;
        }
        File compressedFile = new File(file.getOriginalFilename().substring(0, file.getOriginalFilename().indexOf('.')) + "_copmpressed.txt");
        OutputStream outStream = new FileOutputStream(compressedFile);
        ObjectOutputStream objectOutStream = new ObjectOutputStream(outStream);
        objectOutStream.writeObject(encodedDataBytes);
        objectOutStream.writeObject(extraBits);
        objectOutStream.writeObject(codeMap);
        objectOutStream.close();
        outStream.close();
        if(file.getSize() <= compressedFile.length()){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        ResponseEntity<Resource> response = ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + compressedFile + "\"")
                .contentLength(compressedFile.length())
                .contentType(MediaType.TEXT_PLAIN)
                .body(new InputStreamResource(Files.newInputStream(compressedFile.toPath())));
        compressedFile.delete();
        return response;
    }
    public ResponseEntity<Resource> decodeFile(MultipartFile file) throws Exception{
        if(!file.getContentType().equals("text/plain")){
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
        InputStream inStream = file.getInputStream();
        ObjectInputStream objectInStream = new ObjectInputStream(inStream);
        byte[] encodedDataBytes = (byte[])objectInStream.readObject();
        int extraBits = (int)objectInStream.readObject();
        HashMap<Character, String> codeMap = (HashMap<Character, String>)objectInStream.readObject();
        objectInStream.close();
        inStream.close();
        HashMap<String, Character> decodeMap = new HashMap<>();
        for(char c : codeMap.keySet()){
            decodeMap.put(codeMap.get(c), c);
        }
        StringBuilder encodedByte = new StringBuilder();
        StringBuilder encodedData = new StringBuilder();
        for (byte encodedDataByte : encodedDataBytes) {
            String tempEncodedByte = Integer.toBinaryString(Byte.toUnsignedInt(encodedDataByte));
            int size = tempEncodedByte.length();
            while (size != 8) {
                encodedByte.append('0');
                size++;
            }
            encodedByte.append(tempEncodedByte);
            encodedData.append(encodedByte);
            encodedByte.delete(0, encodedByte.length());
        }
        encodedData.delete(encodedData.length() - extraBits, encodedData.length());
        StringBuilder decodeddata = new StringBuilder();
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < encodedData.length(); i++) {
            temp.append(encodedData.charAt(i));
            if (decodeMap.containsKey(temp.toString())) {
                decodeddata.append(decodeMap.get(temp.toString()));
                temp.delete(0, temp.length());
            }
        }
        File decompressedFile = new File(file.getOriginalFilename().substring(0, file.getOriginalFilename().indexOf('.')) + "_decopmpressed.txt");
        FileWriter fileWriter = new FileWriter(decompressedFile);
        for (int i = 0; i < decodeddata.length(); i++){
            fileWriter.write(decodeddata.charAt(i));
        }
        fileWriter.close();
        if(file.getSize() >= decompressedFile.length()){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        ResponseEntity<Resource> response = ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + decompressedFile + "\"")
                .contentLength(decompressedFile.length())
                .contentType(MediaType.TEXT_PLAIN)
                .body(new InputStreamResource(Files.newInputStream(decompressedFile.toPath())));
        decompressedFile.delete();
        return response;
    }
}
