package ru.shift;

import ru.shift.data.DataFile;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final int MIN_NUMBER_ARGS = 3;

    public static void main(String[] args) {

        String outFile = "";
        ArrayList<String> inFiles = new ArrayList<>();
        boolean ascSortMode = true;
        boolean digitType = true;

        if (!checkArgs(args)) {
            printHelp();
            return;
        }

        for (String arg : args) {
            if (arg.equals("-d")) {
                ascSortMode = false;
            } else if (arg.equals("-a")) {
                ascSortMode = true;
            } else if (arg.equals("-i")) {
                digitType = true;
            } else if (arg.equals("-s")) {
                digitType = false;
            } else {
                if (outFile.isEmpty()) {
                    outFile = arg;
                } else {
                    inFiles.add(arg);
                }
            }
        }

        List<DataFile> dataFiles = getDataFiles(inFiles);
        DataFile dataFile;
        DataFile fileForWrite = new DataFile(0, 0, new File(outFile));

        while ((dataFile = findDataFile(dataFiles)) != null) {
            int number = dataFile.getCurrentLineNumber() + 1;
            String data = dataFile.readData(number);

            if (!isDigit(data) && digitType) {
                dataFile.setCurrentLineNumber(dataFile.getCurrentLineNumber() + 1);
                continue;
            }

            String result = "";
            for (DataFile file : dataFiles) {
                if (dataFile != file && file.getCurrentLineNumber() + 1 <= file.getNumberLines()) {
                    int currentLineNumber = file.getCurrentLineNumber();
                    while (currentLineNumber < file.getNumberLines()) {
                        String currentData = file.readData(currentLineNumber + 1);
                        if (!isDigit(currentData) && digitType) {
                            currentLineNumber++;
                            continue;
                        }
                        if (digitType) {
                            result = compareIntegers(data, currentData, ascSortMode);
                        } else {
                            result = compareStrings(data, currentData, ascSortMode);
                        }
                        if (!result.equalsIgnoreCase(data)) {
                            number = currentLineNumber + 1;
                            dataFile = file;
                            data = result;
                        }
                        currentLineNumber++;
                    }
                }
            }
            dataFile.setCurrentLineNumber(number);
            fileForWrite.writeData(data);
        }
    }

    private static DataFile findDataFile(List<DataFile> dataFiles) {
        for (DataFile dataFile : dataFiles) {
            if (dataFile.getCurrentLineNumber() < dataFile.getNumberLines()) {
                return dataFile;
            }
        }
        return null;
    }

    private static boolean checkArgs(String[] args) {
        if (args.length < MIN_NUMBER_ARGS) {
            return false;
        }
        int countArgSortMode = 0;
        int countArgSortType = 0;
        int countArgTxt = 0;
        int countArgUnknown = 0;

        for (String arg : args) {
            if (arg.equals("-a") || arg.equals("-d")) {
                countArgSortMode++;
            } else if (arg.equals("-s") || arg.equals("-i")) {
                countArgSortType++;
            } else if (arg.endsWith(".txt")) {
                countArgTxt++;
            } else {
                countArgUnknown++;
            }
        }

        if (countArgSortMode > 1 || countArgSortType != 1 || countArgTxt < 2 || countArgUnknown > 0) {
            return false;
        }

        if (countArgSortMode == 0) {
            return args[0].equals("-i") || args[0].equals("-s");
        }
        return args[0].equals("-a") || args[0].equals("-d") || args[1].equals("-i") || args[1].equals("-s");
    }

    private static void printHelp() {
        System.out.println("Ошибка запуска программы! Неправильно указаны аргументы. Необходимо соблюдать указанный ниже порядок аргументов!");
        System.out.println("Аргументы запуска: ");
        System.out.println("1. -a: сортировка по возрастанию, -d: сортировка по убыванию. Аргумент необязательный, " +
                "по умолчанию используется по возрастанию");
        System.out.println("2. -s: сортировка строк, -i: сортировка целых чисел. Аргумент обязательный");
        System.out.println("3. имя выходного файла");
        System.out.println("4. имена входных файлов через пробел (необходим минимум один файл)");
    }

    private static List<DataFile> getDataFiles(List<String> filePaths) {
        List<DataFile> dataFiles = new ArrayList<>();
        for (String filePath : filePaths) {
            try {
                File file = new File(filePath);
                dataFiles.add(new DataFile(getNumberLines(file), 0, file));
            } catch (IllegalArgumentException ex) {
                System.out.println("Сортировка слиянием выполнена без данного файла.");
            }
        }
        return dataFiles;
    }

    private static int getNumberLines(File file) {
        try (BufferedReader bf = new BufferedReader(new FileReader(file))) {
            int numberLines = 0;
            while (bf.ready() && bf.readLine() != null) {
                ++numberLines;
            }
            return numberLines;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    private static boolean isDigit(String data) {
        if (data == null || data.isEmpty()) return false;
        for (int i = 0; i < data.length(); i++) {
            if (!Character.isDigit(data.charAt(i))) return false;
        }
        return true;
    }

    private static String compareStrings(String firstStr, String secondStr, boolean ascSortMode) {
        if (firstStr.compareToIgnoreCase(secondStr) >= 0) {
            if (!ascSortMode) {
                return firstStr;
            }
            return secondStr;
        } else {
            if (!ascSortMode) {
                return secondStr;
            }
            return firstStr;
        }
    }

    private static String compareIntegers(String firstStr, String secondStr, boolean ascSortMode) {
        int number1 = Integer.parseInt(firstStr);
        int number2 = Integer.parseInt(secondStr);

        if (number1 >= number2) {
            if (!ascSortMode) {
                return firstStr;
            }
            return secondStr;
        } else {
            if (!ascSortMode) {
                return secondStr;
            }
            return firstStr;
        }
    }
}