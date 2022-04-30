package com.example.fine_dust_alarm;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.Buffer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class DustAPI {

    static enum dust_type {
        FINE_DUST,
        VFINE_DUST
    }

    static String dustinfo = "working";

    public DustAPI() {}

    public String get_dust_info(String location_name){
        new Thread() {
            public void run() {
                InputStream inputStream = null;
                InputStreamReader inputStreamReader = null;
                BufferedReader bufferedReader = null;
                HttpURLConnection conn = null;

                try {
                    StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/B552584/ArpltnInforInqireSvc/getCtprvnRltmMesureDnsty"); /*URL*/
                    urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=kj9rVtwid%2FEpOFekRhn%2Bzyu2KBcIReuxj%2BpLdZ1NuMyi1Im5XORdmUe9lkxXAAHBNqEhOg18ATJOj4Lqs5MM%2FA%3D%3D"); /*Service Key*/
                    urlBuilder.append("&" + URLEncoder.encode("returnType", "UTF-8") + "=" + URLEncoder.encode("xml", "UTF-8")); /*xml 또는 json*/
                    urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("100", "UTF-8")); /*한 페이지 결과 수*/
                    urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지번호*/
                    urlBuilder.append("&" + URLEncoder.encode("sidoName", "UTF-8") + "=" + URLEncoder.encode("서울", "UTF-8")); /*시도 이름(전국, 서울, 부산, 대구, 인천, 광주, 대전, 울산, 경기, 강원, 충북, 충남, 전북, 전남, 경북, 경남, 제주, 세종)*/
                    urlBuilder.append("&" + URLEncoder.encode("ver", "UTF-8") + "=" + URLEncoder.encode("1.0", "UTF-8")); /*버전별 상세 결과 참고*/
                    URL url = new URL(urlBuilder.toString());
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Content-type", "application/json");
                    //System.out.println("Response code: " + conn.getResponseCode());

                    inputStream = (InputStream) conn.getContent();
                    inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                    bufferedReader = new BufferedReader(inputStreamReader);

                    StringBuffer buff = new StringBuffer();

                    String oneLine = null;
                    while ((oneLine = bufferedReader.readLine()) != null) {
                        if (oneLine == null || oneLine.length() == 0) {
                            continue;
                        }
                        buff.append(oneLine);
                    }
                    bufferedReader.close();
                    conn.disconnect();
                    Log.d("[DEBUG] DustAPI: ", buff.toString());
                    dustinfo = xml_parsing(buff.toString(), get_location_name(location_name));
                } catch (Exception e) {
                    Log.e("[ERROR] DustAPI1: ", e.toString());
                }
            }
        }.start();

        return dustinfo;
    }


    public String xml_parsing(String data, String target_location) {
        String res_str = "";

        try {
            String xml = data;
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            InputStream instream = new ByteArrayInputStream(xml.getBytes());
            Document doc = builder.parse(instream);
            Element root = doc.getDocumentElement();
            NodeList items = root.getElementsByTagName("item");

            for(int i=0;i<items.getLength();i++) {
                Node item = items.item(i);
                NodeList nodeList = item.getChildNodes();

                String tmp_location = "none";
                String tmp_finedust = "none";
                String tmp_vfinedust = "none";

                for(int j=0;j<nodeList.getLength();j++) {
                    Node cur_node = nodeList.item(j);
                    if (cur_node.getNodeName().equals("stationName")) {
                        tmp_location = cur_node.getTextContent();
                    }
                    if (cur_node.getNodeName().equals("pm10Value")) {
                        tmp_finedust = cur_node.getTextContent();
                    }
                    if (cur_node.getNodeName().equals("pm25Value")) {
                        tmp_vfinedust = cur_node.getTextContent();
                    }
                }

                if(tmp_location.equals(target_location)) {
                    res_str = res_str + "지역: "+tmp_location + "\n";
                    res_str = res_str + "미세먼지: "+tmp_finedust + "\n";
                    res_str = res_str + "초미세먼지: "+tmp_vfinedust + "\n";

                    res_str = res_str + "미세먼지 등급: "+dust_level(tmp_finedust, dust_type.FINE_DUST) + "\n";
                    res_str = res_str + "초미세먼지 등급: "+dust_level(tmp_vfinedust, dust_type.VFINE_DUST) + "\n";
                    Log.d("[DEBUG] res_str: ", res_str);
                    break;
                }
            }
        }
        catch(Exception e) {
            Log.e("[ERROR] DustAPI2: ", e.toString());
        }
        return res_str;
    }

    public String dust_level(String input, dust_type dust_type) {
        int[] section_array;
        if (dust_type == dust_type.FINE_DUST) {
            section_array = new int[] {30,80,15};
        }
        else if(dust_type == dust_type.VFINE_DUST){
            section_array = new int[] {15,35,75};
        }
        else {
            System.out.println("ERROR");
            return "error";
        }

        if (input.equals('-')) {
            return input;
        }
        int value = Integer.parseInt(input);

        if (value <= section_array[0]) {
            return "좋음";
        }
        else if (value <= section_array[1]) {
            return "보통";
        }
        else if (value <= section_array[2]) {
            return "나쁨";
        }
        else {
            return "매우나쁨";
        }
    }

    public String get_location_name(String address){
        String[] split_str = address.split(",");
        String target_location = split_str[1].trim();
        switch(target_location){
            case "Jung-gu":
                return "중구";
            case "Jongno-gu":
                return "종로구";
            case "Yongsan-gu":
                return "용산구";
            case "Seongdong-gu":
                return "성동구";
            default:
                return "중구";
        }
    }
}