package demo;

import com.utils.excel.ExcelRewriter;
import com.utils.excel.ISheetWriter;
import com.utils.excel.Rownum;
import com.utils.util.FPath;
import lombok.Builder;
import lombok.Cleanup;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

import static com.utils.excel.enums.Column.*;

@Slf4j
public class Excel {
    @SneakyThrows
    public static void main(String[] args) {
//        Workbook wb = WorkbookFactory.create(DIR.FILES.file("excel/联系人-111111.xlsx"), "111111", true);//设置密码打开
//        wb.forEach(sheet -> {
//            sheet.forEach(row->{
//                row.forEach(cell -> {
//                    System.out.println(cell.getStringCellValue());
//                });
//            });
//        });
//        System.out.println("=================================");
//        System.out.println("Number of Sheets:" + wb.getNumberOfSheets());
//        System.out.println("Sheet3's name:" + wb.getSheetName(0));
//        System.out.println();
        @Cleanup final ExcelRewriter rewriter = ExcelRewriter
                .of(
                        FPath.of("common-utils/src/test/files/excel/data-row-template.xlsx").file(),
                        ISheetWriter.Options.builder().build()
                )
                .sheet(0);
        final int fromRow = 0;
        final Rownum rownum = Rownum.of(11);
        Arrays.asList(
                DataRow.builder().id(1).name("Jack").phone("1234567980").amount(123456D).num(1.5).count(21).build(),
                DataRow.builder().id(2).name("Joe").amount(123456D).num(1.5).count(21).build(),
                DataRow.builder().id(3).name("Jhon").amount(123456D).num(1.5).count(21).build(),
                DataRow.builder().id(4).name("Joise").amount(123456D).num(1.5).count(21).build(),
                DataRow.builder().id(5).name("vcx").count(21).build(),
                DataRow.builder().id(6).name("fdsak").count(21).build(),
                DataRow.builder().id(7).name("Jackfdsa").phone("1234567980").amount(123456D).num(1.5).count(21).build(),
                DataRow.builder().id(8).name("Jack1").phone("1234567980").amount(123456D).build(),
                DataRow.builder().id(9).name("Jackf").phone("1234567980").amount(123456D).count(21).build(),
                DataRow.builder().id(10).name("Jack4").phone("1234567980").num(1.5).count(21).build()
        ).forEach(row -> {
            rewriter.copy(fromRow, rownum.index())
                    .row(rownum)
//                    .cell(B).writeNumber(row.getId())
                    .cell(C).writeString(row.getName())
                    .cell(D).writeString(row.getPhone())
                    .cell(E).writeNumber(row.getAmount())
                    .cell(F).writeNumber(row.getNum())
                    .cell(G).writeNumber(row.getCount())
            ;
            rownum.next();
            });
        rewriter.evaluateAllFormulaCells();
        log.info(String.format("写入路径：%s", rewriter.saveWorkBook(FPath.of("logs", "data-row-template-重写.xlsx")).absolute()));
    }

    @Data
    @Builder
    static class DataRow {
        private int id;
        private String name;
        private String phone;
        private Double amount;
        private double num;
        private int count;
    }
}
