package org.example;

import org.junit.jupiter.api.Test;

public class uploadFileTest {
    @Test
    public void test1() {
        String fileName = "testtest.jpg";
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        System.out.println(suffix);
    }
}
