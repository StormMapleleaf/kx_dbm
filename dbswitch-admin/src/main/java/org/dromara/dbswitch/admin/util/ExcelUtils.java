
package org.dromara.dbswitch.admin.util;

import com.alibaba.excel.EasyExcel;
import org.dromara.dbswitch.admin.common.exception.DbswitchException;
import org.dromara.dbswitch.admin.common.response.ResultCode;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import javax.servlet.http.HttpServletResponse;


public final class ExcelUtils {

  public static <T> void write(HttpServletResponse response, Class<T> clazz, List<T> list, String fileName,
      String sheetName) {
    try {
      fileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
      response.setContentType("application/vnd.ms-excel");
      response.setCharacterEncoding("utf-8");
      response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
      response.setHeader("Cache-Control", "no-cache");
      response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
      EasyExcel.write(response.getOutputStream(), clazz)
          .sheet(sheetName)
          .doWrite(list);
    } catch (IOException ex) {
      throw new DbswitchException(ResultCode.ERROR_INTERNAL_ERROR, ex.getMessage());
    }

  }
}
