package org.example;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 처음 시도해 보는 file 작업!!
 * 2023.02 전력데이터 (약)700만개를 분류하기 위해 만들었던 프로그램
 * 분류까지 걸린 시간
 * 처음 = 14시간 -> 최적화 후 24분
 * 새로 알게된 사실
 * 1. 라이브러리에 의존하지 말고 근본을 파악하자
 * 2. 대부분의 파일 확장자에는 hex code가 존재한다. 그렇지만 .txt 파일과 .csv 파일은 존재하지 않는다.
 * 3. Out 인터페이스들은 마지막에 한 번에 out 메소드를 사용하는 것이 좋다.
 * 4. OS의 FileSystem 에서 file open 이후 장시간 close 가 안 될 경우 한 번씩 flush 를 수행한다.
 */
public class Main {
    // output 을 위한 인터페이스 class
    static class OutStreams {

        OutputStreamWriter out;
        FileOutputStream writer;

        public OutStreams(OutputStreamWriter out, FileOutputStream writer) {
            this.out = out;
            this.writer = writer;
        }
    }

    //최적화를 위한 map 사용
    static Map<String, OutStreams> map;
    private final static String csvFileAddress = "000";
    private final static String roadFileAddress = "000";

    // path(디렉토리) 의 모든 파일 불러오기
    public static File[] roadFileList(String path) {
        File dir = new File(path);
        return dir.listFiles();
    }

    //처음 발견된 key의 경우 파일을 생성하고 데이터를 추가합니다.
    private static void createAndAppend(String type, String name, List<String> appendContent) throws IOException {
        File csvFile = roadFile(roadFileAddress + type, name);
        FileOutputStream writer = new FileOutputStream(csvFile);
        OutputStreamWriter out = new OutputStreamWriter(writer, "CP949");
        out.write(appendContent.get(0) + "," + appendContent.get(1));
        map.put(name + type, new OutStreams(out, writer));
    }

    //기존 csv 파일을 불러와서 마지막 줄에 새로운 데이터 append
    private static void append(String type, String name, List<String> appendContent) throws IOException {
        OutStreams streams = map.get(name + type);
        streams.out.append("\r\n" + appendContent.get(0) + "," + appendContent.get(1));
        map.put(name + type, streams);
    }

    //csv 불러오기
    private static File roadFile(String path, String name) {
        return new File(path + "\\" + name + ".csv");
    }

    // 엑셀에서 요구하는 형식에 맞춰서 DateTime 을 만듭니다. format ex) 2017-01-01 11:30
    private static String calDateTime(String date, String timeNumber) {
        String year = date.substring(0, 4);
        String month = date.substring(4, 6);
        String day = date.substring(6);
        String hour = timeNumber.substring(0, 2);
        String minute = timeNumber.substring(2);
        return year + "-" + month + "-" + day + " " + hour + ":" + minute;
    }

    //메인 메소드
    public static void main(String[] args) throws IOException {
        //폴더의 모든 csv 파일 불러오기
        File[] list = roadFileList(csvFileAddress);
        //키 유무를 위한 map 선언
        map = new HashMap<>();
        for (File fileList : list) {
            // 파일 읽어오기
            BufferedReader reader = new BufferedReader(new FileReader(fileList, Charset.forName("CP949")));
            //0번째 줄 생략하기 위해 이렇게 작
            String str = reader.readLine();
            //csv 1번쨰 줄부터 읽으면서 실행
            while ((str = reader.readLine()) != null) {
                String[] cur = str.split(",");
                try {
                    //dateTime = 시간형식, name = 파일 이름 type = 전력분류(ex 일반용전기 ) elect = 전력사용량
                    String dateTime = calDateTime(cur[0], cur[1]);
                    String name = cur[4].substring(1, cur[4].length() - 1);
                    String type = cur[5].substring(1, cur[5].length() - 1);
                    String elect = cur[7];
                    String namePlusType = name + type;
                    //appendContent = append 할 내용을 담아논 동적 메모리(시간, 전력사용량)
                    List<String> appendContent = new ArrayList<>();
                    appendContent.add(dateTime);
                    appendContent.add(elect);
                    if (map.containsKey(namePlusType)) {
                        //파일에 라인 추가
                        append(type, name, appendContent);
                    } else {
                        //파일이 존재하는지 확인 후 없으면 생성 후 라인 추가, 있으면 그냥 라인 추가
                        createAndAppend(type, name, appendContent);
                    }
                } catch (Exception e) {
                    continue;
                }
            }
            reader.close();
        }
        //전체적인 class OutStreams close
        for (Map.Entry<String, OutStreams> pair : map.entrySet()) {
            OutStreams out = pair.getValue();
            out.out.close();
            out.writer.close();
        }
        // 완료 메시지
        System.out.println("success");
    }
}