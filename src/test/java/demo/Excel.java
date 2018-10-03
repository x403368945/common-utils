package demo;

import enums.DIR;
import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class Excel {
    @SneakyThrows
    public static void main(String[] args) {
        Workbook wb = WorkbookFactory.create(DIR.FILES.file("excel/联系人-111111.xlsx"), "111111", true);//设置密码打开
        wb.forEach(sheet -> {
            sheet.forEach(row->{
                row.forEach(cell -> {
                    cell.setCellType(CellType.STRING);
                    System.out.println(cell.getStringCellValue());
                });
            });
        });
        System.out.println("=================================");
        System.out.println("Number of Sheets:" + wb.getNumberOfSheets());
        System.out.println("Sheet3's name:" + wb.getSheetName(0));
        System.out.println();
    }
}
