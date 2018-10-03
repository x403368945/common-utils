package demo;

import org.apache.poi.ss.util.CellAddress;

public class Test {

    public static void main(String[] args) {
        System.out.println("ref!A1:B1".replaceAll("[^A-Za-z0-9]", ""));
        System.out.println("Sheet1!A1:B1".replaceAll("([A-Z]+)(\\d+):([A-Z]+)(\\d+)", "\\$$1\\$$2:\\$$3\\$$4"));
        System.out.println("Sheet1!A1:AB1".replaceAll("([A-Z]+)(\\d+):([A-Z]+)(\\d+)", "\\$$1\\$$2:\\$$3\\$$4"));
        System.out.println(new CellAddress("A1").formatAsString());
        System.out.println("该单元格的值将会作为E列选项，该单元格的值将会作为E列选项".length());

        System.out.println("a.xlsx".matches("[\\s\\S]*\\.xls(x)?$"));
        System.out.println("a.xls".matches("[\\s\\S]\\.xls(x)?$"));
        System.out.println("a.xlss".matches("[\\s\\S]\\.xls(x)?$"));
        System.out.println("rL8aqMy3ZV8yiB/hui2WUA==".replaceAll("\\w", "*"));

    }
}
