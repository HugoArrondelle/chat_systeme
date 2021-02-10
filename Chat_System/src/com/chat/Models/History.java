package com.chat.Models;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Date;

public class History {
    public static ArrayList<Message> getHistory(String id) {
        Application.createConvHistoryFile(id);
        ArrayList<Message> messageList = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(Application.getHistoryPath().resolve(id), StandardCharsets.US_ASCII)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] line_split = line.split(":", 5);
                if (line_split[0].equals(StringMessage.id)) {
                    messageList.add(new StringMessage(line_split[3], line_split[1].equals("1"), new Date(Long.parseLong(line_split[2]))));
                }
                else if (line_split[0].equals(FileMessage.id)) {
                    messageList.add(new FileMessage(new File(line_split[4]), line_split[3], line_split[1].equals("1"), new Date(Long.parseLong(line_split[2]))));
                }
            }
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }
        return messageList;
    }

    public static void addToHistory(String id, Message message) {
        try (BufferedWriter writer = Files.newBufferedWriter(Application.getHistoryPath().resolve(id), StandardCharsets.US_ASCII, StandardOpenOption.APPEND)) {
            String m = message.toString();
            writer.write(m, 0, m.length());
            writer.newLine();
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }
    }
}
