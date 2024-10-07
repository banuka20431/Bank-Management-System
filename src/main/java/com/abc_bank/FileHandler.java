package com.abc_bank;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class FileHandler {

    public static void write(String Data, String Path) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(Path))) {
            writer.write(Data);
        }
    }

    public static void write(String fileName, String printStr, String Path) throws IOException {
        write(printStr, Path + fileName);
    }

    public static String read(String Path) throws IOException {
        StringBuilder jsonStrBuilder = new StringBuilder();
        int out;
        try (FileReader fileReader = new FileReader(Path)) {
            out = fileReader.read();
            while (out != -1) {
                jsonStrBuilder.append(((char) out));
                out = fileReader.read();
            }
        } catch (FileNotFoundException _) {
            File file = new File(Path);
            FileWriter fWriter = new FileWriter(Path);
            fWriter.append('{');
            fWriter.append('}');
            boolean _ = file.createNewFile();
            fWriter.close();
            return "{}";
        }
        return String.valueOf(jsonStrBuilder);
    }
}
