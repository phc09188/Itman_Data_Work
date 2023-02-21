import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * 데이터를 날짜, 요일, 시간에 따라 분류하기 위해 작성한 class
 * 2023.02 AI 학습 전 들어갈 데이터를 필요한 날짜, 요일, 시간에 따라 분류하고
 * 없는 데이터는 0 값으로 row 추가
 * 1시간 단위 2년치 24 * 365 * 2 = 17520 row로 
 */
public class SortService {
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
    // 달, 날짜, 시간 별로 비어있는 데이터에 0 데이터를 추가하기 위해 사용한 3중 map month<day<hour,'1'>> 구조
    static Map<String, Map<String, Map<String, Character>>> dayTimes;
    // 자료에 필요한 년 월 리스트
    static String[] months = {"2020-09", "2020-10", "2020-11", "2020-12", "2021-01", "2021-02", "2021-03", "2021-04", "2021-05", "2021-06", "2021-07", "2021-08", "2021-09", "2021-10", "2021-11", "2021-12", "2022-01", "2022-02", "2022-03", "2022-04", "2022-05", "2022-06", "2022-07", "2022-08"};
    // 월 별 요일 수
    static int[] days = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    private static final String resultFolderAddress = "C:\\Users\\itman\\Desktop\\일반용데이터소팅";
    private static final String sortingAddress = "C:\\Users\\itman\\Desktop\\소팅 결과";

    // path(디렉토리) 의 모든 파일의 리스트를 불러옵니다.
    public static File[] roadFileList(String path) {
        File dir = new File(path);
        return dir.listFiles();
    }

    public static void main(String[] args) throws IOException {
        File[] files = roadFileList(resultFolderAddress);
        outStreamsMap = new HashMap<>();
        // sorting이 필요한 file들
        for (File file : files) {
            dayTimes = new HashMap<>();
            BufferedReader reader = new BufferedReader(new FileReader(resultFolderAddress + "\\" + file.getName(), Charset.forName("CP949")));
            // 한 줄 씩 write하기 위한 동적 메모리
            List<String> list = new ArrayList<>();
            String str;
            //처음에 필요한 년 월에 대한 map value를 만들어 놓는다.
            for (String value : months) {
                Map<String, Map<String, Character>> a = new HashMap<>();
                dayTimes.put(value, a);
            }
            //한 줄 씩 읽어 나간다.
            while ((str = reader.readLine()) != null) {
                String[] contents = str.split(",");
                //년 월
                String month = contents[0].substring(0, 7);
                // 일
                String day = contents[0].substring(8, 10);
                //시간
                String hour = contents[0].substring(11);
                Map<String, Map<String, Character>> dayMap;
                dayMap = dayTimes.get(month);
                Map<String, Character> a;
                if (dayMap.containsKey(day)) {
                    a = dayMap.get(day);
                } else {
                    a = new HashMap<>();
                }
                a.put(hour, '1');
                dayMap.put(day, a);
                dayTimes.put(month, dayMap);
                list.add(str + "\r\n");
            }
            // 비어있는 내용을 찾아서 추가
            for (Map.Entry<String, Map<String, Map<String, Character>>> entry : dayTimes.entrySet()) {
                Map<String, Map<String, Character>> result = entry.getValue();
                String localDate = entry.getKey();
                String month = localDate.substring(5);
                int intMonth = Integer.parseInt(month);
                int lastDay = days[intMonth - 1];
                for (int i = 1; i <= lastDay; i++) {
                    String day = String.valueOf(i);
                    if (i < 10) {
                        day = "0" + day;
                    }
                    if (result.containsKey(day)) {
                        for (int j = 0; j < 24; j++) {
                            Map<String, Character> a = result.get(day);
                            String hour = String.valueOf(j);
                            if (j < 10) {
                                hour = "0" + hour;
                            }
                            if (!a.containsKey(hour)) {
                                list.add(localDate + "-" + day + " " + hour + ",0.001\r\n");
                            }
                        }
                    } else {
                        for (int j = 0; j < 24; j++) {
                            String hour = String.valueOf(j);
                            if (j < 10) {
                                hour = "0" + hour;
                            }
                            list.add(localDate + "-" + day + " " + hour + ",0.001\r\n");
                        }
                    }
                }
            }
            // 년 월 일 시 단위로 오름차순 정렬
            Collections.sort(list);
            // list에서 한 줄씩 가져와서 write
            File sortResult = new File(sortingAddress + "\\" + file.getName());
            FileOutputStream writer = new FileOutputStream(sortResult);
            OutputStreamWriter out = new OutputStreamWriter(writer, "CP949");
            for (String s : list) {
                out.write(s);
            }
            outStreamsMap.put(file.getName(), new OutStreams(out, writer));
        }
        //전체 close
        for (Map.Entry<String, OutStreams> entry : outStreamsMap.entrySet()) {
            OutStreams a = entry.getValue();
            a.out.close();
            a.writer.close();
        }
    }
}
