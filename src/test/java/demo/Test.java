package demo;

import org.apache.poi.ss.util.CellAddress;

public class Test {

    public static void main(String[] args) {
        System.out.println("ref!A1:B1".replaceAll("[^A-Za-z0-9]", ""));
        System.out.println("Sheet1!A1:B1".replaceAll("([A-Z]+)(\\d+):([A-Z]+)(\\d+)", "\\$$1\\$$2:\\$$3\\$$4"));
        System.out.println("Sheet1!A1:AB1".replaceAll("([A-Z]+)(\\d+):([A-Z]+)(\\d+)", "\\$$1\\$$2:\\$$3\\$$4"));
        System.out.println(new CellAddress("A1").formatAsString());

    }
}
