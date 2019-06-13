package com.company;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class datajoin {

    private static Date ParseDate(String stringDate,DateFormat df) throws ParseException {
        return df.parse(stringDate);
    }

    private static Date FindDate(String[] words) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
//        Date buff = ParseDate(words[2]+" "+words[3], dateFormat);
        Date buff = dateFormat.parse(words[2] + " " + words[3]);
        return buff != null ? buff : dateFormat.parse(words[4]+ " " + words[5]);
    }

    private static String FindLost(String line) {
        String[] words = line.split(";");

        if (words[0].equals("NULL")^words[1].equals("NULL")) {
            try {
                BufferedReader DB = new BufferedReader(new InputStreamReader(new FileInputStream("Database.csv")/*, "Cp1251"*/));
                DB.readLine();//строка заголовка

                Calendar date = Calendar.getInstance();
                date.setTime(FindDate(words));
                String Answer = null;
                boolean exclusive = false;
                byte lostColumn = words[0].equals("NULL") ? (byte) 0 : (byte) 1;//какое именно поле отсутствует

                while ((line = DB.readLine()) != null) {
                    String[] wordsTemp = line.split(";");
                    if (date.after(ParseDate(wordsTemp[2], new SimpleDateFormat("dd.MM.yyyy"))) && date.before(ParseDate(wordsTemp[3], new SimpleDateFormat("dd.MM.yyyy"))))
                    {
                        if (words[lostColumn].equals(wordsTemp[lostColumn])) {
                            Answer = wordsTemp[lostColumn];
                            if (exclusive) {
                                exclusive = false;
                                break;
                            } else
                                exclusive = true;
                        }
                    }
                }
                if (exclusive) {
                    words[lostColumn]= Answer;
                }
                DB.close();
                return String.join(";",words);
            }
            catch(IOException ex)
            {
                System.out.println("Ошибка открытия файлов. Проверьте целостность файла базы фамилий-номеров.");
                System.out.println(ex.toString());
            } catch (ParseException e) {
                System.out.println("Parse exception");
                e.printStackTrace();
            }
        }
        return line;
    }

    public static void main(String[] args) {
        try {
            BufferedReader inputData = new BufferedReader(new InputStreamReader(new FileInputStream("BigExample.csv")/*,"Cp1251"*/));
            BufferedWriter outputData = new BufferedWriter (new FileWriter("Answer.csv"));

            outputData.write(inputData.readLine().trim()+"\n"); //Запись заголовков в файл ответа.

            ArrayList<String> lackingDevices = new ArrayList<>(); //недостающие устройства зоны
            ArrayList<String> missingLines = new ArrayList<>(); //очередь пропущенных строк

            String[] words; //массив для разбиения строки на слова
            String[] lineInWork = null;
            String DevZone;
            String line;
            boolean placeCheck;
            boolean dataCheck;

            Calendar date = Calendar.getInstance();
            Calendar date2 = Calendar.getInstance();

            boolean workInProgress = false;
            boolean workInQueue;
            int queuePos = 0;

            while((line = inputData.readLine()) != null || !missingLines.isEmpty()) {
                if (line != null)
                    line = FindLost(line);//дозаполнение полей, если это возможно
                if (workInProgress) {
                    if (queuePos < missingLines.size()) {
                        if (line != null)
                            missingLines.add(line);
                        line = missingLines.get(queuePos);
                        workInQueue = true;
                    }
                    else
                        workInQueue = false;

                    words = line.split(";");

                    date2.setTime(FindDate(words));

                    if (java.lang.Math.abs(date2.getTimeInMillis() - date.getTimeInMillis()) <= 300000) { //проверка даты
                        placeCheck = lackingDevices.contains(words[10]);//проверка на искомое устройство
                        dataCheck = (words[0].equals("NULL")^words[1].equals("NULL") ?
                                lineInWork[0].equals(words[0])||lineInWork[1].equals(words[1]) :
                                lineInWork[0].equals(words[0])&&lineInWork[1].equals(words[1]));//фамилия И номер, или, если одного из полей нет, Фамилия ИЛИ номер

                        if (placeCheck && dataCheck) {
                            outputData.write(line+"\n");
                            lackingDevices.remove(words[10]);//убрать из списка отчёт найденного устройства
                            if (workInQueue) {
                                missingLines.remove(queuePos);//не забываем исключить из очереди
                            }
                            if (lackingDevices.isEmpty()) {
                                workInProgress = false;
                                outputData.write("Completeness: True\n");
                                queuePos = 0;
                            }
                        }
                        else {
                            if (!workInQueue) {
                                missingLines.add(line);//пропущенная строка
                            }
                            queuePos++;
                        }
                    }
                    else {
                        if (!lackingDevices.isEmpty())
                            outputData.write("Completeness: False\n");
                        if (!workInQueue) {
                            missingLines.add(line);//пропущенная строка
                        }
                        workInProgress = false;
                        queuePos = 0;
                    }
                }
                else {
                    if (!missingLines.isEmpty()) {
                        if (line != null)
                            missingLines.add(line);
                        line = missingLines.get(0);
                        missingLines.remove(0);
                    }
                    lineInWork = line.split(";");

                    BufferedReader zonesDB = new BufferedReader(new InputStreamReader(new FileInputStream("Zones.csv")/*,"Cp1251"*/));
                    lackingDevices.clear();

                    date.setTime(FindDate(lineInWork));

                    while((DevZone = zonesDB.readLine())!=null) {
                        words = DevZone.split(";");
                        if (Objects.equals(lineInWork[9], words[0]))//9-ый параметр - Зона
                            lackingDevices.add(words[1]);
                    }
                    lackingDevices.remove(lineInWork[10]);//убрать устройство, по отчёту которого мы ищем остальные

                    outputData.write(line+"\n");
                    workInProgress=true;
                    zonesDB.close();
                }
            }
            if (workInProgress)
                outputData.write("Completeness: False");
            outputData.close();
            inputData.close();

            System.out.println("Answer ready");
            postProcess();
            System.out.println("All done, check FullAnswer file");
        }
        catch(IOException ex) {
            System.out.println("Ошибка открытия файлов. Проверьте целостность файлов.");
            System.out.println(ex.toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private static void postProcess() throws IOException {
        BufferedReader inputData = new BufferedReader(new InputStreamReader(new FileInputStream("Answer.csv")));
        BufferedWriter outputData = new BufferedWriter (new FileWriter("FinalAnswer.csv"));
        outputData.write(inputData.readLine().trim()+"\n"); //Запись заголовков в файл ответа.

        String line;
        String[][] singleEventLines = new String[6][11];
        String[] singleEventLine = new String[11];
        int eventReadingCounter = 0;
        while ((line = inputData.readLine()) != null) {
            if (line.contains("Completeness")) {
                outputData.write(String.join(";",singleEventLine) + "\n");
                eventReadingCounter = 0;
                continue;
            }

            singleEventLines[eventReadingCounter++] = line.split(";");
            if (eventReadingCounter > 1) {
                for (int i = 0; i < singleEventLine.length; i++) {
                    for (int j = 0; j < eventReadingCounter; j++) {
                        if (!singleEventLines[j][i].isEmpty() /*|| !singleEventLines[j][i].equals("NULL")*/) {
                            singleEventLine[i] = singleEventLines[j][i];
                        }
                    }
                }
            } else {
                singleEventLine = line.split(";");
            }
        }
        outputData.close();
        inputData.close();
    }
}

