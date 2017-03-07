// Import required java libraries
import java.io.*;
import java.util.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

// Extend HttpServlet class
public class NikkiRec extends HttpServlet {
  private static final int THRESHOLD = 2048 * 1024;

  String head = "<head>"
    + "<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'/>"
    + "<meta name='viewport' content='width=device-width, initial-scale=1' />"
    + "<title>奇迹暖暖灰科技</title>"
    + "<style>"
    + "table > tbody > tr > td {"
    + "  padding: 5px;"
    + "  background: #f8f8f8;"
    + "}"
    + "</style>"
    + "</head>";

  String context = "<h2>奇迹暖暖梦幻大使（哼！）服装识别器</h2>"
    + "<div>By Ivan's Workshop. Contributed by 玉人及其后宫</div>"
    + "<div>在小伙伴们的帮助下大致识别了各个关卡的真相，以下是总结，万一识别失败可以参考:"
    + "<ul>"
    + "<li>第一关: (海边派对的时候)顺便挑战个冬泳。高分搭配：露。低分搭配：裹。</li>"
    + "<li>第二关: (圣诞聚会吃太久了就成了)年夜饭。高分搭配：暖。低分搭配：冻。</li>"
    + "<li>第三关: (办公室明星就得)温雅格调。高分搭配：熟。低分搭配：嫩。</li>"
    + "<li>第四关: 新年的派对一样是(女王大人)。高分搭配：骚。低分搭配：土。</li>"
    + "<li>第五关: 新春的（运动进行时）装备。高分搭配：汉子。低分搭配：妹子。</li>"
    + "</ul>"
    + "</div>"
    + "<div>2016-02-04: 感谢玉人发扬了吵完架摔门而出不忘顺便买个菜回家的精神，不计前嫌地添加了1.6.6的各类衣服。跪谢</div>"
    + "<hr/>"
    + "<form method='POST' action='NikkiRec' enctype='multipart/form-data' >"
    + "<input type='file' name='file' accept='image/*'>"
    + "<input type='submit'>"
    + "</form>";

  String footer = "<div style='font-size:0.8em; color:#ccc'><hr/>"
    + "<h3><b>Dev Team</b></h3>"
    + "<p><b>TL</b></br>ip</p>"
    + "<p><b>PM</b></br>玉人</p>"
    + "<p><b>Operation</b></br>乐乐、暖医生、琉璃、饭饭、云韵、桂花</p>"
    + "<p><b>Operation</b></br>鸾雨生， ako， Crayon， 苏爷，樱桃大丸子，二凡， 鹅肝，白无常神教</p>"
    + "<p><b>Researcher</b></br>夏天*薇</p>"
    + "<p><b>QA</b></br>小五、球球、eighter、兔子、蓝蓝</p></div>";
  private Detect detect;
  private Scores scores;

  public void init() throws ServletException
  {
    String fullPath = getServletContext().getRealPath("/WEB-INF");
    try {
      detect = Detect.getInstance();
      detect.initialize(fullPath /*"/home/ec2-user/nikkirec/dressraw"*/);
      scores = Scores.getInstance();
      scores.initialize(fullPath + "/scores");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void doPost(HttpServletRequest request,
                    HttpServletResponse response)
            throws ServletException, IOException {
    long a = System.currentTimeMillis();
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println("<html>");
    out.println("<head>");
    out.println(head);
    out.println(detect.getSampleMeta());
    out.println("</head>");
    out.println("<body>");
    FileItemFactory factory = new DiskFileItemFactory(THRESHOLD, null);
    //factory.setSizeThreshold(THRESHOLD);
    ServletFileUpload upload = new ServletFileUpload(factory);
    
    out.println(context);
    try {
      long b = System.currentTimeMillis();
      List<FileItem> fields = upload.parseRequest(request);
      Iterator<FileItem> it = fields.iterator();
      if (!it.hasNext()) {
        out.println("No fields found");
        return;
      }
      long c = System.currentTimeMillis();
      while (it.hasNext()) {
        out.println("<tr>");
        FileItem fileItem = it.next();
        boolean isFormField = fileItem.isFormField();
        if (!isFormField) {
          long d = System.currentTimeMillis();
          BufferedImage img = ImageIO.read(fileItem.getInputStream());
          final int origWidth = img.getWidth();
          final int origHeight = img.getHeight();
          /*
          final int origWidth = result.thumb.getWidth();
          final int origHeight = result.thumb.getHeight();
          */

          BufferedImage thumb = new BufferedImage(origWidth * 480 / origHeight, 480, BufferedImage.TYPE_3BYTE_BGR);
          Graphics2D g = thumb.createGraphics();
          g.drawImage(img, 0, 0, origWidth * 480 / origHeight, 480, null);
          g.dispose();
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          ImageIO.write(thumb, "jpg", baos);
          String imageString = "data:image/jpg;base64," +
              DatatypeConverter.printBase64Binary(baos.toByteArray());
          out.println("<div><img src='" + imageString +  "' /></div>");
          Map<Detect.Theme, Detect.MatchingResult> results = detect.matching(img);
          long e = System.currentTimeMillis();
          String[] rows = {"结果", "名称", "id"};
          List<List<String>> table1 = new ArrayList();
          List<List<String>> table2 = new ArrayList();

          for (Detect.Theme theme : Detect.THEMES) {
            Detect.MatchingResult result = results.get(theme);
            Scores.Score score1 = scores.get(theme.label, result.id1);
            Scores.Score score2 = scores.get(theme.label, result.id2);
            List<String> column1 = new ArrayList();
            List<String> column2 = new ArrayList();
            table1.add(column1);
            table2.add(column2);
            column1.add(genImg(theme.label, result.id1));
            column1.add(score1 == null ? "--" : score1.name);
            column1.add(String.format("%s(%1.3f)", result.id1, result.similarity1));
            column2.add(genImg(theme.label, result.id2));
            column2.add(score2 == null ? "--" : score2.name);
            column2.add(String.format("%s(%1.3f)", result.id2, result.similarity2));
            for (String task : Scores.TASKS) {
              column1.add(score1 == null ? "--" : score1.get(task));
              column2.add(score2 == null ? "--" : score2.get(task));
            }
          }
          out.println("<div>");
          out.println("<table style='white-space: nowrap'>");
          for (int i = 0; i < rows.length; i++) {
            out.println("<tr>");
            for (List<String> list : table1) {
              out.println(td(list.get(i)));
            }
            out.println(td(rows[i]));
            for (List<String> list : table2) {
              out.println(td(list.get(i)));
            }
            out.println("</tr>");
          }
          for (int i = 0; i < Scores.TASKS.length; i++) {
            out.println("<tr>");
            for (List<String> list : table1) {
              out.println(td(list.get(i + rows.length)));
            }
            out.println(td(Scores.taskMap.get(Scores.TASKS[i])));
            for (List<String> list : table2) {
              out.println(td(list.get(i + rows.length)));
            }
            out.println("</tr>");
          }
          out.println("</table>");
          out.println("</div>");
          //out.println("<div><span>" + result.id1 + "</span>&nbsp;<span>" + result.id2 + "</span></div>");
          //out.println("<div>" + (b-a) + " " + (c-b) + " " + (d-c) + " " + (e-d) + "</div>");
          //BufferedImage img = ImageIO.read(fileItem.getInputStream());
          out.println(footer);
          out.println("</body></html>");
        }
      }
    } catch (FileUploadException e) {
      e.printStackTrace();
    }
  }

  private String td(String a) {
    return "<td>" + a + "</td>";
  }

  private String genImg(String type, String id) {
    return "<img src='http://ec2-54-200-184-46.us-west-2.compute.amazonaws.com/" + type + "/" + id + ".jpg'/>";
  }

  public void doGet(HttpServletRequest request,
                    HttpServletResponse response)
            throws ServletException, IOException
  {
      // Set response content type
      response.setContentType("text/html; charset=UTF-8");

      // Actual logic goes here.
      PrintWriter out = response.getWriter();
      out.println("<html>");
      out.println("<head>");
      out.println(head);
      out.println(detect.getSampleMeta());
      out.println("</head>");
      out.println("<body>");
      out.println(context);
      out.println(footer);
      out.println("</body></html>");
  }
  
  public void destroy()
  {
      // do nothing.
  }
}