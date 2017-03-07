import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.io.File;
import java.util.*;

import javax.imageio.ImageIO;

public class Detect
{
  private static Detect INSTANCE;

  private static final int HEIGHT = 800;
  private static final XY SAMPLE_ANCHOR = new XY(71, 194);
  private static final XY PHONE_ANCHOR1 = new XY(89, 33);
  private static final XY PHONE_ANCHOR2 = new XY(89, 249);
  private static final XY PHONE4_ANCHOR1 = new XY(99, 58);
  private static final XY PHONE4_ANCHOR2 = new XY(99, 314);
  private static final XY PAD_ANCHOR1 = new XY(95, 78);
  private static final XY PAD_ANCHOR2 = new XY(95, 348);

  private static final XY SAMPLE_BODY = new XY(195, 255); // 195, 255
  private static final XY BODY_SIZE = new XY(300, 100);
  private static final XY SAMPLE_TOP = new XY(185, 245);
  private static final XY TOP_SIZE = new XY(100, 120);
  private static final XY SAMPLE_BOTTOM = new XY(330, 250);
  private static final XY BOTTOM_SIZE = new XY(100, 120);

  private static final double SAMPLE_RATIO = 1;
  private static final double PHONE_RATIO = 0.8;
  private static final double PHONE4_RATIO = 0.77;
  private static final double PAD_RATIO = 0.8;

  public static final Theme[] THEMES = {
    new Theme("dressraw", "dress", "连衣裙", SAMPLE_BODY, BODY_SIZE),
    new Theme("topsraw", "tops", "上衣", SAMPLE_TOP, TOP_SIZE),
    new Theme("bottomsraw", "bottoms", "下装", SAMPLE_BOTTOM, BOTTOM_SIZE)
  };

  private enum Device {
    PHONE(PHONE_RATIO, PHONE_ANCHOR1, PHONE_ANCHOR2),
    PHONE4(PHONE4_RATIO, PHONE4_ANCHOR1, PHONE4_ANCHOR2),
    PAD(PAD_RATIO, PAD_ANCHOR1, PAD_ANCHOR2);

    Device(double ratio, XY anchor1, XY anchor2) {
      this.ratio = ratio;
      this.anchor1 = anchor1;
      this.anchor2 = anchor2;
    }

    public double ratio;
    public XY anchor1;
    public XY anchor2;
  };
  
  private Map<Theme, Map<String, Clothes>> clothes = null;

  public static Detect getInstance() throws java.io.IOException {
    if (INSTANCE == null) {
      INSTANCE = new Detect();
    }
    return INSTANCE;
  }

  private Detect() {
  }

  public void initialize(String depot) throws java.io.IOException {
    if (!initialized()) {
      clothes = loadSamples(depot);
    }
  }

  private boolean initialized() {
    return (clothes != null);
  }

	public static void main (String[] args) throws java.lang.Exception {
		// uncomment following to crop and generate training data from raw
    processSample();

		// uncomment following to generate wardrobe statistics
    // Detect detect = Detect.getInstance();
    // detect.initialize(".");
    // System.err.println(detect.getSampleMeta());

		// uncomment following to run unit tests
    // Map<Theme, Map<String, Clothes>> clothes = loadSamples(".");
    // File currentDir = new File("choice");
    // File[] files = currentDir.listFiles();
    // for (File file : files) {
    //   if (file.isFile() && !file.getName().startsWith(".")) {
    //     Map<Theme, MatchingResult> results = matching(clothes, file);
    //     for (Theme theme : results.keySet()) {
    //       MatchingResult result = results.get(theme);
    //       ImageIO.write(result.thumb, "jpg", new File("output", theme.label + "_" + file.getName()));
    //     }
    //   }
    // }
	}

  public String getSampleMeta() {
    StringBuilder builder = new StringBuilder();
    builder.append("<meta name='wardrobe' content='");
    for (Theme theme : clothes.keySet()) {
      builder.append(theme.name).append(":");
      int i = 0;
      for (String id : clothes.get(theme).keySet()) {
        builder.append(id);
        i++;
        if (i < clothes.get(theme).size()) {
          builder.append(",");
        }
      }
      builder.append("|");
    }
    builder.append("'/>");
    return builder.toString();
  }

  public static Map<Theme, Map<String, Clothes>> loadSamples(String depot) throws java.io.IOException {
    Map<Theme, Map<String, Clothes>> ret = new HashMap<>();
    for (Theme theme : THEMES) {
      File currentDir = new File(new File(depot), theme.feed);
      File[] files = currentDir.listFiles();
      Map<String, Clothes> map = new HashMap<>();
      for (File file : files) {
        if (file.getName().endsWith("jpg")) {
          int idx = file.getName().indexOf(".");
          map.put(file.getName().substring(0, idx), processSample(theme, file, null));
        } else {
          System.err.println("wrong format: " + file.getName());
        }
      }
      ret.put(theme, map);
    }
    return ret;
  }

  private static void processSample() throws java.io.IOException {
    for (Theme theme : THEMES) {
      File currentDir = new File(theme.feed);
      File outputDir = new File(theme.label);
      if (!outputDir.exists()) {
        outputDir.mkdir();
      } 
      File[] files = currentDir.listFiles();
      for (File file : files) {
        if (file.getName().endsWith("jpg")) {
          processSample(theme, file, outputDir);
        } else {
          System.err.println("wrong format: " + file.getName());
        }
      }
    }
  }

  private static Map<Theme, MatchingResult> matching(Map<Theme, Map<String, Clothes>> samples, File input) throws java.io.IOException {
    System.err.println(input.getName());
    BufferedImage original = ImageIO.read(input);
    return matching(samples, original);
  }

  public Map<Theme, MatchingResult> matching(BufferedImage original) throws java.io.IOException {
    return matching(this.clothes, original);
  }

  private static int blacks(BufferedImage img, int x) {
    int cnt = 0;
    for (int j = 0; j < img.getHeight(); j++) {
      int rgb = img.getRGB(x, j);
      int red =   (rgb >> 16) & 0xFF;
      int green = (rgb >>  8) & 0xFF;
      int blue =  (rgb      ) & 0xFF;
      if (red <5 && green <5 && blue <5) {
        cnt ++;
      }
    }
    return cnt;
  }

  private static int vblacks(BufferedImage img, int y) {
    int cnt = 0;
    for (int i = 0; i < img.getWidth(); i++) {
      int rgb = img.getRGB(i, y);
      int red =   (rgb >> 16) & 0xFF;
      int green = (rgb >>  8) & 0xFF;
      int blue =  (rgb      ) & 0xFF;
      if (red <5 && green <5 && blue <5) {
        cnt ++;
      }
    }
    return cnt;
  }

  private static Map<Theme, MatchingResult> matching(Map<Theme, Map<String, Clothes>> samples, BufferedImage original)
      throws java.io.IOException {
    final int origWidth = original.getWidth();
    final int origHeight = original.getHeight();
    // Let's cut the black border first;
    int left;
    int right = origWidth;
    int bottom = origHeight;
    for (left = 0; left < origWidth / 2; left++) {
      if (blacks(original, left) < origHeight / 2) {
        break;
      }
    }
    for (right = origWidth -1; right > origWidth / 2; right--) {
      if (blacks(original, right) < origHeight / 2) {
        break;
      }
    }
    for (bottom = origHeight - 1; bottom > origHeight / 2; bottom--) {
      if (vblacks(original, bottom) < origWidth / 2) {
        break;
      } 
    }
    BufferedImage cut = original.getSubimage(left, 0, right - left + 1, bottom + 1);

    BufferedImage img = new BufferedImage(origWidth * 800 / origHeight, 800, BufferedImage.TYPE_3BYTE_BGR);
    Graphics2D g = img.createGraphics();
    g.drawImage(cut, 0, 0, cut.getWidth() * 800 / cut.getHeight(), 800, null);
    g.dispose();

    final int width = img.getWidth();
    final int height = img.getHeight();
    boolean isPad = false;
    Device device = Device.PHONE;
    if (width * 16 > height * 11) {
      device = Device.PAD;
    } else if (width * 3 > height * 2 - 10) {
      device = Device.PHONE4;
    }

    Map<Theme, MatchingResult> ret = new HashMap<>();
    long a = System.currentTimeMillis();
    for (Theme theme : samples.keySet()) {
      int x1, x2, y1, y2, w, h;

      x1 = (int) ((theme.location.col - SAMPLE_ANCHOR.col) * device.ratio + device.anchor1.col);
      x2 = (int) ((theme.location.col - SAMPLE_ANCHOR.col) * device.ratio + device.anchor2.col);
      y1 = (int) ((theme.location.row - SAMPLE_ANCHOR.row) * device.ratio + device.anchor1.row);
      y2 = (int) ((theme.location.row - SAMPLE_ANCHOR.row) * device.ratio + device.anchor2.row);

      w = (int) (theme.size.col * device.ratio);
      h = (int) (theme.size.row * device.ratio);

      System.err.println(theme.label + " - " + x1 + ", " + y1 + " " + w + "x" + h);

      Clothes c1 = crop(img, x1, y1, w, h, theme.size);
      Clothes c2 = crop(img, x2, y2, w, h, theme.size);

      BestResult result1 = find(c1, samples.get(theme));
      BestResult result2 = find(c2, samples.get(theme));
      System.err.println(result1 + " " + result2);
      
      BufferedImage copied = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
      Graphics2D g2 = copied.createGraphics();
      g2.drawImage(img, 0, 0, null);
      g2.setColor(Color.green);
      g2.setStroke(new BasicStroke(2));
      g2.setFont(new Font("Arial", Font.BOLD, 25)); 
      g2.drawRect(x1, y1, w, h);
      g2.drawString(result1.id, x1+10, y1+25);
      g2.drawRect(x2, y2, w, h);
      g2.drawString(result2.id, x2+10, y2+25);
      g2.dispose();

      MatchingResult result = new MatchingResult();

      result.thumb = copied;
      result.id1 = result1.id;
      result.id2 = result2.id;
      result.similarity1 = result1.similarity;
      result.similarity2 = result2.similarity;
      ret.put(theme, result);
    }

    long b = System.currentTimeMillis();
    System.err.println((b-a) + " ms");
    return ret;
  }

  private static BestResult find(Clothes c, Map<String, Clothes> sample) {
    int minSim = 1<<30;
    String ret = "";
    
    for (String id : sample.keySet()) {
      Clothes s = sample.get(id);
      int result = c.similarityDetails(s);
      if (result < minSim) {
        minSim = result;
        ret = id;
      }
    }
    /*
    List<Filtered> filtered = firstPass(c, sample);
    for (int i = 0; i < Math.min(100, filtered.size()); i++) {
      Filtered f = filtered.get(i);
      Clothes s = f.c;
      int result = c.similarityDetails(s);
      if (result < minSim) {
        minSim = result;
        ret = f.id;
      }
    }
    */
    return new BestResult(ret, - 1.0 * minSim / c.data.length);
  }

  private static List<Filtered> firstPass(Clothes c, Map<String, Clothes> sample) {
    List<Filtered> filtered = new ArrayList<>();
    for (String id : sample.keySet()) {
      Filtered f = new Filtered();
      f.id = id;
      f.c = sample.get(id);
      f.sim = c.similarity(f.c);
      filtered.add(f);
    }
    Collections.sort(filtered, new Filtered.Comp());
    return filtered;
  }

  private static Clothes processSample(Theme theme, File input, File outputDir) throws java.io.IOException {
    //System.err.println(input.getName());
    BufferedImage original = ImageIO.read(input);
    final int origWidth = original.getWidth();
    final int origHeight = original.getHeight();
    if (origHeight != 800) {
      System.err.println("Wrong file size: " + input.getName());
    }

    if (outputDir != null) {
      crop(original, theme.location.col, theme.location.row, theme.size.col, theme.size.row, theme.size,
          new File(outputDir, input.getName()));
      return null;
      /*
      Graphics2D g2 = original.createGraphics();
      g2.setColor(Color.green);
      g2.drawRect(SAMPLE_BODY.col, SAMPLE_BODY.row, BODY_SIZE.col, BODY_SIZE.row);
      g2.dispose();
      ImageIO.write(original, "jpg", new File(outputDir, input.getName()));
      return null;
      */
    } else {
      return crop(original, theme.location.col, theme.location.row, theme.size.col, theme.size.row, theme.size);
    }
  }

  private static Clothes crop(BufferedImage img, double x, double y, double w, double h, XY xy) throws java.io.IOException {
    byte[] pixels = ((DataBufferByte) crop(img, x, y, w, h, xy, null).getRaster().getDataBuffer()).getData();
    return new Clothes(xy.col, xy.row, pixels);
  }

  private static BufferedImage crop(BufferedImage img, double x, double y, double w, double h, XY xy, File output)
      throws java.io.IOException  {
    int ix = (int) x;
    int iy = (int) y;
    int iw = (int) w;
    int ih = (int) h;
    BufferedImage cropped = img.getSubimage(ix, iy, iw, ih);
    if (output != null) {
      ImageIO.write(cropped, "jpg", output);
      return null;
    } else {
      BufferedImage resized = new BufferedImage(xy.col, xy.row, BufferedImage.TYPE_3BYTE_BGR);
      Graphics2D g = resized.createGraphics();
      g.drawImage(cropped, 0, 0, xy.col, xy.row, null);
      g.dispose();
      return resized;
    }
  }

  public static class Theme {
    public String feed;
    public String label;
    public String name;
    public XY location;
    public XY size;

    public Theme(String feed, String label, String name, XY location, XY size) {
      this.feed = feed;
      this.label = label;
      this.name = name;
      this.location = location;
      this.size = size;
    }
  }

  public static class MatchingResult {
    public BufferedImage thumb;
    public String id1;
    public String id2;
    public double similarity1;
    public double similarity2;
  }

  public static class BestResult {
    public String id;
    public double similarity;
    public BestResult(String id, double similarity) {
      this.id = id;
      this.similarity = similarity;
    }

    public String toString() {
      return String.format("%s(%1.3f)", id, similarity);
    }
  }

  public static class XY {
    int row; // row
    int col; // col
    public XY(int row, int col) {
      this.row = row;
      this.col = col;
    }
  }

  private static class Filtered {
    String id;
    Clothes c;
    int sim;
    public static class Comp implements Comparator {
      public final int compare(Object a, Object b) {
        Filtered first = (Filtered) a;
        Filtered second = (Filtered) b;
        if (first.sim < second.sim) {
          return -1;
        } else if (first.sim > second.sim) {
          return 1;
        } else {
          return 0;
        }
      }
    }
  }
}