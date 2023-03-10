package com.company.files;

import com.company.container.ComponentContainer;
import com.company.entity.*;
import com.company.util.InlineKeyboardUtil;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static com.company.container.ComponentContainer.*;

public class DbFunctionsImpl implements WorkWithDbFunctions {


    public static void getSearchByPrice(SearchPrice searchPrice, String chatId) {
        try (Connection connection = DriverManager.getConnection(URL_DB, USER_DB, PASSWORD_DB)) {
            String query = """
                    select ads.photo_path, ht.name, ads.updated_at as "updated time",
                           ads.phone_number, st.name as "sale name", d.name as "district name",reg.name, par.room_count as "room count", par.area, par.floor,par.max_floor,m.name, ads.price "price",
                           cur.name "currency type",ads.info "description", ads.id, u.chat_id, ads.title from ads join users u
                                                                                                                       on ads.user_id = u.id join sale_type st on ads.sale_type_id = st.id join district d on ads.district_id = d.id join region reg on reg.id = d.region_id
                                                                                                                  join parametr par on  ads.parameter_id = par.id join currency cur on ads.price_type_id = cur.id join home_type ht on
                            ads.home_type_id=ht.id join ads_status stat on ads.status_id = stat.id join material m on par.material_id= m.id where (ads.price between ? AND ? ) AND   ads.district_id= ? AND ads.status_id=3 order by ads.updated_at;
                    """;

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setDouble(1, searchPrice.getPrice1());
            preparedStatement.setDouble(2, searchPrice.getPrice2());
            preparedStatement.setInt(3, searchPrice.getDistrictId());

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String photoPath = resultSet.getString(1);
                String str = "???? Bino turi:" + resultSet.getString(2) + "\n" +
                        "???? Telefon: " + resultSet.getString(4) + "\n" +
                        "???? E'lon turi: " + resultSet.getString(5) + "\n" +
                        "???? Viloyat: " + resultSet.getString(7) + "\n" +
                        "???? Tuman: " + resultSet.getString(6) + "\n" +
                        "???? Maydon: " + resultSet.getInt(9) + "\n" +
                        "\uD83D\uDD22 Xonalar soni: " + resultSet.getInt(8) + "\n";
                if (resultSet.getString(4).equals("Kvartira")) {
                    str += "???? Qavat: " + resultSet.getInt(10) + "/" + resultSet.getInt(11) + "\n";
                } else {
                    str += "???? Qavat: " + resultSet.getInt(11) + "\n";
                }
                str += "??????? Material: " + resultSet.getString(12) + "\n" +
                        "???? Narxi: " + resultSet.getInt(13) + " ";
                if (resultSet.getString(14).equals("dollar")) {
                    str += "$";
                }
                str += "\n\n" +
                        "\uD83D\uDCAC  Qo'shimcha ma'lumot: " + resultSet.getString(15) + "\n"
                        + "\uD83D\uDCC5  Sana: " + resultSet.getString(3) + "\n";
                str += "\n" +
                        "\uD83D\uDD0A  E'lon holati: " + resultSet.getString(16);


                SendPhoto sendPhoto = new SendPhoto();
                sendPhoto.setChatId(chatId);
                sendPhoto.setPhoto(new InputFile(photoPath));
                sendPhoto.setCaption("\n" + str);
                ComponentContainer.MY_BOT.sendMsg(sendPhoto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void searchAdsByDate(String data,String chatId) {
        boolean flag = false;
        try (Connection connection = DriverManager.getConnection(URL_DB, USER_DB, PASSWORD_DB);
        ) {
            int date = Integer.parseInt(data);
            String sql = """
                    select ads.photo_path, ht.name, ads.updated_at as "updated time",
                           ads.phone_number, st.name as "sale name", d.name as "district name",reg.name, par.room_count as "room count", par.area, par.floor,par.max_floor,m.name, ads.price "price",
                           cur.name "currency type",ads.info "description", ads.id, u.chat_id, ads.title from ads join users u
                         on ads.user_id = u.id join sale_type st on ads.sale_type_id = st.id join district d on ads.district_id = d.id join region reg on reg.id = d.region_id
                         join parametr par on  ads.parameter_id = par.id join currency cur on ads.price_type_id = cur.id join home_type ht on
                            ads.home_type_id=ht.id join ads_status stat on ads.status_id = stat.id join material m on par.material_id= m.id where ads.status_id=3 AND (ads.updated_at between  (NOW()::date - ?) AND now());
                    """;

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, date);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String photoPath = resultSet.getString(1);
                String str = "???? Bino turi:" + resultSet.getString(2) + "\n" +
                        "???? Telefon: " + resultSet.getString(4) + "\n" +
                        "???? E'lon turi: " + resultSet.getString(5) + "\n" +
                        " \uD83C\uDFEC Viloyat: " + resultSet.getString(7) + "\n" +
                        "???? Rayon: " + resultSet.getString(6) + "\n" +
                        "???? Maydon: " + resultSet.getInt(9) + "\n" +
                        "\uD83D\uDD22 Xonalar soni: " + resultSet.getInt(8) + "\n";
                if (resultSet.getString(4).equals("Kvartira")) {
                    str += "???? Qavat: " + resultSet.getInt(10) + "/" + resultSet.getInt(11) + "\n";
                } else {
                    str += "???? Qavat: " + resultSet.getInt(11) + "\n";
                }
                str += "??????? Material: " + resultSet.getString(12) + "\n" +
                        "???? Narxi: " + resultSet.getInt(13) + " ";
                if (resultSet.getString(14).equals("dollar")) {
                    str += "$";
                }
                str += "\n\n" +
                        "\uD83D\uDCAC Qo'shimcha ma'lumot: " + resultSet.getString(15) + "\n"
                        + "\uD83D\uDCC5 Sana: " + resultSet.getString(3) + "\n";
                SendPhoto sendPhoto = new SendPhoto();
                sendPhoto.setChatId(chatId);
                sendPhoto.setPhoto(new InputFile(photoPath));
                sendPhoto.setCaption("\n" + str);
                ComponentContainer.MY_BOT.sendMsg(sendPhoto);
                flag = true;
            }
            preparedStatement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (!flag) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText("So'rovingizga mos e'lonlar topilmadi qayta urinib kuring ");
            MY_BOT.sendMsg(sendMessage);
        }

    }

    @Override
    public boolean acceptAd(int adsId) {
        int i = 0;
        try (Connection connection = DriverManager.getConnection(URL_DB, USER_DB, PASSWORD_DB);) {
            Class.forName("org.postgresql.Driver");


            String statusChanger = " update ads set status_id = 3  where id = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(statusChanger);
            preparedStatement.setInt(1, adsId);
            i = preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return i > 0;
    }

    @Override
    public boolean rejectAd(int adsId) {
        int i = 0;
        try (Connection connection = DriverManager.getConnection(URL_DB, USER_DB, PASSWORD_DB);) {
            Class.forName("org.postgresql.Driver");


            String statusChanger = " update ads set status_id = 2  where id = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(statusChanger);
            preparedStatement.setInt(1, adsId);
            i = preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return i > 0;
    }

    @Override
    public int isAdsEmpty(String chatId) {
        int anInt = 0;
        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(URL_DB, USER_DB, PASSWORD_DB);
            String query = """
                    select count(chat_id) from ads join users u on ads.user_id = u.id  where ? like '%'  ||chat_id||  '%';""";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, chatId);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                anInt = resultSet.getInt(1);
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return anInt;
    }

    @Override
    public Users getUserByChatId(String chatId) {
        Users users = new Users();
        try (Connection connection = DriverManager.getConnection(URL_DB, USER_DB, PASSWORD_DB)) {
            Class.forName("org.postgresql.Driver");
            String query = """
                    select * from users where chat_id = ?;
                    """;

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, chatId);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                users.setId(resultSet.getInt("id"));
                users.setChatId(resultSet.getString("chat_id"));
                users.setFirstName(resultSet.getString("first_name"));
                users.setLastName(resultSet.getString("last_name"));
                users.setPhoneNumber(resultSet.getString("phone_number"));
                users.setUsername(resultSet.getString("username"));
                users.setActive(resultSet.getBoolean("is_active"));
                users.setBlocked(resultSet.getBoolean("is_blocked"));
                users.setAdmin(resultSet.getBoolean("is_admin"));
                System.out.println(users);
            }
            preparedStatement.close();
            return users;

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return users;
    }

    @Override
    public boolean addAdvertisement(Ads ads) {

        // bu metod advertisementni fildlarini tekshiradi
        // agar barchasi to'g'ri bo'lsa bu yangi advertisementni bazaga qo'shadi va boolean
        // qaytaradi

        try (Connection connection = DriverManager.getConnection(
                URL_DB, USER_DB, PASSWORD_DB);
        ) {

            String query = """
                    insert into ads(user_id,
                     ad_type, title, updated_at,
                    phone_number, sale_type_id,
                    district_id, parameter_id,
                     price, price_type_id, info,
                      home_type_id, status_id)
                    values (?,?,?,?,?,?,?,?,?,?,?,?,?);
                    """;

            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setInt(1, (int) ads.getUserId());
            preparedStatement.setString(2, ads.getAdType());
            preparedStatement.setString(3, ads.getTitle());
            preparedStatement.setTimestamp(4, ads.getUpdatedAt());
            preparedStatement.setString(5, ads.getPhoneNumber());
            preparedStatement.setInt(6, ads.getSaleTypeId());
            preparedStatement.setInt(7, ads.getDistrictId());
            preparedStatement.setInt(8, ads.getParameterId());
            preparedStatement.setBigDecimal(9, ads.getPrice());
            preparedStatement.setInt(10, ads.getPriceTypeId());
            preparedStatement.setString(11, ads.getInfo());
            preparedStatement.setInt(12, ads.getHomeTypeId());
            preparedStatement.setInt(13, ads.getStatusId());

            int result = preparedStatement.executeUpdate();
            System.out.println("result = " + result);
            if (result == 1) return true;

            preparedStatement.close();
        } catch (Exception e) {
            return false;
        }


        return false;
    }

    @Override
    public List<Users> getAllUsers() {
        List<Users> users = new ArrayList<>();
        // by metod bazadagi barcha userlar ro'yxatini oladi va uni userlar listi ko'rinishida qaytaradi


        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(URL_DB, USER_DB, PASSWORD_DB);
            Statement statement = connection.createStatement();
            String query = """
                    select * from users;
                    """;
            ResultSet resultSet = statement.executeQuery(query);
            int id;
            String chatId, firstname, lastName, username, phoneNumber;
            boolean isActive, isBlocked, isAdmin;
            while (resultSet.next()) {
                id = resultSet.getInt("id");
                chatId = resultSet.getString(2);
                firstname = resultSet.getString(3);
                lastName = resultSet.getString(4);
                phoneNumber = resultSet.getString(5);
                username = resultSet.getString(6);
                isActive = resultSet.getBoolean(7);
                isBlocked = resultSet.getBoolean(8);
                isAdmin = resultSet.getBoolean(9);

                users.add(new Users(id, chatId, firstname, lastName, phoneNumber,
                        username, isActive, isBlocked, isAdmin));
            }

            connection.close();
            statement.close();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return users;
    }

    @Override
    public List<String> getAdminsChatIds() {
        List<String> adminChatIDs = new ArrayList<>();
        // by metod bazadagi barcha userlar ro'yxatini oladi va uni userlar listi ko'rinishida qaytaradi


        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(URL_DB, USER_DB, PASSWORD_DB);
            Statement statement = connection.createStatement();
            String query = """
                    select chat_id from users where is_admin = '1';
                    """;
            ResultSet resultSet = statement.executeQuery(query);

            String chatId;
            while (resultSet.next()) {

                chatId = resultSet.getString(1);
                adminChatIDs.add(chatId);
            }
            connection.close();
            statement.close();

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return adminChatIDs;
    }

    @Override
    public boolean blockUser(String chatId) {
        try {
            Class.forName("org.postgresql.Driver");

            Connection connection = DriverManager.getConnection(URL_DB, USER_DB, PASSWORD_DB);

            String blockUser = " update users set is_blocked = ?" +
                    "where chat_id = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(blockUser);
            Users user = getUserByChatId(chatId);
            preparedStatement.setBoolean(1, !user.isBlocked());
            preparedStatement.setString(2, chatId);
            int i = preparedStatement.executeUpdate();
            System.out.println(chatId + " user blocked with this chat id");
            connection.close();
            preparedStatement.close();
            return true;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean setAdmin(String chatId) {



        try {
            Class.forName("org.postgresql.Driver");

            Connection connection = DriverManager.getConnection(URL_DB, USER_DB, PASSWORD_DB);

            String isBlocked = " update users set is_blocked = ?, is_admin = ?, is_active = ?" +
                    "where chat_id = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(isBlocked);
            preparedStatement.setBoolean(1, false);
            preparedStatement.setBoolean(2, true);
            preparedStatement.setBoolean(3, true);
            preparedStatement.setString(4, chatId);
            int i = preparedStatement.executeUpdate();
            System.out.println(i);

            connection.close();
            preparedStatement.close();

            return true;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public Users addUser(String chatId, Contact contact, String userName) {
        Users users = new Users();
        try (Connection connection = DriverManager.getConnection(URL_DB, USER_DB, PASSWORD_DB)) {
            Class.forName("org.postgresql.Driver");
            String query = """
                    insert into users
                    (chat_id,first_name,last_name,phone_number,username,is_active)
                    values (?,?,?,?,?,?);
                    """;

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, chatId);
            preparedStatement.setString(2, contact.getFirstName());
            preparedStatement.setString(3, contact.getLastName());
            preparedStatement.setString(4, contact.getPhoneNumber());
            preparedStatement.setString(5, userName);
            preparedStatement.setBoolean(6, true);

            int execute = preparedStatement.executeUpdate();

            connection.close();
            preparedStatement.close();

            System.out.println("execute = " + execute);
            System.out.println("Created!");
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return users;
    }

    @Override
    public void getMyAdsHistory(String chatId) {
        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(URL_DB, USER_DB, PASSWORD_DB);
            String query = """
                    select ads.photo_path, ht.name, ads.updated_at as "updated time",\s
                                ads.phone_number, st.name as "sale name", d.name as "district name",reg.name, par.room_count as "room count", par.area, par.floor,par.max_floor,m.name, ads.price "price",\s
                                cur.name "currency type",ads.info "description", stat.name, stat.id,ads.id from ads join users u
                                 on ads.user_id = u.id join sale_type st on ads.sale_type_id = st.id join district d on ads.district_id = d.id join region reg on reg.id = d.region_id\s
                                 join parametr par on  ads.parameter_id = par.id join currency cur on ads.price_type_id = cur.id join home_type ht on
                                  ads.home_type_id=ht.id join ads_status stat on ads.status_id = stat.id join material m on par.material_id= m.id where u.chat_id = ?;
                    """;

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, chatId);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String photoPath = resultSet.getString(1);
                String str = "???? Building type:" + resultSet.getString(2) + "\n" +
                        "???? Phone: " + resultSet.getString(4) + "\n" +
                        "???? Ad type: " + resultSet.getString(5) + "\n" +
                        "??????? Region: " + resultSet.getString(7) + "\n" +
                        "???? district: " + resultSet.getString(6) + "\n" +
                        "??????? Area: " + resultSet.getInt(9) + "\n" +
                        "\uD83D\uDD22 Number of rooms: " + resultSet.getInt(8) + "\n";
                if (resultSet.getString(4).equals("Apartment")) {
                    str += "???? Floor: " + resultSet.getInt(10) + "/" + resultSet.getInt(11) + "\n";
                } else {
                    str += "???? Floor: " + resultSet.getInt(11) + "\n";
                }
                str += "??????? Material: " + resultSet.getString(12) + "\n" +
                        "???? Cost: " + resultSet.getInt(13) + " ";
                if (resultSet.getString(14).equals("dollar")) {
                    str += "$";
                }
                str += "\n" +
                        "\uD83D\uDCAC  Additional information: " + resultSet.getString(15) + "\n"
                        + "\uD83D\uDCC5 Date: " + resultSet.getString(3) + "\n";
                str += "\n" +
                        "\uD83D\uDD0A Ad status: " + resultSet.getString(16);
                SendPhoto sendPhoto = new SendPhoto();
                sendPhoto.setChatId(chatId);
                sendPhoto.setPhoto(new InputFile(photoPath));
                sendPhoto.setCaption("\n" + str);
                int statusId=resultSet.getInt(17);
                if (statusId==1 || statusId==2){
                    int adsId=resultSet.getInt(18);
                    sendPhoto.setReplyMarkup(InlineKeyboardUtil.getEditMenu(adsId));
                }

                ComponentContainer.MY_BOT.sendMsg(sendPhoto);
                System.out.println(str);

                connection.close();
                preparedStatement.close();

            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void getAdsToCheck(String chatId) {
        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(URL_DB, USER_DB, PASSWORD_DB);
            String query = """
                    select ads.photo_path, ht.name, ads.updated_at as "updated time", 
                    ads.phone_number, st.name as "sale name", d.name as "district name",reg.name, par.room_count as "room count", par.area, par.floor,par.max_floor,m.name, ads.price "price", 
                    cur.name "currency type",ads.info "description", ads.id, u.chat_id, ads.title from ads join users u
                     on ads.user_id = u.id join sale_type st on ads.sale_type_id = st.id join district d on ads.district_id = d.id join region reg on reg.id = d.region_id 
                     join parametr par on  ads.parameter_id = par.id join currency cur on ads.price_type_id = cur.id join home_type ht on
                      ads.home_type_id=ht.id join ads_status stat on ads.status_id = stat.id join material m on par.material_id= m.id where ads.status_id = ?;""";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, 1);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String photoPath = resultSet.getString(1);
                String str = "Building type:" + resultSet.getString(2) + "\n" +
                        "-------------------------------------------------------"+
                        "Phone ????: " + resultSet.getString(4) + "\n" +
                        "-------------------------------------------------------"+

                        "Ad type: " + resultSet.getString(5) + "\n" +
                        "-------------------------------------------------------"+

                        "Region ???????: " + resultSet.getString(7) + "\n" +
                        "-------------------------------------------------------"+

                        "district ???????: " + resultSet.getString(6) + "\n" +
                        "-------------------------------------------------------"+

                        "Area ????: " + resultSet.getInt(9) + "\n" +
                        "-------------------------------------------------------"+

                        "Number of rooms: " + resultSet.getInt(8) + "\n";
                if (resultSet.getString(4).equals("Apartment")) {
                    str += "Floor: " + resultSet.getInt(10) + "/" + resultSet.getInt(11) + "\n"+
                            "-------------------------------------------------------";
                } else {
                    str += "Floor: " + resultSet.getInt(11) + "\n" +
                            "-------------------------------------------------------";
                }
                str += "Material ???????: " + resultSet.getString(12) + "\n" +
                        "-------------------------------------------------------"+
                        "Cost ????: " + resultSet.getInt(13) + " ";

                if (resultSet.getString(14).equals("dollar")) {
                    str += "$";
                }
                str += "\n" +
                        "Additional: " + resultSet.getString(15) + "\n"+
                        "-------------------------------------------------------"
                        + "Date ????: " + resultSet.getString(3) + "\n";
                int adsId = resultSet.getInt(16);
                String adsUserChatId = resultSet.getString(17);
                String adsTitle = resultSet.getString(18);

                SendPhoto sendPhoto = new SendPhoto();
                sendPhoto.setChatId(chatId);
                sendPhoto.setPhoto(new InputFile(photoPath));
                sendPhoto.setCaption("\n" + str);
                sendPhoto.setReplyMarkup(InlineKeyboardUtil.confirmAd(adsId, adsUserChatId, adsTitle));
                ComponentContainer.MY_BOT.sendMsg(sendPhoto);
                System.out.println(str);
            }

            connection.close();
            preparedStatement.close();

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    public static District getDistrictById(String districtId) {
        District district = new District();

        try (Connection connection = DriverManager.getConnection(URL_DB, USER_DB, PASSWORD_DB);) {
            Class.forName("org.postgresql.Driver");
            String query = "select * from district where id=" + districtId;

            Statement statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                district.setId(resultSet.getInt(1));
                district.setRegionId(resultSet.getInt(2));
                district.setName(resultSet.getString(3));
            }

            statement.close();

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        return district;
    }

    public static Region getRegionById(String regionId) {
        Region region = new Region();

        try (Connection connection = DriverManager.getConnection(URL_DB, USER_DB, PASSWORD_DB);) {
            Class.forName("org.postgresql.Driver");
            String query = "select * from region where id=" + regionId;

            Statement statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                region.setId(resultSet.getInt(1));
                region.setName(resultSet.getString(2));
            }
            statement.close();

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        return region;
    }

    public static int addNewParameter(Parameter parameter) {
        Users users = new Users();
        try (Connection connection = DriverManager.getConnection(URL_DB, USER_DB, PASSWORD_DB);) {
            Class.forName("org.postgresql.Driver");
            String query = """
                    insert into parametr
                    (room_count,area,living_area,floor,max_floor,material_id, status_id)
                    values (?,?,?,?,?,?,?);
                    """;

            BigDecimal bigDecimal = new BigDecimal(parameter.getArea());
//            BigDecimal livingArea=new BigDecimal(String.valueOf(parameter.getLivingArea()));
            parameter.setLivingArea(BigDecimal.valueOf(0));
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, parameter.getRoomCount());
            preparedStatement.setBigDecimal(2, bigDecimal);
            preparedStatement.setBigDecimal(3, parameter.getLivingArea());
            preparedStatement.setInt(4, parameter.getFloor());
            if (parameter.getMaxFloor() == null) {
                parameter.setMaxFloor(1);
            }
            preparedStatement.setInt(5, parameter.getMaxFloor());
            preparedStatement.setInt(6, parameter.getMaterialId());
            preparedStatement.setInt(7, parameter.getStatusId());

            int execute = preparedStatement.executeUpdate();
            System.out.println("execute = " + execute);
            System.out.println("Created!");

            connection.close();

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public static int getLastInsertParameterId() {

        int parameterId = 0;
        try (Connection connection = DriverManager.getConnection(URL_DB, USER_DB, PASSWORD_DB)) {

            Class.forName("org.postgresql.Driver");
            String query = """
                    select id from parametr order by id desc;
                    """;
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            resultSet.next();

            parameterId = resultSet.getInt(1);

            connection.close();
            statement.close();

        } catch (Exception e) {
            WorkWithDbFunctions object = new DbFunctionsImpl();
            SendMessage sendMessage = new SendMessage();
            for (String adminsChatId : object.getAdminsChatIds()) {
                sendMessage.setChatId(adminsChatId);
                sendMessage.setText(String.valueOf(e));
                MY_BOT.sendMsg(sendMessage);
            }
        }
        return parameterId;
    }

    public static boolean addNewAd(Ads ads, String chatId, Parameter parameter) {
        DbFunctionsImpl db = new DbFunctionsImpl();
        Users user = db.getUserByChatId(chatId);
        int userId = user.getId();

        int execute = 0;


        Users users = new Users();
        try (Connection connection = DriverManager.getConnection(URL_DB, USER_DB, PASSWORD_DB);) {
            int addNewParameter = addNewParameter(parameter);

            Class.forName("org.postgresql.Driver");


            String query = """
                    insert into ads
                    (user_id,ad_type,title,phone_number,sale_type_id,district_id, 
                    parameter_id, price, price_type_id, info, home_type_id, photo_path)
                    values (?,?,?,?,?,?,?,?,?,?,?,?);
                    """;


            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, String.valueOf(ads.getSaleTypeId()));
            preparedStatement.setString(3, ads.getTitle());
            preparedStatement.setString(4, ads.getPhoneNumber());
            preparedStatement.setInt(5, ads.getSaleTypeId());
            preparedStatement.setInt(6, ads.getDistrictId());
            int lastInsertParameterId = getLastInsertParameterId();
            preparedStatement.setInt(7, lastInsertParameterId);
            preparedStatement.setBigDecimal(8, ads.getPrice());
            preparedStatement.setInt(9, ads.getPriceTypeId());
            preparedStatement.setString(10, ads.getInfo());
            preparedStatement.setInt(11, ads.getHomeTypeId());
            String photo = ads.getPhotoPath();
            preparedStatement.setString(12, photo);

            execute = preparedStatement.executeUpdate();
            System.out.println("execute = " + execute);
            System.out.println("New ads added successfully!");

            connection.close();
            preparedStatement.close();

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return execute > 0;
    }

    public static HomeType getHomeTypeById(int homeId) {
        HomeType homeType = new HomeType();

        try (Connection connection = DriverManager.getConnection(URL_DB, USER_DB, PASSWORD_DB);) {
            Class.forName("org.postgresql.Driver");
            String query = "select * from home_type where id=" + homeId;

            Statement statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                homeType.setId(resultSet.getInt(1));
                homeType.setName(resultSet.getString(2));
            }
            connection.close();
            statement.close();

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        return homeType;
    }

    public static void getSearchResultsByNumberOfRooms(SearchHelper searchHelper, String chatId) {
        boolean flag = false;

        try (Connection connection = DriverManager.getConnection(URL_DB, USER_DB, PASSWORD_DB);
        ) {
            Class.forName("org.postgresql.Driver");
            String query = """
                    select ads.photo_path, ht.name, ads.updated_at as "updated time",
                           ads.phone_number, st.name as "sale name", d.name as "district name",reg.name, par.room_count as "room count", par.area, par.floor,par.max_floor,m.name, ads.price "price",
                           cur.name "currency type",ads.info "description" from ads join users u
                                                                                         on ads.user_id = u.id join sale_type st on ads.sale_type_id = st.id join district d on ads.district_id = d.id join region reg on reg.id = d.region_id
                                                                                    join parametr par on  ads.parameter_id = par.id join currency cur on ads.price_type_id = cur.id join home_type ht on
                            ads.home_type_id=ht.id join ads_status stat on ads.status_id = stat.id join material m on par.material_id= m.id where ads.status_id=3 AND ads.district_id=? AND par.room_count=? order by ads.updated_at ;""";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, searchHelper.getDistrictId());
            preparedStatement.setInt(2, searchHelper.getNumberOfRooms());  // todo
//            preparedStatement.setString(2, searchHelper.getAdParameterId());

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String photoPath = resultSet.getString(1);
                String str = "???? Building type:" + resultSet.getString(2) + "\n" +
                        "???? Phone: " + resultSet.getString(4) + "\n" +
                        "???? Ad type: " + resultSet.getString(5) + "\n" +
                        "??????? region: " + resultSet.getString(7) + "\n" +
                        "???? district: " + resultSet.getString(6) + "\n" +
                        "??????? area: " + resultSet.getInt(9) + "\n" +
                        "\uD83D\uDD22 Number of rooms: " + resultSet.getInt(8) + "\n";
                if (resultSet.getString(4).equals("Apartment")) {
                    str += "???? Floor: " + resultSet.getInt(10) + "/" + resultSet.getInt(11) + "\n";
                } else {
                    str += "???? Floor: " + resultSet.getInt(11) + "\n";
                }
                str += "???????  Material: " + resultSet.getString(12) + "\n" +
                        "????  Cost: " + resultSet.getInt(13) + " ";
                if (resultSet.getString(14).equals("dollar")) {
                    str += "$";
                }
                str += "\n" +
                        "\uD83D\uDCAC Additional information: " + resultSet.getString(15) + "\n"
                        + "???? Date: " + resultSet.getString(3) + "\n";
                SendPhoto sendPhoto = new SendPhoto();
                sendPhoto.setChatId(chatId);
                sendPhoto.setPhoto(new InputFile(photoPath));
                sendPhoto.setCaption("\n" + str);
                ComponentContainer.MY_BOT.sendMsg(sendPhoto);
                flag = true;
                System.out.println(str);
            }
            preparedStatement.close();

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (!flag) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText("No results found by your search");
            MY_BOT.sendMsg(sendMessage);
        }
    }

    public void getMyAdsSearch(Ads ads) {
        boolean flag = false;

        try (Connection connection = DriverManager.getConnection(URL_DB, USER_DB, PASSWORD_DB);
        ) {
            Class.forName("org.postgresql.Driver");
            String query = """
                    select ads.photo_path, ht.name, ads.updated_at as "updated time",
                           ads.phone_number, st.name as "sale name", d.name as "district name",reg.name, par.room_count as "room count", par.area, par.floor,par.max_floor,m.name, ads.price "price",
                           cur.name "currency type",ads.info "description" from ads join users u
                                                                                         on ads.user_id = u.id join sale_type st on ads.sale_type_id = st.id join district d on ads.district_id = d.id join region reg on reg.id = d.region_id
                                                                                    join parametr par on  ads.parameter_id = par.id join currency cur on ads.price_type_id = cur.id join home_type ht on
                            ads.home_type_id=ht.id join ads_status stat on ads.status_id = stat.id join material m on par.material_id= m.id where ads.sale_type_id=? AND ads.district_id=? AND ads.status_id=3 order by ads.updated_at ;""";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, ads.getSaleTypeId());
            preparedStatement.setInt(2, ads.getDistrictId());

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String photoPath = resultSet.getString(1);
                String str = "???? Building type:" + resultSet.getString(2) + "\n" +
                        "???? Phone: " + resultSet.getString(4) + "\n" +
                        "???? Ad type: " + resultSet.getString(5) + "\n" +
                        "\uD83C\uDFEB region: " + resultSet.getString(7) + "\n" +
                        "\uD83C\uDFD8 district: " + resultSet.getString(6) + "\n" +
                        "???? area: " + resultSet.getInt(9) + "\n" +
                        "\uD83D\uDD22 Number of rooms: " + resultSet.getInt(8) + "\n";
                if (resultSet.getString(4).equals("Apartment")) {
                    str += "???? Floor: " + resultSet.getInt(10) + "/" + resultSet.getInt(11) + "\n";
                } else {
                    str += "???? Floor: " + resultSet.getInt(11) + "\n";
                }
                str += "??????? Material: " + resultSet.getString(12) + "\n" +
                        "???? Cost: " + resultSet.getInt(13) + " ";
                if (resultSet.getString(14).equals("dollar")) {
                    str += "$";
                }
                str += "\n" +
                        "\uD83D\uDCAC Additional information: " + resultSet.getString(15) + "\n"
                        + "???? Date: " + resultSet.getString(3) + "\n";
                SendPhoto sendPhoto = new SendPhoto();
                sendPhoto.setChatId(ads.getUserId());
                sendPhoto.setPhoto(new InputFile(photoPath));
                sendPhoto.setCaption("\n" + str);
                ComponentContainer.MY_BOT.sendMsg(sendPhoto);
                flag = true;
                System.out.println(str);
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (!flag) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(ads.getUserId());
            sendMessage.setText("No results found by your search ");
            MY_BOT.sendMsg(sendMessage);
        }
    }


    public static void searchAllAds(String chatId) {
        boolean flag = false;

        try (Connection connection = DriverManager.getConnection(URL_DB, USER_DB, PASSWORD_DB);
        ) {
            Class.forName("org.postgresql.Driver");
            String query = """
                    select ads.photo_path, ht.name, ads.updated_at as "updated time",
                           ads.phone_number, st.name as "sale name", d.name as "district name",reg.name, par.room_count as "room count", par.area, par.floor,par.max_floor,m.name, ads.price "price",
                           cur.name "currency type",ads.info "description" from ads join users u
                                                                                         on ads.user_id = u.id join sale_type st on ads.sale_type_id = st.id join district d on ads.district_id = d.id join region reg on reg.id = d.region_id
                                                                                    join parametr par on  ads.parameter_id = par.id join currency cur on ads.price_type_id = cur.id join home_type ht on
                            ads.home_type_id=ht.id join ads_status stat on ads.status_id = stat.id join material m on par.material_id= m.id order by ads.updated_at ;""";

            PreparedStatement preparedStatement = connection.prepareStatement(query);


            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String photoPath = resultSet.getString(1);
                String str = "???? Building type:" + resultSet.getString(2) + "\n" +
                        "???? Phone: " + resultSet.getString(4) + "\n" +
                        "???? Ad type: " + resultSet.getString(5) + "\n" +
                        "???? region: " + resultSet.getString(7) + "\n" +
                        "\uD83C\uDFD8 district: " + resultSet.getString(6) + "\n" +
                        "???? area: " + resultSet.getInt(9) + "\n" +
                        "\uD83D\uDD22 Number of room: " + resultSet.getInt(8) + "\n";
                if (resultSet.getString(4).equals("Kvartira")) {
                    str += "???? Floor: " + resultSet.getInt(10) + "/" + resultSet.getInt(11) + "\n";
                } else {
                    str += "???? Floor: " + resultSet.getInt(11) + "\n";
                }
                str += "??????? Material: " + resultSet.getString(12) + "\n" +
                        "???? Cost: " + resultSet.getInt(13) + " ";
                if (resultSet.getString(14).equals("dollar")) {
                    str += "$";
                }
                str += "\n" +
                        "\uD83D\uDCAC  Additional information: " + resultSet.getString(15) + "\n"
                        + "\uD83D\uDCC5 Date: " + resultSet.getString(3) + "\n";
                SendPhoto sendPhoto = new SendPhoto();
                sendPhoto.setChatId(chatId);
                sendPhoto.setPhoto(new InputFile(photoPath));
                sendPhoto.setCaption("\n" + str);
                ComponentContainer.MY_BOT.sendMsg(sendPhoto);
                flag = true;
                System.out.println(str);
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (!flag) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText("No results found by your search ???????");
            MY_BOT.sendMsg(sendMessage);
        }
    }


    public static void editPhoneNumber(Ads ads, String newPhoneNumber) {
        try {
            Class.forName("org.postgresql.Driver");

            Connection connection = DriverManager.getConnection(URL_DB, USER_DB, PASSWORD_DB);

            String query = " update ads set phone_number = ?" +
                    "where ads.id = ?;";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, newPhoneNumber);
            preparedStatement.setInt(2, ads.getId());

            int execute = preparedStatement.executeUpdate();
            System.out.println("execute = " + execute);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void editDistrictId(Ads ads, Integer newDistrictId) {
        try {
            Class.forName("org.postgresql.Driver");

            Connection connection = DriverManager.getConnection(URL_DB, USER_DB, PASSWORD_DB);

            String query = " update ads  set district_id = ? where ads.id = ?;";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, newDistrictId);
            preparedStatement.setInt(2, ads.getId());

            int execute = preparedStatement.executeUpdate();
            System.out.println("execute = " + execute);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void editPrice(Ads ads, BigDecimal newPrice) {
        try {
            Class.forName("org.postgresql.Driver");

            Connection connection = DriverManager.getConnection(URL_DB, USER_DB, PASSWORD_DB);

            String query = " update ads set price = ?" +
                    "where ads.id = ?;";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setBigDecimal(1, newPrice);
            preparedStatement.setInt(2, ads.getId());

            int execute = preparedStatement.executeUpdate();
            System.out.println("execute = " + execute);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void editNumberOfRooms(Ads ads, Integer newNumberOfRooms) {
        try {
            Class.forName("org.postgresql.Driver");

            Connection connection = DriverManager.getConnection(URL_DB, USER_DB, PASSWORD_DB);

            String query = " update parametr p set p.room_count = ? from ads where p.id = ads.parameter_id and ads.id = ?;";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, newNumberOfRooms);
            preparedStatement.setInt(2, ads.getId());

            int execute = preparedStatement.executeUpdate();
            System.out.println("execute = " + execute);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void editPhotoUrl(Ads ads, String newPhotoUrl) {
        try {
            Class.forName("org.postgresql.Driver");

            Connection connection = DriverManager.getConnection(URL_DB, USER_DB, PASSWORD_DB);

            String query = " update ads set photo_path = ?" +
                    "where ads.id = ?;";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, newPhotoUrl);
            preparedStatement.setInt(2, ads.getId());

            int execute = preparedStatement.executeUpdate();
            System.out.println("execute = " + execute);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void editInfo(Ads ads, String newInfo) {
        try {
            Class.forName("org.postgresql.Driver");

            Connection connection = DriverManager.getConnection(URL_DB, USER_DB, PASSWORD_DB);
            String query = " update ads set info = ?" +
                    "where ads.id = ?;";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, newInfo);
            preparedStatement.setInt(2, ads.getId());

            int execute = preparedStatement.executeUpdate();
            System.out.println("execute = " + execute);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}


