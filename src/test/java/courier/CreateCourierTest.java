package courier;

import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static java.net.HttpURLConnection.*;
import static org.hamcrest.CoreMatchers.equalTo;

public class CreateCourierTest {

    public static final String COURIER_URI = "/api/v1/courier";

    private int courierId;
    private Courier courier;

    @Before
    public void setUp() {
        courier = new Courier("Oleg", "123456");
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru/";
    }

    @Test
    @DisplayName("Успешное создание курьера")
    @Description("Тест проверяет:\n" +
            "1. Курьера можно создать;\n" +
            "2. Чтобы создать курьера, нужно передать в ручку все обязательные поля;\n" +
            "3. Запрос возвращает правильный код ответа;\n" +
            "4. Успешный запрос возвращает ok: true.")
    public void createCourierSuccessfully() {
        given()
                .contentType(ContentType.JSON)
                .and()
                .body(courier)
                .when()
                .post(COURIER_URI)
                .then()
                .assertThat().statusCode(201)
                .assertThat().body("ok", equalTo(true));

        courierId = loginCourier(courier).path("id");
    }

    @Test
    @DisplayName("Нельзя создать двух одинаковых курьеров")
    @Description("Тест проверяет:\n" +
            "1. Нельзя создать двух одинаковых курьеров;\n" +
            "2. Если создать пользователя с логином, который уже есть, возвращается ошибка.")
    public void createForTwoIdenticalCouriersFailed() {
        given()
                .contentType(ContentType.JSON)
                .and()
                .body(courier)
                .when()
                .post(COURIER_URI)
                .then()
                .assertThat().statusCode(201)
                .assertThat().body("ok", equalTo(true));

        given()
                .contentType(ContentType.JSON)
                .and()
                .body(courier)
                .when()
                .post(COURIER_URI)
                .then()
                .assertThat().statusCode(409)
                .assertThat().body("message", equalTo("Этот логин уже используется. Попробуйте другой."))
                .and()
                .statusCode(HTTP_CONFLICT);

        courierId = loginCourier(courier).path("id");
    }

    @Test
    @DisplayName("Нельзя создать курьера без логина")
    @Description("Тест проверяет:\n" +
            "1. Нельзя создать курьера без логина.")
    public void createWithoutLoginFailed() {
        courier.setLogin(null);
        given()
                .contentType(ContentType.JSON)
                .and()
                .body(courier)
                .when()
                .post(COURIER_URI)
                .then()
                .assertThat().statusCode(400)
                .assertThat().body("message", equalTo("Недостаточно данных для создания учетной записи"))
                .and()
                .statusCode(HTTP_BAD_REQUEST);
    }

    private Response loginCourier(Courier courier) {
        return given()
                .contentType(ContentType.JSON)
                .and()
                .body(courier)
                .when()
                .post(COURIER_URI + "/login");
    }

    @After
    public void deleteCourier() {
        if (courierId > 0) {
            String json = String.format("{\"id\": \"%d\"}", courierId);
            given()
                    .contentType(ContentType.JSON)
                    .and()
                    .body(json)
                    .when()
                    .delete(COURIER_URI + "/" + courierId)
                    .then()
                    .assertThat().body("ok", equalTo(true))
                    .and()
                    .statusCode(HTTP_OK);
        }
    }
}