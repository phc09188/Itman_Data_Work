import java.io.*;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 누락이 적고 row수가 많은 데이터 필터링 결과를 위해 작성한 클래스
 * 2023.02 dataDictionarySet()메소드를 통해 필터링
 * 에러사항
 * 2020년의 경우 2월이 29일까지 존재해서, localDate로 파싱하는데 문제가 생겼었다.
 */
public class DataMergeService {
    static class OutStreams {

        OutputStreamWriter out;
        FileOutputStream writer;

        public OutStreams(OutputStreamWriter out, FileOutputStream writer) {
            this.out = out;
            this.writer = writer;
        }
    }
    // close를 묶기 위한 map
    static Map<String, OutStreams> outStreamsMap;
    // 필요한 자료의 정보를 저장해놓기 위한 map
    static Map<String, String> hasValue;
 
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String distributionAddress = "111";
    private static final String fileList = "111";
    private static final String resultFolderAddress = "111";

    // path(디렉토리) 의 모든 파일의 리스트를 불러옵니다.
    public static File[] roadFileList(String path) {
        File dir = new File(path);
        return dir.listFiles();
    }

    public static void main(String[] args) throws IOException {
        dataDictionarySet();
        outStreamsMap = new HashMap<>();

        File[] files = roadFileList(fileList + "");
        for (File file : files) {
            String fileName = file.getName().substring(0, file.getName().length() - 4);
            //필요 없는 파일 지나치기
            if (!hasValue.containsKey(fileName)) {
                continue;
            }

            timeFormatChange(file);
        }
        for (Map.Entry<String, OutStreams> a : outStreamsMap.entrySet()) {
            a.getValue().out.close();
            a.getValue().writer.close();
        }
    }

    private static void timeFormatChange(File file) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(file, Charset.forName("CP949")));
        String str = reader.readLine();
        int rowCnt = 1;
        Map<String, Double> map = new TreeMap<>();
        try {
            while ((str = reader.readLine()) != null) {
                String[] contents = str.split(",");
                String[] times = contents[0].split(" ")[1].split(":");
                String date = contents[0];
                //2
                LocalDateTime dateTime = null;
                String hour = "";
                if (date.contains("2020-02-29")) {
                    date = date.replace("2020-02-29", "2021-02-27");
                    contents[0] = contents[0].replace("2020-02-29", "2021-02-27");
                }
                if (contents[0].split(" ")[1].substring(0, 2).equals("24")) {
                    date = date.substring(0, date.length() - 5);
                    date += "23:45";
                    dateTime = LocalDateTime.parse(date, formatter);
                } else {
                    dateTime = LocalDateTime.parse(date, formatter);
                    dateTime = dateTime.minusMinutes(15);
                }

                //1
                if (dateTime.getHour() < 10) {
                    hour = "0" + dateTime.getHour();
                } else {
                    hour = String.valueOf(dateTime.getHour());
                }
                if (map.containsKey(date.substring(0, 10) + " " + hour)) {
                    map.put(date.substring(0, 10) + " " + hour, map.get(date.substring(0, 10) + " " + hour) + Double.parseDouble(contents[1]));
                } else {
                    map.put(date.substring(0, 10) + " " + hour, Double.parseDouble(contents[1]));
                }
                rowCnt++;
            }
        } catch (Exception e) {
            System.out.println(file.getName());
            System.out.println(rowCnt);
            System.exit(0);

        }
        File newFile = new File(resultFolderAddress + "\\" + file.getName());
        FileOutputStream writer = new FileOutputStream(newFile);
        OutputStreamWriter out = new OutputStreamWriter(writer, "CP949");
        out.write("시간,전력사용량" + "\r\n");
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            out.write(entry.getKey() + "," + String.valueOf(Math.round(entry.getValue() * 1000) / 1000.0) + "\r\n");
        }
        outStreamsMap.put(file.getName().substring(0, file.getName().length() - 4), new OutStreams(out, writer));
    }

    private static void dataDictionarySet() throws IOException {
        File distributionFile = new File(distributionAddress);
        BufferedReader reader = new BufferedReader(new FileReader(distributionFile, Charset.forName("CP949")));
        String str = reader.readLine();
        hasValue = new HashMap<>();
        while ((str = reader.readLine()) != null) {
            String[] contents = str.split(",");
            String name = contents[1];
            String months = contents[4];
            if (Integer.parseInt(months.replace("개월", "")) == 24 && contents[0].equals("일반용(갑)저압")
                    && Integer.parseInt(contents[5]) <= 142 && contents[2].equals("15분"))
                hasValue.put(name, contents[2]);
        }
        reader.close();
    }

}
