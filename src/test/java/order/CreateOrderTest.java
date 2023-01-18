package order;

import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;

@RunWith(Parameterized.class)
public class CreateOrderTest {

    public static final String ORDERS_URL = "/api/v1/orders";
    public static final String BASE_URI = "https://qa-scooter.praktikum-services.ru/";
    private final String firstName;
    private final String lastName;
    private final String address;
    private final String metroStation;
    private final String phone;
    private final int rentTime;
    private final String deliveryDate;
    private final String comment;
    private final List<String> color;

    public CreateOrderTest(String firstName, String lastName, String address, String metroStation,
                           String phone, int rentTime, String deliveryDate, String comment, List<String> color) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.metroStation = metroStation;
        this.phone = phone;
        this.rentTime = rentTime;
        this.deliveryDate = deliveryDate;
        this.comment = comment;
        this.color = color;
    }

    @Before
    public void setUp() {
        RestAssured.baseURI = BASE_URI;
    }

    @Parameterized.Parameters
    public static Object[][] colorOfScooter() {
        return new Object[][]{
                {"Олег", "Петров", "Шереметное, 6", "Савеловская", "+79995695566", 2, "2023-02-02", "чистый", List.of("GREY")},
                {"Иван", "Шеремет", "Савы, 7", "Москвовская", "+79995695577", 1, "2023-02-04", "коммент", List.of("BLACK")},
                {"Жорж", "Лептен", "Петарова, 8", "Савеловская", "+79995695588", 6, "2023-02-03", "лучшие", List.of("GREY", "BLACK")},
                {"Мирно", "Дидо", "Краснова, 6", "Петровско-Разумовская", "+79995695533", 3, "2023-02-01", "всем доброго вечера", List.of("")},
        };
    }

    @Test
    @DisplayName("Успешное создание заказа")
    @Description("Когда создаёшь заказ:\n" +
            "1. Можно указать один из цветов — BLACK или GREY;\n" +
            "2. Можно указать оба цвета;\n" +
            "3. Можно совсем не указывать цвет;\n" +
            "4. Тело ответа содержит track.")
    public void orderCreateSuccessfully() {
        Order order = new Order(firstName, lastName, address, metroStation, phone, rentTime, deliveryDate, comment, color);
        Response response = sendPostRequest(order, ORDERS_URL);
        compareResponseResultsNotNull(response, 201, "track");
    }

    @Step("Отправка POST запроса на ручку " + ORDERS_URL)
    public Response sendPostRequest(Object object, String uri) {
        return given()
                .contentType(ContentType.JSON)
                .and()
                .body(object)
                .when()
                .post(uri);
    }

    @Step("Сравниваем результат ответа с передаваемыми значениями для проверки, в том числе, notNullValue")
    public void compareResponseResultsNotNull(Response response, int statusCode, String message) {
        response.then()
                .assertThat().statusCode(statusCode)
                .and()
                .assertThat().body(message, notNullValue());
    }
}
