package com.company.controller;

import com.company.container.ComponentContainer;
import com.company.entity.*;
import com.company.enums.AdminStatus;
import com.company.files.DbFunctionsImpl;
import com.company.files.WorkWithDbFunctions;
import com.company.files.WorkWithFiles;
import com.company.util.InlineButtonConstants;
import com.company.util.KeyboardButtonConstants;
import com.company.util.KeyboardButtonUtil;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.*;

import java.io.File;

import static com.company.container.ComponentContainer.BASE_FOLDER;
import static com.company.container.ComponentContainer.adminStatusMap;


public class AdminController {


    static WorkWithDbFunctions object = new DbFunctionsImpl();

    static AdminStatus status = AdminStatus.NOTHING;
    static String blockingChatId = null;

    public static void handleMessage(User user, Message message) {

        String s = String.valueOf(message.getChatId());

        if (adminStatusMap.get(s).equals(AdminStatus.AD_SEND_1)) {
//        if (status.equals(AdminStatus.AD_SEND_1)) {
            for (Users oneUser : object.getAllUsers()) {
                if(!oneUser.isAdmin()) {
                    ForwardMessage forwardMessage = new ForwardMessage(oneUser.getChatId(),
                            String.valueOf(message.getChatId()), message.getMessageId());
                    ComponentContainer.MY_BOT.sendMsg(forwardMessage);
                }
            }

            adminStatusMap.put(s, AdminStatus.NOTHING);
//            System.out.println(adminStatusMap);
//            status = AdminStatus.NOTHING;
        } else if (message.hasText()) {
            String text = message.getText();
            handleText(user, message, text);
        } else if (message.hasContact()) {
            Contact contact = message.getContact();
            handleContact(user, message, contact);
        }


    }


    private static void handleContact(User user, Message message, Contact contact) {


        String chatId = String.valueOf(message.getChatId());
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        sendMessage.setText("Menu: ");
        sendMessage.setReplyMarkup(KeyboardButtonUtil.getAdminMenu());
        ComponentContainer.MY_BOT.sendMsg(sendMessage);

    }


    private static void handleText(User user, Message message, String text) {
        String chatId = String.valueOf(message.getChatId());
        SendDocument sendDocument = new SendDocument();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);


        if (text.equals("/start")) {
            adminStatusMap.put(chatId, AdminStatus.NOTHING);
//            if(adminStatusMap.get(chatId).equals())
//            System.out.println(adminStatusMap);
            sendMessage.setText("Hello " + user.getFirstName());
            sendMessage.setReplyMarkup(KeyboardButtonUtil.getAdminMenu());
            ComponentContainer.MY_BOT.sendMsg(sendMessage);

        } else if (text.equalsIgnoreCase(KeyboardButtonConstants.USERS_LIST)) {
            WorkWithFiles.getUserFile();
            File file = new File(BASE_FOLDER, "usersList.xlsx");
            sendDocument.setChatId(chatId);
            sendDocument.setDocument(new InputFile(file));
            ComponentContainer.MY_BOT.sendMsg(sendDocument);
            file.delete();

        } else if (text.equalsIgnoreCase(KeyboardButtonConstants.SET_ADMIN)) {
            sendMessage.setText("Enter user's chat id: ");
            ComponentContainer.MY_BOT.sendMsg(sendMessage);
//            status = AdminStatus.SETTING_ADMIN;
            adminStatusMap.put(chatId, AdminStatus.SETTING_ADMIN);

        } else if (text.equalsIgnoreCase(KeyboardButtonConstants.CONFIRM_ADD_FROM_USER)) {
            object.getAdsToCheck(chatId);

        } else if (text.equalsIgnoreCase(KeyboardButtonConstants.BLOCK_USER)) {
            sendMessage.setText("Enter user's chat id: ");
            ComponentContainer.MY_BOT.sendMsg(sendMessage);
            adminStatusMap.put(chatId, AdminStatus.BLOCKING_P1);
//            status = AdminStatus.BLOCKING_P1;
//            System.out.println(adminStatusMap);
        } else if (ComponentContainer.adminAnswerMap.containsKey(chatId)) {

            MessageData messageData = ComponentContainer.adminAnswerMap.get(chatId);

            Integer messageId = messageData.getMessage().getMessageId();
            String messageText = messageData.getMessage().getText();
            Integer customerMessageId = messageData.getMessageId();
            String customerChatId = messageData.getCustomerChatId();


            sendMessage.setChatId(customerChatId);
            sendMessage.setText(text);
            ComponentContainer.MY_BOT.sendMsg(sendMessage);

            EditMessageText editMessageText = new EditMessageText();
            editMessageText.setChatId(chatId);
            String str = "ChatId : " + chatId +
                    "\nFull name: " + messageData.getMessage().getForwardSenderName() +
                    "\nText: " + messageText +
                    "\nAnswered to this message" +
                    "\n---------------------------" +
                    "\nAnswered Admin: " + "@" + user.getUserName() +
                    "\nAnswered Admin's name: " + user.getFirstName() +
                    "\nAnswer: " + text;
            editMessageText.setText(str);
            editMessageText.setMessageId(messageId);
            ComponentContainer.MY_BOT.sendMsg(editMessageText);
            DeleteMessage deleteMessage = new DeleteMessage(chatId, message.getMessageId());
            ComponentContainer.MY_BOT.sendMsg(deleteMessage);
            ComponentContainer.adminAnswerMap.remove(chatId);

//            List<Message> mustMessageList = null;
//            Message mustKey = null;
//
//            for (Message keyMessage : ComponentContainer.messagesMap.keySet()) {
//                if (keyMessage.getMessageId().equals(messageId)) {
//                    mustKey = keyMessage;
//                    mustMessageList = ComponentContainer.messagesMap.get(keyMessage);
//                    break;
//                }
//            }
//
//            for (Message message1 : mustMessageList) {
//                String adminChatId = message1.getChatId().toString();
//                if (adminChatId.equals(message.getChatId().toString())) {
//                    EditMessageText editMessageText1 = new EditMessageText();
//                    editMessageText1.setChatId(adminChatId);
//                    editMessageText1.setText("Admin " + message.getFrom().getFirstName() + " answered for this question");
//                    editMessageText1.setMessageId(message1.getMessageId());
//                    ComponentContainer.MY_BOT.sendMsg(editMessageText1);
//                }
//            }
//
//            ComponentContainer.messagesMap.remove(mustKey);

        }
//        else if (status.equals(AdminStatus.SETTING_ADMIN)) {
        else if (adminStatusMap.get(chatId).equals(AdminStatus.SETTING_ADMIN)) {
            boolean isAdmin = object.setAdmin(text);
            if (isAdmin) {
                sendMessage.setText("New admin added");
                ComponentContainer.MY_BOT.sendMsg(sendMessage);
                sendMessage.setChatId(text);
                sendMessage.setText("You have been appointed as an admin by " + user.getFirstName());
                sendMessage.setReplyMarkup(KeyboardButtonUtil.getAdminMenu());
                ComponentContainer.MY_BOT.sendMsg(sendMessage);
            } else {
                sendMessage.setText("Error occured" +
                        "\n Reasons to error" +
                        "\n 1.Wrong chat id" +
                        "\n 2.Maybe this id owner have not been registered in bot yet");
                ComponentContainer.MY_BOT.sendMsg(sendMessage);
            }
//            status = AdminStatus.NOTHING;
            adminStatusMap.put(chatId, AdminStatus.NOTHING);
        } else if (text.equalsIgnoreCase(KeyboardButtonConstants.SEND_AD_TO_ALL_USERS)) {
            adminStatusMap.put(chatId, AdminStatus.AD_SEND_1);
//            status = AdminStatus.AD_SEND_1;
            sendMessage.setText("Send advertisement");
            sendMessage.setChatId(chatId);
            ComponentContainer.MY_BOT.sendMsg(sendMessage);
//        } else if (status.equals(AdminStatus.BLOCKING_P1)) {
        } else if (adminStatusMap.get(chatId).equals(AdminStatus.BLOCKING_P1)) {
            Users checkIsBlocked = object.getUserByChatId(text);
            if (checkIsBlocked.getId() == 0) {
                sendMessage.setText("No such user");
//                status = AdminStatus.NOTHING;
                adminStatusMap.put(chatId, AdminStatus.NOTHING);
            } else if (checkIsBlocked.isBlocked()) {
                sendMessage.setText("Enter reason to unblock");
//                status = AdminStatus.BLOCKING_P2_UNBLOCK;
                adminStatusMap.put(chatId, AdminStatus.BLOCKING_P2_UNBLOCK);

                blockingChatId = text;
            } else {
                sendMessage.setText("Enter the reason to block.");
//                status = AdminStatus.BLOCKING_P2_BLOCK;
                adminStatusMap.put(chatId, AdminStatus.BLOCKING_P2_BLOCK);

                blockingChatId = text;
            }
            ComponentContainer.MY_BOT.sendMsg(sendMessage);
        } else if (
                adminStatusMap.get(chatId).equals(AdminStatus.BLOCKING_P2_BLOCK) ||
                        adminStatusMap.get(chatId).equals(AdminStatus.BLOCKING_P2_UNBLOCK)
//                status.equals(AdminStatus.BLOCKING_P2_BLOCK)||
//                        status.equals(AdminStatus.BLOCKING_P2_UNBLOCK)
        ) {
            Users checker = object.getUserByChatId(blockingChatId);
            if (checker.getId() == 0) {
                sendMessage.setText("No such user");
                ComponentContainer.MY_BOT.sendMsg(sendMessage);
            } else {
                object.blockUser(blockingChatId);
                if (adminStatusMap.get(chatId).equals(AdminStatus.BLOCKING_P2_BLOCK)) {
//                if (status.equals(AdminStatus.BLOCKING_P2_BLOCK)) {
                    sendMessage.setText(blockingChatId + " this user has been blocked");
                    ComponentContainer.MY_BOT.sendMsg(sendMessage);
                    sendMessage.setChatId(blockingChatId);
                    sendMessage.setText("Your account has been blocked" +
                            "\nReason: " + text);
                    sendMessage.setReplyMarkup(KeyboardButtonUtil.getUserMenu());

                } else if (adminStatusMap.get(chatId).equals(AdminStatus.BLOCKING_P2_UNBLOCK)) {
//                } else if (status.equals(AdminStatus.BLOCKING_P2_UNBLOCK)) {
                    sendMessage.setText(blockingChatId + " user has been unblocked");
                    ComponentContainer.MY_BOT.sendMsg(sendMessage);
                    sendMessage.setChatId(blockingChatId);
                    sendMessage.setText("Your accoun unblocked" +
                            "\nReason unblock: " + text);
                    sendMessage.setReplyMarkup(KeyboardButtonUtil.getUserMenu());
                }

                ComponentContainer.MY_BOT.sendMsg(sendMessage);
            }
//            status = AdminStatus.NOTHING;
            adminStatusMap.put(chatId, AdminStatus.NOTHING);
        } else {
            sendMessage.setText("Something went wrong");
            ComponentContainer.MY_BOT.sendMsg(sendMessage);

        }
    }

    public static void handleCallback(User user, Message message, String data) {
        String chatId = String.valueOf(message.getChatId());
        Integer messageId = message.getMessageId();
        SendMessage sendMessage = new SendMessage();
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(chatId);
        if (data.startsWith(InlineButtonConstants.REPLY_CALL_BACK)) {
            sendMessage.setChatId(chatId);
            String customerChatId = data.split("/")[1];
            System.out.println(customerChatId);
            ComponentContainer.adminAnswerMap.put(chatId, new MessageData(message, customerChatId, messageId));

            sendMessage.setText("Enter your answer: ");
            ComponentContainer.MY_BOT.sendMsg(sendMessage);
        } else if (data.startsWith(InlineButtonConstants.CONFIRM_AD_CALL_BACK)) {
//            deleteMessage.setChatId(chatId);
            deleteMessage.setMessageId(messageId);
            ComponentContainer.MY_BOT.sendMsg(deleteMessage);
            int adsId = Integer.parseInt(data.split("/")[1]);
            String adsUserChatId = data.split("/")[2];
            String adsTitle = data.split("/")[3];
            object.acceptAd(adsId);
            String str = "Your advertisement confirmed✅. Under the name: " + adsTitle  + "\n";
            sendMessage.setChatId(adsUserChatId);

            sendMessage.setText(str);
            ComponentContainer.MY_BOT.sendMsg(sendMessage);

            sendMessage.setChatId(chatId);
            sendMessage.setText("Advertisement list updated");
            ComponentContainer.MY_BOT.sendMsg(sendMessage);

        } else if (data.startsWith(InlineButtonConstants.REJECT_AD_CALL_BACK)) {
//            deleteMessage.setChatId(chatId);
            deleteMessage.setMessageId(messageId);
            ComponentContainer.MY_BOT.sendMsg(deleteMessage);
            int adsId = Integer.parseInt(data.split("/")[1]);
            String adsUserChatId = data.split("/")[2];
            String adsTitle = data.split("/")[3];
            object.rejectAd(adsId);
            String str = "Your advertisement rejected under the name : " + adsTitle + " \n";
            sendMessage.setChatId(adsUserChatId);

            sendMessage.setText(str);
            ComponentContainer.MY_BOT.sendMsg(sendMessage);

            sendMessage.setChatId(chatId);
            sendMessage.setText("Operation succeed");
            ComponentContainer.MY_BOT.sendMsg(sendMessage);

        }
    }

}

