import java.util.*;
import java.util.concurrent.*;
import java.text.*;

public class Alarm3 {
  public static Object lock = new Object();
  public static Map<Long,Integer> schedulerAlarm =new TreeMap<>();
  public static Map<Long,String > mapMessageAlarm = new TreeMap <>();
  public static Map<Integer, Long> mapIndexAlarm = new TreeMap<>();
  public static Map<Integer, String> statusAlarm = new TreeMap<>();
  public static Map<Integer, String> timeAlarm = new TreeMap<>();

  public static void main(String[] args) {
    int indexAlarm=0;
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    Runnable task = () -> {
      try {
        long delayAlarm = ExecAlarm();
        TimeUnit.MILLISECONDS.sleep(delayAlarm);
      } catch (InterruptedException e) {
        //System.err.println("task interrupted");
      }
    };
    ScheduledFuture scheduledFuture = executor.scheduleWithFixedDelay(task, 1,1, TimeUnit.MILLISECONDS);

    while (true) {
      Scanner scan = new Scanner(System.in);
      String str = scan.nextLine();
      long currentTime = System.currentTimeMillis();
      SimpleDateFormat ft = new SimpleDateFormat (" dd.MM.yyyy hh:mm");

      if (str.startsWith("exit")) {
        System.out.println("exit");
        scheduledFuture.cancel(true);
        executor.shutdown();
        return;
      }
      else if  (str.startsWith("add ")){
        String[] strs = str.split("-");
        String dt = strs[2];
        try {
          long dTime = ft.parse(dt).getTime();
          if (mapMessageAlarm.containsKey(dTime)) {
            System.out.println(strs[1] + strs[2] + ": denied. in past");
          }else if (dTime > currentTime) {
            if (AddAlarm(dTime,strs,indexAlarm)){
              indexAlarm++;
              System.out.println("added");
              scheduledFuture.cancel(true);
              scheduledFuture = executor.scheduleWithFixedDelay(task, 1, 1, TimeUnit.MILLISECONDS);
            }
          } else{
            System.out.println("Error :"+strs[2]);
          }
        }catch (ParseException e) {
          System.out.println("Eror for Date " + ft);
        }
      }
      else if  (str.startsWith("cancel ")){
        String[] strs = str.split(" ");
        int index = Integer.parseInt(strs[1]);
        if (statusAlarm.containsKey(index)) {
          if (CancelAlarm(index)) {
            System.out.println("canceled");
            scheduledFuture.cancel(true);
            scheduledFuture = executor.scheduleWithFixedDelay(task, 1, 1, TimeUnit.MILLISECONDS);
          }
        } else{
          System.out.println("Error Cancel");
        }
      }
      else if  (str.startsWith("list")){
        ListAlarm();
      }
      else {
        System.out.println("Eror String");
      }
    }
  }

  private static  boolean  AddAlarm (long dTime,String [] strs,int indexAlarm){
    mapMessageAlarm.put(dTime, strs[1]);
    mapIndexAlarm.put(indexAlarm, dTime);
    timeAlarm.put(indexAlarm, strs[2]);
    synchronized(lock) {
      schedulerAlarm.put(dTime,indexAlarm);
      statusAlarm.put(indexAlarm, "SCHEDULED");
    }
    return true;
  }
  private static boolean CancelAlarm (int index) {
    synchronized(lock) {
      statusAlarm.put(index, "CANCELED");
    }
    return true;
  }
  private static void ListAlarm() {
    for (int i : mapIndexAlarm.keySet()) {
      System.out.println("["+i+"]"+timeAlarm.get(i)+" ["+statusAlarm.get(i)+"]");
    }
  }
  private static long ExecAlarm() {
    long currentTime = System.currentTimeMillis();
    long delayAlarm = 3600 * 1000;
    for (long dTime : schedulerAlarm.keySet()) {
      int index = schedulerAlarm.get(dTime);
      if (dTime <= currentTime && statusAlarm.get(index).equals("SCHEDULED")) {
        System.out.println(mapMessageAlarm.get(mapIndexAlarm.get(index)) + "[" + index + "]" + timeAlarm.get(index));
        synchronized (lock) {
          statusAlarm.put(index, "EXECUTED");
        }
      } else if (dTime > currentTime) {
        delayAlarm = dTime - currentTime;
        break;
      }
    }
    return delayAlarm;
  }
}

