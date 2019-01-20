import java.util.*;
import java.util.concurrent.*;
import java.text.*;

public class Alarm2 {
  public static Map<Long,Integer> schedulerAlarm =new TreeMap<>();
  public static Map<Long,String > mapMessageAlarm = new TreeMap <>();
  public static Map<Integer, Long> mapIndexAlarm = new TreeMap<>();
  public static Map<Integer, String> statusAlarm = new TreeMap<>();
  public static Map<Integer, String> timeAlarm = new TreeMap<>();
  public static int indexAlarm=0;
  public  static long delayAlarm = 1000000;

  private static  void AddAlarm (String letter){
    String[] strs = letter.split("-");
    String dt = strs[2];
    Date dateNow = new Date();
    SimpleDateFormat ft = new SimpleDateFormat (" dd.MM.yyyy hh:mm");
    Date parsingDate;
    try {
      parsingDate = ft.parse(dt);
      if (mapMessageAlarm.containsKey(parsingDate.getTime())) {
        System.out.println(strs[1] + strs[2] + ": denied. in past");
        return;
      }
      if (parsingDate.getTime() > dateNow.getTime()) {
        schedulerAlarm.put(parsingDate.getTime(),indexAlarm);
        mapMessageAlarm.put(parsingDate.getTime(), strs[1]);
        mapIndexAlarm.put(indexAlarm, parsingDate.getTime());
        timeAlarm.put(indexAlarm, strs[2]);
        statusAlarm.put(indexAlarm, "SCHEDULED");
        System.out.println("added");
        indexAlarm++;
      } else System.out.println("error "+strs[2]);
    }catch (ParseException e) {
      System.out.println("Eror for Date " + ft);
    }
  }
  private static void CancelAlarm (String instr) {
    String[] r = instr.split(" ");
    int index = Integer.parseInt(r[1]);
    if (statusAlarm.containsKey(index)) {
      statusAlarm.put(index, "CANCELED");
      //scheduler.remove(dat.get(k));
      System.out.println("canceled");
    }
  }
  private static void ListAlarm() {
    for (int i : mapIndexAlarm.keySet()) {
      System.out.println("["+i+"]"+timeAlarm.get(i)+" ["+statusAlarm.get(i)+"]");
    }
  }
  private static void ExecAlarm() {
    Date dateNow = new Date();
    long tmp = 0;
    for (long t : schedulerAlarm.keySet()) {
      tmp = t;
      int index = schedulerAlarm.get(t);
      if (t <= dateNow.getTime() && statusAlarm.get(index).equals("SCHEDULED")) {
        System.out.println(mapMessageAlarm.get(mapIndexAlarm.get(index)) + "[" + index + "]" + timeAlarm.get(index));
        statusAlarm.put(index, "EXECUTED");
        //scheduler.remove(t);
      } else if (t > dateNow.getTime()) break;
    }
    if (tmp > dateNow.getTime()) delayAlarm = tmp - dateNow.getTime();
    else delayAlarm=1000;
  }

  public static void main(String[] args) {
    Object lock = new Object();
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    Runnable task = () -> {
      try {
        synchronized(lock) {
          ExecAlarm();
        }
        TimeUnit.MILLISECONDS.sleep(delayAlarm);
      } catch (InterruptedException e) {
        System.err.println("task interrupted");
      }
    };

    while (true) {
      Scanner scan = new Scanner(System.in);
      String str = scan.nextLine();
      if (str.startsWith("exit")) {
        System.out.println("exit");
        executor.shutdown();
        return;
      }
      else if  (str.startsWith("add ")){
        synchronized(lock) {
          AddAlarm(str);
        }
        executor.shutdown();
        executor = Executors.newScheduledThreadPool(1);
        executor.scheduleWithFixedDelay(task, 1, 1, TimeUnit.MILLISECONDS);

      }
      else if  (str.startsWith("cancel ")){
        synchronized(lock) {
          CancelAlarm(str);
        }
        executor.shutdown();
        executor = Executors.newScheduledThreadPool(1);
        executor.scheduleWithFixedDelay(task, 1, 1, TimeUnit.MILLISECONDS);
      }
      else if  (str.startsWith("list")){
        synchronized(lock) {
          ListAlarm();
        }
      }
      else {
        System.out.println("Eror");
      }
    }
  }
}
