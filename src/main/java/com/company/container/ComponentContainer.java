package com.company.container;


import com.company.Bot.RealEstateBot;
import com.company.entity.MessageData;
import com.company.enums.AdminStatus;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComponentContainer {
    public static RealEstateBot MY_BOT = null;

    public static String BOT_USERNAME = "";
    public static String BOT_TOKEN = "";
    public static final String USER_DB = "";
    public static final String PASSWORD_DB = "";
    public static final String URL_DB = "";
    public static final String DATABASE = "";
    public static final String HOST = "";
    public static final String BASE_FOLDER = "src/main/java/com/company/allFiles";

    public static Map<String, AdminStatus> adminStatusMap = new HashMap<>();
    {
        adminStatusMap.put("1223201050",AdminStatus.NOTHING);
    }

    public static Map<String, Boolean> customerMap = new HashMap<>();
    public static Map<String, MessageData> adminAnswerMap = new HashMap<>();
//    public static Map<Message,List<Message>> messagesMap = new HashMap<>();


}
