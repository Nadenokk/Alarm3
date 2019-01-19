import java.util.*;
import java.util.concurrent.*;
import java.text.*;

public class Alarm {
  public static Map<Long,Integer> scheduler =new TreeMap<>();
  public static Map<Long,String > map = new TreeMap <>();
  public static Map<Integer, Long> dat = new TreeMap<>();
  public static Map<Integer, String> status = new TreeMap<>();
  public static Map<Integer, String> time_alarm = new TreeMap<>();
  public static int k=0;
  public  static long p = 1000000;

  private static  void AddAlarm (String letter){
    String[] r = letter.split("-");
    String dt = r[2];
    Date dateNow = new Date();
    SimpleDateFormat ft = new SimpleDateFormat (" dd.MM.yyyy hh:mm");
    Date parsingDate;
    try {
      parsingDate = ft.parse(dt);
      if (map.containsKey(parsingDate.getTime())) {
        System.out.println(r[1] + r[2] + ": denied. in past");
        return;
      }
      if (parsingDate.getTime() > dateNow.getTime()) {
        scheduler.put(parsingDate.getTime(),k);
        map.put(parsingDate.getTime(), r[1]);
        dat.put(k, parsingDate.getTime());
        time_alarm.put(k, r[2]);
        status.put(k, "SCHEDULED");
        System.out.println("added");
        k++;
      } else System.out.println("error "+r[2]);
    }catch (ParseException e) {
      System.out.println("Eror for Date " + ft);
    }
  }
  private static void CancelAlarm (String instr) {
    String[] r = instr.split(" ");
    int k = Integer.parseInt(r[1]);
    if (status.containsKey(k)) {
      status.put(k, "CANCELED");
      //scheduler.remove(dat.get(k));
      System.out.println("canceled");
    }
  }
  private static void ListAlarm() {
    for (int i : dat.keySet()) {
      System.out.println("["+i+"]"+time_alarm.get(i)+" ["+status.get(i)+"]");
    }
  }
  private static void ExecAlarm() {
    Date dateNow = new Date();
    long j = 0;
    for (long t : scheduler.keySet()) {
      j = t;
      int index = scheduler.get(t);
      if (t <= dateNow.getTime() && status.get(index).equals("SCHEDULED")) {
        System.out.println(map.get(dat.get(index)) + "[" + index + "]" + time_alarm.get(index));
        status.put(index, "EXECUTED");
        //scheduler.remove(t);
      } else if (t > dateNow.getTime()) break;
    }
    if (j > dateNow.getTime()) p = j - dateNow.getTime();
    else p=1000;
  }

  public static void main(String[] args) {
    Object lock = new Object();
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    Runnable task = () -> {
      try {
        synchronized(lock) {
          ExecAlarm();
        }
        TimeUnit.MILLISECONDS.sleep(p);
      } catch (InterruptedException e) {
        System.err.println("task interrupted");
      }
    };

    while (true) {
      Scanner scan = new Scanner(System.in);
      String str = scan.nextLine();
      if (str.equals("exit")) {
        System.out.println("exit");
        executor.shutdown();
        return;
      }
      else if  (!(str.indexOf("add ") == -1)){
        synchronized(lock) {
          AddAlarm(str);
        }
        executor.shutdown();
        executor = Executors.newScheduledThreadPool(1);
        executor.scheduleWithFixedDelay(task, 1, 1, TimeUnit.MILLISECONDS);

      }
      else if  (!(str.indexOf("cancel ") == -1)){
        synchronized(lock) {
          CancelAlarm(str);
        }
        executor.shutdown();
        executor = Executors.newScheduledThreadPool(1);
        executor.scheduleWithFixedDelay(task, 1, 1, TimeUnit.MILLISECONDS);
      }
      else if  (!(str.indexOf("list") == -1)){
        synchronized(lock) {
          ListAlarm();
        }
      }
      else System.out.println("Eror");
    }
  }
}
