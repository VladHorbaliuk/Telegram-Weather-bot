import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Bot extends TelegramLongPollingBot {

    public static String city = "";
    String url = "https://api.openweathermap.org/data/2.5/weather?";
    final String key = "54a56e6dd61d962d8e01b2ac4feb413a";

    public void onUpdateReceived(Update update) {
        Message msg = update.getMessage();
        String txt = msg.getText();
        if (txt.equals("/start")) {
            sendMessage(msg, "Hello user, i`m Telegram weather bot \nInput sity name:");
        } else {
            city = txt;
            try {
                writeFile(createConnection(createURL(url, key, city)));
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                sendMessage(msg, parseJSON());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendMessage(Message msg, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(msg.getChatId());
        sendMessage.setText(text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public String getBotUsername() {
        return "My_telegram_weather_bot";
    }

    public String getBotToken() {
        return "1129987255:AAFxsfw605jpuyilLybPxmxSpvxLo6kv0TA";
    }

    public static void main(String[] args) throws Exception {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(new Bot());
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
    }

    static String createURL(String url, String key, String city) {
        Scanner scanner = new Scanner(System.in);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("q", city);
        map.put("appid", key);
        map.put("units", "metric");
        map.put("lang", "ru");
        for (Map.Entry e : map.entrySet()) {
            url += e.getKey() + "=" + e.getValue() + "&";
        }
        return url;
    }

    static String createConnection(String url) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
        connection.setRequestMethod("GET");
        Scanner fileScanner = new Scanner(connection.getInputStream());
        String response = "";
        while (fileScanner.hasNextLine()) {
            response += fileScanner.nextLine();
        }
        return response;
    }

    static void writeFile(String response) throws Exception {
        FileWriter fileWriter = new FileWriter("data.json", false);
        fileWriter.write(response);
        fileWriter.close();
    }

    static String parseJSON() throws Exception {
        FileReader fileReader = new FileReader("data.json");
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(fileReader);

        String output = "";

        JSONObject main = (JSONObject) jsonObject.get("main");
        JSONObject sys = (JSONObject) jsonObject.get("sys");

        output += "Страна: " + sys.get("country") + "\n";
        output += "Город: " + jsonObject.get("name") + "\n";
        output += "temp: " + main.get("temp") + "°С" + "\n";
        output += "min temp: " + main.get("temp_min") + "°С" + "\n";
        output += "max temp: " + main.get("temp_max") + "°С" + "\n";

        return output;
    }
}
