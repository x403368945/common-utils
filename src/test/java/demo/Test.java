package demo;

import com.utils.util.Dates;

public class Test {

    public static void main(String[] args) {
        System.out.println(Dates.parse("2018-12-05 10:11:12:333"));
        System.out.println(Dates.parse("2018-12-05"));
        System.out.println(Dates.parse("10:11:12:333"));
        System.out.println(Dates.parse("10:11:12"));
    }
}
