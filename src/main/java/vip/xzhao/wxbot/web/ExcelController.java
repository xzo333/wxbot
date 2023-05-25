package vip.xzhao.wxbot.web;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vip.xzhao.wxbot.data.Orderdate;
import vip.xzhao.wxbot.data.Userdate;
import vip.xzhao.wxbot.service.OrderdateService;
import vip.xzhao.wxbot.service.UserdateService;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

@RestController
public class ExcelController {

    //订单密码
    private static final String OrderPassword = "3300533572@";
    //用户密码
    private static final String UserPassword = "3300533572";
    @Autowired
    private OrderdateService orderdateService;
    @Autowired
    private UserdateService userdateService;

    /**
     * 导出订单Excel文件
     *
     * @param response
     * @throws IOException
     */
    @GetMapping("/export/order")
    public void exportOrder(HttpServletResponse response, @RequestParam(required = false) String password) throws IOException {
        // 如果密码为空，则弹出输入框要求用户输入密码
        if (password == null) {
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().println("<html>");
            response.getWriter().println("<head>");
            response.getWriter().println("<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">");
            response.getWriter().println("<title>请输入密码</title>");
            response.getWriter().println("<style>");
            response.getWriter().println(".input-container {");
            response.getWriter().println("  display: flex;");
            response.getWriter().println("  justify-content: center;");
            response.getWriter().println("  align-items: center;");
            response.getWriter().println("  height: 100vh;");
            response.getWriter().println("}");
            response.getWriter().println(".input-container input[type='password'] {");
            response.getWriter().println("  border: none;");
            response.getWriter().println("  border-radius: 3px;");
            response.getWriter().println("  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);");
            response.getWriter().println("  padding: 10px 20px;");
            response.getWriter().println("  font-size: 16px;");
            response.getWriter().println("  min-width: 300px;");
            response.getWriter().println("}");
            response.getWriter().println(".input-container button {");
            response.getWriter().println("  background-color: #4CAF50;");
            response.getWriter().println("  color: white;");
            response.getWriter().println("  border: none;");
            response.getWriter().println("  border-radius: 3px;");
            response.getWriter().println("  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);");
            response.getWriter().println("  padding: 10px 20px;");
            response.getWriter().println("  font-size: 16px;");
            response.getWriter().println("  margin-left: 20px;");
            response.getWriter().println("}");
            response.getWriter().println("</style>");
            response.getWriter().println("</head>");
            response.getWriter().println("<body>");
            response.getWriter().println("<div class='input-container'>");
            response.getWriter().println("  <input type='password' placeholder='请输入订单表下载密码' id='password-input'>");
            response.getWriter().println("  <button onclick='submitPassword()'>确定</button>");
            response.getWriter().println("</div>");
            response.getWriter().println("<script>");
            response.getWriter().println("  function submitPassword() {");
            response.getWriter().println("    var password = document.getElementById('password-input').value;");
            response.getWriter().println("    location.href = '/export/order?password=' + password;");
            response.getWriter().println("  }");
            response.getWriter().println("</script>");
            response.getWriter().println("</body>");
            response.getWriter().println("</html>");
            return;
        }

        // 验证密码是否正确
        if (!OrderPassword.equals(password)) {
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().println("<html>");
            response.getWriter().println("<head>");
            response.getWriter().println("<script>");
            response.getWriter().println("  alert('密码错误');");
            response.getWriter().println("  history.go(-1);"); // 返回上一页
            response.getWriter().println("</script>");
            response.getWriter().println("</head>");
            response.getWriter().println("<body>");
            response.getWriter().println("</body>");
            response.getWriter().println("</html>");
            return;
        }
        // 查询数据库中的Orderdate数据
        List<Orderdate> orderdateList = orderdateService.getOrderdateList();

        // 设置响应头信息
        response.setContentType("application/ms-excel");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("订单信息.xlsx", "UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        // 创建ExcelWriter对象，指定输出流
        ServletOutputStream outputStream = response.getOutputStream();
        ExcelWriter excelWriter = EasyExcel.write(outputStream).build();

        // 创建Sheet对象
        WriteSheet writeSheet = EasyExcel.writerSheet(0, "订单信息").head(Orderdate.class).build();

        // 将数据写入到Excel文件中
        excelWriter.write(orderdateList, writeSheet);

        // 关闭资源
        excelWriter.finish();
        outputStream.flush();
        outputStream.close();
    }

    /**
     * 导出用户Excel文件
     *
     * @param response
     * @throws IOException
     */
    @GetMapping("/export/user")
    public void exportUser(HttpServletResponse response, @RequestParam(required = false) String password) throws IOException {
        // 如果密码为空，则弹出输入框要求用户输入密码
        if (password == null) {
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().println("<html>");
            response.getWriter().println("<head>");
            response.getWriter().println("<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">");
            response.getWriter().println("<title>请输入密码</title>");
            response.getWriter().println("<style>");
            response.getWriter().println(".input-container {");
            response.getWriter().println("  display: flex;");
            response.getWriter().println("  justify-content: center;");
            response.getWriter().println("  align-items: center;");
            response.getWriter().println("  height: 100vh;");
            response.getWriter().println("}");
            response.getWriter().println(".input-container input[type='password'] {");
            response.getWriter().println("  border: none;");
            response.getWriter().println("  border-radius: 3px;");
            response.getWriter().println("  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);");
            response.getWriter().println("  padding: 10px 20px;");
            response.getWriter().println("  font-size: 16px;");
            response.getWriter().println("  min-width: 300px;");
            response.getWriter().println("}");
            response.getWriter().println(".input-container button {");
            response.getWriter().println("  background-color: #4CAF50;");
            response.getWriter().println("  color: white;");
            response.getWriter().println("  border: none;");
            response.getWriter().println("  border-radius: 3px;");
            response.getWriter().println("  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);");
            response.getWriter().println("  padding: 10px 20px;");
            response.getWriter().println("  font-size: 16px;");
            response.getWriter().println("  margin-left: 20px;");
            response.getWriter().println("}");
            response.getWriter().println("</style>");
            response.getWriter().println("</head>");
            response.getWriter().println("<body>");
            response.getWriter().println("<div class='input-container'>");
            response.getWriter().println("  <input type='password' placeholder='请输入用户表下载密码' id='password-input'>");
            response.getWriter().println("  <button onclick='submitPassword()'>确定</button>");
            response.getWriter().println("</div>");
            response.getWriter().println("<script>");
            response.getWriter().println("  function submitPassword() {");
            response.getWriter().println("    var password = document.getElementById('password-input').value;");
            response.getWriter().println("    location.href = '/export/user?password=' + password;");
            response.getWriter().println("  }");
            response.getWriter().println("</script>");
            response.getWriter().println("</body>");
            response.getWriter().println("</html>");
            return;
        }

        // 验证密码是否正确
        if (!UserPassword.equals(password)) {
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().println("<html>");
            response.getWriter().println("<head>");
            response.getWriter().println("<script>");
            response.getWriter().println("  alert('密码错误');");
            response.getWriter().println("  history.go(-1);"); // 返回上一页
            response.getWriter().println("</script>");
            response.getWriter().println("</head>");
            response.getWriter().println("<body>");
            response.getWriter().println("</body>");
            response.getWriter().println("</html>");
            return;
        }
        // 查询数据库中的Userdate数据
        List<Userdate> userdateList = userdateService.getUserdateList();

        // 设置响应头信息
        response.setContentType("application/ms-excel");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("用户信息.xlsx", "UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        // 创建ExcelWriter对象，指定输出流
        ServletOutputStream outputStream = response.getOutputStream();
        ExcelWriter excelWriter = EasyExcel.write(outputStream).build();

        // 创建Sheet对象
        WriteSheet writeSheet = EasyExcel.writerSheet(0, "用户信息").head(Userdate.class).build();

        // 将数据写入到Excel文件中
        excelWriter.write(userdateList, writeSheet);

        // 关闭资源
        excelWriter.finish();
        outputStream.flush();
        outputStream.close();
    }
}

