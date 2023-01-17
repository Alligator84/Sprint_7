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
import static org.hamcrest.CoreMatchers.notNullValue;

public class LoginCourierTest {

    public static final String COURIER_URI = "/api/v1/courier";

    private int courierId;
    private Courier courier;

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru/";
        courier = new Courier("Oleg", "123456");
        createCourier(courier);
    }

    @Test
    @DisplayName("Успешный вход курьера в систему")
    @Description("Тест проверяет:\n" +
            "1. Курьер может авторизоваться;\n" +
            "2. Для авторизации нужно передать все обязательные поля;\n" +
            "3. Успешный запрос возвращает id.")
    public void loginCourierSuccessfully() {
        courier.setFirstName(null);
        given()
                .contentType(ContentType.JSON)
                .and()
                .body(courier)
                .when()
                .post(COURIER_URI + "/login")
                .then()
                .assertThat().statusCode(200)
                .assertThat().body("id", notNullValue())
                .statusCode(HTTP_OK);
    }

    @Test
    @DisplayName("Ошибка при авторизации с неправильными данными")
    @Description("Тест проверяет:\n" +
            "1. Система вернёт ошибку, если неправильно указать логин;\n" +
            "2. Если авторизоваться под несуществующим пользователем, запрос возвращает ошибку.")
    public void loginCourierWithIncorrectValueFailed() {
        courier.setLogin("Incorrect");
        given()
                .contentType(ContentType.JSON)
                .and()
                .body(courier)
                .when()
                .post(COURIER_URI + "/login")
                .then()
                .assertThat().statusCode(404)
                .assertThat().body("message", equalTo("Учетная запись не найдена"))
                .and()
                .statusCode(HTTP_NOT_FOUND);
    }

    @Test
    @DisplayName("Ошибка при указании неверного пароля")
    @Description("Тест проверяет:\n" +
            "1. Система вернёт ошибку, если неправильно указать пароль.")
    public void loginCourierWithIncorrectPasswordFailed() {
        courier.setPassword("00000");
        given()
                .contentType(ContentType.JSON)
                .and()
                .body(courier)
                .when()
                .post(COURIER_URI + "/login")
                .then()
                .assertThat().statusCode(404)
                .assertThat().body("message", equalTo("Учетная запись не найдена"))
                .and()
                .statusCode(HTTP_NOT_FOUND);
    }

    @Test
    @DisplayName("Ошибка если не передать обязательное поле")
    @Description("Тест проверяет:\n" +
            "1. Если какого-то поля нет, запрос возвращает ошибку.")
    public void loginCourierWithoutPasswordFailed() {
        courier.setPassword("");
        given()
                .contentType(ContentType.JSON)
                .and()
                .body(courier)
                .when()
                .post(COURIER_URI + "/login")
                .then()
                .assertThat().statusCode(400)
                .assertThat().body("message", equalTo("Недостаточно данных для входа"))
                .and()
                .statusCode(HTTP_BAD_REQUEST);
    }


    private void createCourier(Courier courier) {
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