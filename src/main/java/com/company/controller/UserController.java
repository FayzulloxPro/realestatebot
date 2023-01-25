package com.company.controller;


import com.company.container.ComponentContainer;
import com.company.entity.*;
import com.company.enums.State;
import com.company.files.DbFunctionsImpl;
import com.company.files.WorkWithDbFunctions;
import com.company.util.InlineKeyboardUtil;
import com.company.util.KeyboardButtonConstants;
import com.company.util.KeyboardButtonUtil;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;

import java.math.BigDecimal;
import java.util.*;

public class UserController {
    static Map<String, State> userStatus = new HashMap<>();
    static Map<String, Ads> userAds = new HashMap<>();

    // javohir
    static Map<String, Ads> searchAds = new HashMap<>();
    static Map<String, Region> searchRegion = new HashMap<>();


    static Map<String, String> forRegionId = new HashMap<>();

    static Map<String, Parameter> userAdParameters = new HashMap<>();

    static WorkWithDbFunctions object = new DbFunctionsImpl();

    static Map<String, SearchHelper> userSearch = new HashMap<>();
    static Map<String, SearchPrice> searchPriceMap = new HashMap<>();

    public static void handleMessage(User user, Message message) {

        if (message.hasText()) {
            String text = message.getText();
            handleText(user, message, text);
        } else if (message.hasContact()) {
            Contact contact = message.getContact();
            handleContact(user, message, contact);
        } else if (message.hasPhoto()) {
            List<PhotoSize> photoSizeList = message.getPhoto();
            handlePhoto(user, message, photoSizeList);
        }
    }

    private static void handleContact(User user, Message message, Contact contact) {

//        if (!contact.getPhoneNumber().matches("(\\+)?998\\d{9}")) return;

        String chatId = String.valueOf(message.getChatId());
        Users customer = object.getUserByChatId(chatId);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        if (customer.getId() == 0) {
            customer = object.addUser(chatId, contact, user.getUserName());
            sendMessage.setText("Your membershio confirmed" +
                    "\nEnjoy from using this bot");
            sendMessage.setReplyMarkup(KeyboardButtonUtil.getUserMenu());

        } else {
            sendMessage.setText("Menu: ");
            sendMessage.setReplyMarkup(KeyboardButtonUtil.getUserMenu());
        }
        ComponentContainer.MY_BOT.sendMsg(sendMessage);
    }

    private static void handleText(User user, Message message, String text) {
        String chatId = String.valueOf(message.getChatId());
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        Users customer = object.getUserByChatId(chatId);
        Ads s_ads = searchAds.get(chatId);

        State state1 = userStatus.get(chatId);

        if (text.equals("/start")) {
            if (customer.getId() == 0) {
                sendMessage.setText("Welcome!");
                sendMessage.setReplyMarkup(KeyboardButtonUtil.getContactMenu());
                ComponentContainer.MY_BOT.sendMsg(sendMessage);
            } else {
                if (customer.isBlocked()) {
                    sendMessage.setText("Your account blocked");
                    sendMessage.setReplyMarkup(KeyboardButtonUtil.BlockedUserMenu());
                    ComponentContainer.MY_BOT.sendMsg(sendMessage);
                } else {
                    sendMessage.setText("Menu");
                    sendMessage.setReplyMarkup(KeyboardButtonUtil.getUserMenu());
                    ComponentContainer.MY_BOT.sendMsg(sendMessage);
                }
            }

        } else if (text.equals(KeyboardButtonConstants.CONTACT_WITH_ADMIN)) {

            ComponentContainer.customerMap.put(chatId, true);
            sendMessage.setChatId(chatId);
            sendMessage.setText("Contact admin \uD83D\uDCAC ");
            ComponentContainer.MY_BOT.sendMsg(sendMessage);
        } else if (ComponentContainer.customerMap.containsKey(chatId)) {

            ComponentContainer.customerMap.remove(chatId);

            sendMessage.setText("Your message has been sent to admin! 😊");
            ComponentContainer.MY_BOT.sendMsg(sendMessage);

            String str = "ChatId : " + customer.getChatId() + "\nFull name: " + customer.getFirstName() +
                    "\nPhone number: " + customer.getPhoneNumber() +
                    "\nText: " + text;
            SendMessage sendMessage1 = new SendMessage();
            for (String adminChatId : object.getAdminsChatIds()) {

                sendMessage1.setText(str);
                sendMessage1.setChatId(adminChatId);
                sendMessage1.setReplyMarkup(InlineKeyboardUtil.getConnectMarkup(chatId, message.getMessageId()));
                ComponentContainer.MY_BOT.sendMsg(sendMessage1);
                break;
            }
        } else if (customer.isBlocked()) {
            sendMessage.setText("Your account blocked");
            sendMessage.setReplyMarkup(KeyboardButtonUtil.BlockedUserMenu());
            ComponentContainer.MY_BOT.sendMsg(sendMessage);

        }
        else if (text.equals(KeyboardButtonConstants.MY_ADS)) {
            int adsEmpty = object.isAdsEmpty(chatId);
            if (adsEmpty > 0) {
                object.getMyAdsHistory(chatId);
            } else {
                sendMessage.setText("You have no advertisements yet");
                ComponentContainer.MY_BOT.sendMsg(sendMessage);
            }
        } else if (text.equals(KeyboardButtonConstants.SEND_AD_TO_ADMIN)) {
            sendMessage.setText("Post advertisement\n" + "Send title: ");
            userAds.put(chatId, new Ads());
            userStatus.put(chatId, State.GET_TITLE);
            ComponentContainer.MY_BOT.sendMsg(sendMessage);


        }else if (text.equals(KeyboardButtonConstants.SEARCH)) {

            sendMessage.setText("Search menu");
            sendMessage.setReplyMarkup(KeyboardButtonUtil.getSearchMenu());
            ComponentContainer.MY_BOT.sendMsg(sendMessage);
        } else if (text.equals(KeyboardButtonConstants.BACK_MENU)) {

            sendMessage.setText("Main menu");
            sendMessage.setReplyMarkup(KeyboardButtonUtil.getUserMenu());
            ComponentContainer.MY_BOT.sendMsg(sendMessage);
        } else if (text.equals(KeyboardButtonConstants.S_BY_ADDRES)) {
            sendMessage.setText("What is category of your search? ");
            searchAds.put(chatId, new Ads());
            searchRegion.put(chatId, new Region());
            userStatus.put(chatId, State.SEARCH_GET_SALE_TYPE_ID);
            sendMessage.setReplyMarkup(InlineKeyboardUtil.getSaleTypeId());
            ComponentContainer.MY_BOT.sendMsg(sendMessage);
        } else if (text.equals(KeyboardButtonConstants.S_BY_COUNT_HOME)) {
            userSearch.put(chatId, new SearchHelper());

            sendMessage.setText("Enter the number of rooms");
            userStatus.put(chatId, State.S_ENTER_COUNT_ROOM);
            ComponentContainer.MY_BOT.sendMsg(sendMessage);
        } else if (text.equals(KeyboardButtonConstants.S_BY_PRICE)) {

            searchPriceMap.put(chatId, new SearchPrice());
            sendMessage.setText("Send interval to search by cost like given below\n 200-300");
            ComponentContainer.MY_BOT.sendMsg(sendMessage);
            userStatus.put(chatId, State.GET_PRICE);
        } else if (text.equals(KeyboardButtonConstants.S_BY_HOME_DATE)) {
            sendMessage.setText("Choose time interval to search📄 ");
            sendMessage.setReplyMarkup(InlineKeyboardUtil.getSearchDateAds());
            userStatus.put(chatId, State.S_DATE);
            ComponentContainer.MY_BOT.sendMsg(sendMessage);

        } else if (state1.equals(State.S_ENTER_COUNT_ROOM)) {
            try {
                int count = Integer.parseInt(text);
                if (count < 1) {
                    sendMessage.setText("Wrong number of rooms try again");
                    ComponentContainer.MY_BOT.sendMsg(sendMessage);
                } else {
                    SearchHelper searchHelper = userSearch.get(chatId);
                    searchHelper.setNumberOfRooms(count);
                    sendMessage.setText("Number of rooms accepted ");
                    ComponentContainer.MY_BOT.sendMsg(sendMessage);
                    sendMessage.setText("In which region you are searching for");
                    sendMessage.setReplyMarkup(InlineKeyboardUtil.getRegions());
                    ComponentContainer.MY_BOT.sendMsg(sendMessage);
                    userStatus.put(chatId, State.SEARCH_REGION);
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendMessage.setText("Wrong number of rooms try again");
                ComponentContainer.MY_BOT.sendMsg(sendMessage);
            }
        }else {
            State state = userStatus.get(chatId);
            Ads ads = userAds.get(chatId);
            if (state.equals(State.GET_TITLE)) {

                ads.setTitle(text);
                userStatus.put(chatId, State.GET_HOME_TYPE_ID);

                sendMessage.setText("Choose building type: ");
                sendMessage.setReplyMarkup(InlineKeyboardUtil.getHomeTypes());
                ComponentContainer.MY_BOT.sendMsg(sendMessage);

            } else if (state.equals(State.GET_PHONE_NUMBER)) {
                if (isValidNumber(text)) {
                    ads.setPhoneNumber(text);
                    userStatus.put(chatId, State.GET_SALE_TYPE_ID);
                    sendMessage.setText("Phone number accepted");
                    ComponentContainer.MY_BOT.sendMsg(sendMessage);

                    sendMessage.setText("In what category you are gonna post advertisement: ");
                    sendMessage.setChatId(chatId);
                    sendMessage.setReplyMarkup(InlineKeyboardUtil.getSaleTypeId());

                    ComponentContainer.MY_BOT.sendMsg(sendMessage);

                } else {
                    sendMessage.setChatId(chatId);
                    sendMessage.setText("Wrong phone number.\n" +
                            "resend phone number.");
                    ComponentContainer.MY_BOT.sendMsg(sendMessage);
                }
            } else if (state.equals(State.ROOM_COUNT)) {

                int numberOfRooms = Integer.parseInt(text);

                userAdParameters.put(chatId, new Parameter());

                sendMessage.setChatId(chatId);

                if (numberOfRooms < 1 || numberOfRooms > 100) {
                    sendMessage.setText("Wrong number of rooms try again ");
                } else {
                    Parameter parameter = userAdParameters.get(chatId);
                    parameter.setRoomCount(numberOfRooms);
                    sendMessage.setText("Number of rooms accepted\n" +
                            "Enter the area of building(m square): ");
                    userStatus.put(chatId, State.AREA);
                }
                ComponentContainer.MY_BOT.sendMsg(sendMessage);
            } else if (state.equals(State.AREA)) {
                int area = Integer.parseInt(text);
                sendMessage.setChatId(chatId);
                if (area < 0) {
                    sendMessage.setText("Area is wrong.\n" +
                            "Try again: ");
                } else {
                    userStatus.put(chatId, State.FLOOR);
                    Parameter parameter = userAdParameters.get(chatId);
                    parameter.setArea(area);

                    if (ads.getHomeTypeId() == 1) {
                        sendMessage.setText("Enter the floor of house and enter tho total number floors in the building Example: \"<b>2/4</b>\"");
                        sendMessage.setParseMode(ParseMode.HTML);
                    } else {
                        sendMessage.setText("How many floor does building have?:");
                    }
                }
                ComponentContainer.MY_BOT.sendMsg(sendMessage);
            } else if (state.equals(State.FLOOR)) {
                if (ads.getHomeTypeId() == 1) {
                    String[] split = text.split("/");

                    try {
                        int floor = Integer.parseInt(split[0]);
                        int maxFloor = Integer.parseInt(split[1]);

                        sendMessage.setChatId(chatId);

                        if (floor < 0 || maxFloor < 0 || floor > maxFloor) {
                            sendMessage.setText("Information is wrong.\nEnter the floor of house and enter tho total " +
                                    "number floors in the building Example: \\\"<b>2/4</b>\\\"\"");
                            sendMessage.setParseMode(ParseMode.HTML);
                        } else {
                            Parameter parameter = userAdParameters.get(chatId);
                            parameter.setFloor(floor);
                            parameter.setMaterialId(maxFloor);
                            sendMessage.setText("Information accepted.\n" +
                                    "What is the material of the house?");
                            sendMessage.setReplyMarkup(InlineKeyboardUtil.getMaterial());
                            userStatus.put(chatId, State.MATERIAL_ID);

                        }
                        ComponentContainer.MY_BOT.sendMsg(sendMessage);

                    } catch (Exception e) {
                        sendMessage.setChatId(""); // you can put your telegram chat id to send exception to your chat
                        sendMessage.setText(String.valueOf(e));
                        ComponentContainer.MY_BOT.sendMsg(sendMessage);
                        sendMessage.setChatId(chatId);
                        sendMessage.setText("Information is wrong.\nEnter the floor of house and enter tho total " +
                                "number floors in the building Example: \\\"<b>2/4</b>\\\"\"");
                        sendMessage.setParseMode(ParseMode.HTML);
                    }
                } else {
                    sendMessage.setChatId(chatId);
                    try {
                        int floor = Integer.parseInt(text);
                        Parameter parameter = userAdParameters.get(chatId);
                        parameter.setFloor(floor);
                        sendMessage.setText("Number of floors accepted.\n" +
                                "What is the material of the house?");
                        userStatus.put(chatId, State.MATERIAL_ID);
                        sendMessage.setReplyMarkup(InlineKeyboardUtil.getMaterial());
                    } catch (Exception e) {
                        sendMessage.setText("Wrong number of floors.\nTry again: ");
                    }
                    ComponentContainer.MY_BOT.sendMsg(sendMessage);
                }
            } else if (state.equals(State.PRICE)) {
                sendMessage.setChatId(chatId);
                try {

                    double price = Double.parseDouble(text);
                    if (price < 0) {
                        sendMessage.setText("Cost is wrong. Try again:");
                    } else {
                        ads.setPrice(new BigDecimal(price));
                        sendMessage.setText("Cost accepted");
                        ComponentContainer.MY_BOT.sendMsg(sendMessage);
                        sendMessage.setText("Enter the number of rooms: ");
                        userStatus.put(chatId, State.ROOM_COUNT);
                        ComponentContainer.MY_BOT.sendMsg(sendMessage);
                    }

                } catch (Exception e) {
                    sendMessage.setText("Cost is wrong. Try again");
                }
            } else if (state.equals(State.INFO)) {


                Parameter parameter = userAdParameters.get(chatId);
                ads.setInfo(text);

                userStatus.put(chatId, State.GET_PHOTO);
                sendMessage.setText("Information accepted.\n" +
                        "Send the photo of the building(house): ");
                sendMessage.setChatId(chatId);
                ComponentContainer.MY_BOT.sendMsg(sendMessage);
            } else if (state.equals(State.GET_PRICE)) {
                try {
                    String[] split = text.split("-");
                    int price1 = Integer.parseInt(split[0]);
                    int price2 = Integer.parseInt(split[1]);
                    if (price1 < 0 || price2 < 0 || price1 > price2) {
                        sendMessage.setText("Cost is wrong. Try again");
                        ComponentContainer.MY_BOT.sendMsg(sendMessage);
                    } else {
                        SearchPrice searchPrice = searchPriceMap.get(chatId);
                        searchPrice.setPrice1(price1);
                        searchPrice.setPrice2(price2);
                        sendMessage.setText("Cost interval accepted ");
                        ComponentContainer.MY_BOT.sendMsg(sendMessage);
                        sendMessage.setText("In which region you are searching for");
                        sendMessage.setReplyMarkup(InlineKeyboardUtil.getRegions());
                        ComponentContainer.MY_BOT.sendMsg(sendMessage);
                        userStatus.put(chatId, State.SEARCH_REGION_BY_PRICE);  // todo
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    sendMessage.setText("Cost is wrong. Try again");
                    ComponentContainer.MY_BOT.sendMsg(sendMessage);
                }
            } else {
                sendMessage.setText("Wrong operation. Try again ♻️");
                ComponentContainer.MY_BOT.sendMsg(sendMessage);
            }
        }
    }


    public static boolean isValidNumber(String number) {
        return number.matches("[+]998[0-9]{9}");
    }


    public static void handleCallback(User user, Message message, String data) {
        String chatId = String.valueOf(message.getChatId());

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        State state = userStatus.get(chatId);
        Ads ads = userAds.get(chatId);
        Ads s_ads = searchAds.get(chatId);
        Region s_region = searchRegion.get(chatId);


        DeleteMessage deleteMessage = new DeleteMessage();

        if (data.endsWith("/editAd")) {
            deleteMessage.setChatId(chatId);
            deleteMessage.setMessageId(message.getMessageId());
            ComponentContainer.MY_BOT.sendMsg(deleteMessage);
            String[] split = data.split("/");
            int adsId = Integer.parseInt(split[0]);

        }

        if (state.equals(State.GET_SALE_TYPE_ID)) {
            userStatus.put(chatId, State.GET_REGION_ID);


            deleteMessage.setChatId(chatId);
            deleteMessage.setMessageId(message.getMessageId());
            ComponentContainer.MY_BOT.sendMsg(deleteMessage);

            sendMessage.setText("Type has been chosen");
            ComponentContainer.MY_BOT.sendMsg(sendMessage);

            ads.setSaleTypeId(Integer.parseInt(data));
            sendMessage.setChatId(chatId);
            sendMessage.setText("Choose the region: ");
            sendMessage.setReplyMarkup(InlineKeyboardUtil.getRegions());
            ComponentContainer.MY_BOT.sendMsg(sendMessage);

        } else if (state.equals(State.GET_REGION_ID)) {
            forRegionId.put(chatId, data);
            userStatus.put(chatId, State.GET_DISTRICT_ID);

            sendMessage.setText("Region chosen.");
            ComponentContainer.MY_BOT.sendMsg(sendMessage);
            deleteMessage.setChatId(chatId);
            deleteMessage.setMessageId(message.getMessageId());
            ComponentContainer.MY_BOT.sendMsg(deleteMessage);

            sendMessage.setChatId(chatId);
            sendMessage.setText("Choose the district: ");
            sendMessage.setReplyMarkup(InlineKeyboardUtil.getDistrictByRegionId(data));
            ComponentContainer.MY_BOT.sendMsg(sendMessage);

        } else if (state.equals(State.GET_DISTRICT_ID)) {

            deleteMessage.setChatId(chatId);
            deleteMessage.setMessageId(message.getMessageId());
            ComponentContainer.MY_BOT.sendMsg(deleteMessage);

            ads.setDistrictId(Integer.parseInt(data));
            District district = DbFunctionsImpl.getDistrictById(data);
            sendMessage.setChatId(chatId);

            Region region = DbFunctionsImpl.getRegionById(String.valueOf(district.getRegionId()));

            sendMessage.setText(region.getName() + ", " + district.getName() + " chosen district.");
            ComponentContainer.MY_BOT.sendMsg(sendMessage);

            userStatus.put(chatId, State.PRICE_TYPE_ID);
            sendMessage.setText("Choose currency type to enter the cost?");
            sendMessage.setReplyMarkup(InlineKeyboardUtil.getCurrencies());
            ComponentContainer.MY_BOT.sendMsg(sendMessage);

        } else if (state.equals(State.PRICE_TYPE_ID)) {
            deleteMessage.setChatId(chatId);
            deleteMessage.setMessageId(message.getMessageId());
            ComponentContainer.MY_BOT.sendMsg(deleteMessage);

            sendMessage.setText("Currency chosen");
            ComponentContainer.MY_BOT.sendMsg(sendMessage);

            userStatus.put(chatId, State.PRICE);
            ads.setPriceTypeId(Integer.parseInt(data));

            sendMessage.setText("Enter the cost: ");
            ComponentContainer.MY_BOT.sendMsg(sendMessage);
        } else if (state.equals(State.GET_HOME_TYPE_ID)) {
            deleteMessage.setChatId(chatId);
            deleteMessage.setMessageId(message.getMessageId());
            ComponentContainer.MY_BOT.sendMsg(deleteMessage);

            ads.setHomeTypeId(Integer.parseInt(data));

            sendMessage.setText("Building type has been choosen.");
            ComponentContainer.MY_BOT.sendMsg(sendMessage);

            userStatus.put(chatId, State.GET_PHONE_NUMBER);
            sendMessage.setText("Enter the phone number.\n" +
                    "Example: +998991234567");
            sendMessage.setChatId(chatId);

            ComponentContainer.MY_BOT.sendMsg(sendMessage);

        } else if (state.equals(State.MATERIAL_ID)) {
            deleteMessage.setChatId(chatId);
            deleteMessage.setMessageId(message.getMessageId());
            ComponentContainer.MY_BOT.sendMsg(deleteMessage);

            Parameter parameter = userAdParameters.get(chatId);
            parameter.setMaterialId(Integer.parseInt(data));

            userStatus.put(chatId, State.INFO);
            sendMessage.setText("Enter the additional information: ");
            sendMessage.setChatId(chatId);
            ComponentContainer.MY_BOT.sendMsg(sendMessage);
        }

        // Javohir


        else if (state.equals(State.SEARCH_GET_SALE_TYPE_ID)) {
            deleteMessage.setChatId(chatId);
            deleteMessage.setMessageId(message.getMessageId());
            ComponentContainer.MY_BOT.sendMsg(deleteMessage);

            s_ads.setSaleTypeId(Integer.parseInt(data));
            sendMessage.setChatId(chatId);
            sendMessage.setText("Type chosen.\n" +
                    "Choose region:");
            userStatus.put(chatId, State.SEARCH_GET_REGION_ID);
            sendMessage.setReplyMarkup(InlineKeyboardUtil.getRegions());
            ComponentContainer.MY_BOT.sendMsg(sendMessage);

        } else if (state.equals(State.SEARCH_GET_REGION_ID)) {

            userStatus.put(chatId, State.SEARCH_GET_DISTRICT_ID);


            deleteMessage.setChatId(chatId);
            deleteMessage.setMessageId(message.getMessageId());
            ComponentContainer.MY_BOT.sendMsg(deleteMessage);

            s_region.setId(Integer.parseInt(data));

            sendMessage.setChatId(chatId);
            sendMessage.setText("District chosen: ");


            sendMessage.setReplyMarkup(InlineKeyboardUtil.getDistrictByRegionId(data));
            ComponentContainer.MY_BOT.sendMsg(sendMessage);


        } else if (state.equals(State.SEARCH_GET_DISTRICT_ID)) {

            userStatus.put(chatId, State.SEARCH_GET_MENU);

            deleteMessage.setChatId(chatId);
            deleteMessage.setMessageId(message.getMessageId());
            ComponentContainer.MY_BOT.sendMsg(deleteMessage);

            s_ads.setDistrictId(Integer.parseInt(data));
            s_ads.setUserId(Long.parseLong(chatId));
            DbFunctionsImpl db = new DbFunctionsImpl();
            db.getMyAdsSearch(s_ads);
            sendMessage.setChatId(chatId);
            ComponentContainer.MY_BOT.sendMsg(sendMessage);

        } else if (state.equals(State.SEARCH_REGION)) {
            deleteMessage.setChatId(chatId);
            deleteMessage.setMessageId(message.getMessageId());
            ComponentContainer.MY_BOT.sendMsg(deleteMessage);

            SearchHelper searchHelper = userSearch.get(chatId);
            searchHelper.setRegionId(Integer.valueOf(data));
            sendMessage.setText("Region chosen");
            ComponentContainer.MY_BOT.sendMsg(sendMessage);
            sendMessage.setText("Choose district: ");
            sendMessage.setReplyMarkup(InlineKeyboardUtil.getDistrictByRegionId(data));
            ComponentContainer.MY_BOT.sendMsg(sendMessage);
            userStatus.put(chatId, State.SEARCH_DISTRICT);

        } else if (state.equals(State.SEARCH_DISTRICT)) {
            deleteMessage.setChatId(chatId);
            deleteMessage.setMessageId(message.getMessageId());
            ComponentContainer.MY_BOT.sendMsg(deleteMessage);

            SearchHelper searchHelper = userSearch.get(chatId);
            searchHelper.setDistrictId(Integer.valueOf(data));

            DbFunctionsImpl.getSearchResultsByNumberOfRooms(searchHelper, chatId);
            userSearch.remove(chatId);
        } else if (state.equals(State.SEARCH_REGION_BY_PRICE)) {
            deleteMessage.setChatId(chatId);
            deleteMessage.setMessageId(message.getMessageId());
            ComponentContainer.MY_BOT.sendMsg(deleteMessage);

            SearchPrice searchPrice = searchPriceMap.get(chatId);
            searchPrice.setRegionId(Integer.parseInt(data));
            sendMessage.setText("Region chosen");
            ComponentContainer.MY_BOT.sendMsg(sendMessage);
            sendMessage.setText("Choose district");
            sendMessage.setReplyMarkup(InlineKeyboardUtil.getDistrictByRegionId(data));
            ComponentContainer.MY_BOT.sendMsg(sendMessage);
            userStatus.put(chatId, State.SEARCH_DISTRICT_BY_PRICE);

        } else if (state.equals(State.SEARCH_DISTRICT_BY_PRICE)) {
            deleteMessage.setChatId(chatId);
            deleteMessage.setMessageId(message.getMessageId());
            ComponentContainer.MY_BOT.sendMsg(deleteMessage);

            SearchPrice searchPrice = searchPriceMap.get(chatId);
            searchPrice.setDistrictId(Integer.parseInt(data));

            DbFunctionsImpl.getSearchByPrice(searchPrice, chatId);
            searchPriceMap.remove(chatId);
        } else if (state.equals(State.S_DATE)) {
            deleteMessage.setChatId(chatId);
            deleteMessage.setMessageId(message.getMessageId());
            ComponentContainer.MY_BOT.sendMsg(deleteMessage);

            sendMessage.setChatId(chatId);
            System.out.println(data);
            sendMessage.setText("Search results: ");
            DbFunctionsImpl.searchAdsByDate(data, chatId);
            ComponentContainer.MY_BOT.sendMsg(sendMessage);
        } else if (state.equals(State.CONFIRMATION_STATE)) {
            deleteMessage.setChatId(chatId);
            deleteMessage.setMessageId(message.getMessageId());
            ComponentContainer.MY_BOT.sendMsg(deleteMessage);
            if (data.equals("confirm")) {
                Ads ads1 = userAds.get(chatId);
                Parameter parameter = userAdParameters.get(chatId);

                DbFunctionsImpl.addNewAd(ads1, chatId, parameter);

                sendMessage.setText("Ad has been sent to admin✅" +
                        "\nYou will be able to find the ad in the search section after the admin approves it❗️");

            } else if (data.equals("delete")) {
                userStatus.remove(chatId);
                sendMessage.setText("The ad has been cancelled");
            }
            ComponentContainer.MY_BOT.sendMsg(sendMessage);
        } else {
            sendMessage.setText("The wrong action was selected. Please try again ♻️");
            ComponentContainer.MY_BOT.sendMsg(sendMessage);
        }

    }

    private static void handlePhoto(User user, Message message, List<PhotoSize> photoSizeList) {
        String chatId = String.valueOf(message.getChatId());

        String fileId = photoSizeList.get(photoSizeList.size() - 1).getFileId();

        State state = userStatus.get(chatId);

        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        if (state.equals(State.GET_PHOTO)) {
            Ads ads = userAds.get(chatId);

            if (ads.getInfo() == null) {
                ads.setInfo("Ad");
            }

            Parameter parameter = userAdParameters.get(chatId);

            ads.setPhotoPath(fileId);
            userStatus.put(chatId, State.CONFIRMATION_STATE);
            sendPhoto.setPhoto(new InputFile(ads.getPhotoPath()));
            String priceName = "";
            switch (ads.getPriceTypeId()) {
                case 1:
                    priceName += "summ";
                    break;
                case 2:
                    priceName += "dollar";
                    break;
                case 3:
                    priceName += "auro";
                    break;
                default:
                    priceName += "sum";
                    break;
            }
            String home = "";
            switch (ads.getSaleTypeId()) {
                case 1:
                    home += "for sale";
                    break;
                case 2:
                    home += "for rent";
                    break;
                case 3:
                    home += "new buildings";
                    break;
                default:
                    home += "courtyard";
                    break;
            }


            String caption = "<b>" + ads.getTitle() + "</b>\n" +
                    "\uD83C\uDFEC Building type: " + DbFunctionsImpl.getHomeTypeById(ads.getHomeTypeId())
                    .getName() + "\n" +
                    "\uD83C\uDFE2 Building " + home + "\n" +
                    "Number of rooms: " + parameter.getRoomCount() + "\n" +
                    "Number of floors: " + parameter.getFloor() + "\n";

            if (parameter.getMaxFloor() != null) {
                caption += "Total number of floors: " + parameter.getMaxFloor() + "\n";
            }
            caption += "\uD83D\uDCB8Cost: " + ads.getPrice() + " " + priceName + "\n" +
                    "\uD83D\uDCCDAddress: " + "" +
                    DbFunctionsImpl.getRegionById(String.valueOf(DbFunctionsImpl.getDistrictById(String.valueOf(ads.getDistrictId())).
                            getRegionId())).getName() +
                    ", " + DbFunctionsImpl.getDistrictById(String.valueOf(ads.getDistrictId())).getName() + " district\n" +
                    "☎️Contact: " + ads.getPhoneNumber() + "\n" +
                    "\uD83D\uDCD1Additional: " + ads.getInfo() + "\n\n\n" +
                    "Do you confirm? ";

            sendPhoto.setCaption(caption);
            sendPhoto.setParseMode(ParseMode.HTML);
            sendPhoto.setReplyMarkup(InlineKeyboardUtil.confirmOrDelete());

            ComponentContainer.MY_BOT.sendMsg(sendPhoto);
        }
    }
}


