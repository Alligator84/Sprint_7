import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jdk.jfr.Description;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

import static java.net.HttpURLConnection.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

public class CreateCourierTest {

    public static final String COURIER_URI = "/api/v1/courier";

    private int courierId;
    private Courier courier;

    @Before
    public void setUp() {
        System.out.println("Before test");
        courier = new Courier("Oleg", "123456");
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
    }

    @Test
    @DisplayName("Check the creation of the courier.")
    @Description("Тест проверяет:\n" +
            "- курьера можно создать;\n" +
            "- чтобы создать курьера, нужно передать в ручку все обязательные поля;\n" +
            "- запрос возвращает правильный код ответа;\n" +
            "- успешный запрос возвращает ok: true.")
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
    @DisplayName("Check that you can't create two identical couriers.")
    @Description("Тест проверяет:\n" +
            "- нельзя создать двух одинаковых курьеров;\n" +
            "- если создать пользователя с логином, который уже есть, возвращается ошибка.")
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