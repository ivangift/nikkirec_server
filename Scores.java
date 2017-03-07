import java.io.*;
import java.util.*;

public class Scores {
  /*
  public static final String[] TASKS = {
    "海边派对的搭配","春天在哪里","办公室明星","夏日物语","圣诞家庭聚会","年轻的春游","运动进行时","金色音乐厅","夏季游园会","女王大人","冬天里的一把火",
    "大侦探福尔摩斯","宫廷歌舞会","奇幻童话园","有女初长成","绝色无双","清秀佳人"};
    */

  public static final String[] TASKS = {
    "海边派对的搭配","圣诞家庭聚会","办公室明星","女王大人","运动进行时"};
	private static Scores INSTANCE;
  public static Map<String, String> taskMap = new HashMap<>();
  static {
    taskMap.put("海边派对的搭配", "今年要挑战冬泳！");
    taskMap.put("圣诞家庭聚会", "缤纷年夜饭");
    taskMap.put("办公室明星", "温雅格调");
    taskMap.put("女王大人", "新年派对女王");
    taskMap.put("运动进行时", "新春的晨跑装备");
  }

	private Map<String, Map<String, Score>> category = null;

  public static Scores getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new Scores();
    }
    return INSTANCE;
  }

	public void initialize(String depot) throws java.io.IOException {
    if (!initialized()) {
      category = loadData(depot);
    }
  }

  private boolean initialized() {
    return (category != null);
  }

  public static void main (String[] args) throws java.lang.Exception {
    Scores scores = new Scores();
    scores.initialize(".");
    scores.log();
  }

  private Map<String, Map<String, Score>> loadData(String depot) throws IOException {
  	Map<String, Map<String, Score>> ret = new HashMap<>();
    for (Detect.Theme theme : Detect.THEMES) {
      File file = new File(new File(depot), theme.label + ".csv");
      Map<String, Score> map = new HashMap<>();
      BufferedReader br = new BufferedReader(new FileReader(file));
      try {
      	String head = br.readLine();
      	String[] heads = head.split(",");
      	for (int i = 2; i < heads.length; i++) {
      	}
        String line;
		    while ((line = br.readLine()) != null) {
	        String[] data = line.split(",");
          String name = data[0];
          String id = data[1];
          Score score = new Score(name);
          for (int i = 2; i < heads.length; i++) {
            score.add(heads[i], data[i]);
          }
          map.put(id, score);
		    }
			} finally {
			    br.close();
			}
      ret.put(theme.label, map);
    }
    return ret;
  }

  public Score get(String type, String id) {
    return category.get(type).get(id);
  }

  private void log() {
    for (String theme : category.keySet()) {
      Map<String, Score> data = category.get(theme);
      System.err.println(theme + ": " + data.size());
      int i = 0;
      for (String id : data.keySet()) {
        System.err.print(id + ": ");
        Score score = data.get(id);
        System.err.print(score.name + ", ");
        for (String task : TASKS) {
          System.err.print(task + ": " + score.get(task) + " ");
        }
        System.err.println();
      }
    }
  }

  public static class Score {
    String name;
    Map<String, String> detailed;
    public Score(String name) {
      this.name = name;
      this.detailed = new HashMap<>();
    }

    public void add(String task, String score) {
      detailed.put(task, score);
    }

    public String get(String task) {
      return detailed.get(task);
    }
  }
}