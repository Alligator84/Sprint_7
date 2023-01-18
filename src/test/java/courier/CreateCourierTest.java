package courier;

import com.github.javafaker.Faker;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

public class CreateCourierTest {

    public static final String COURIER_URI = "/api/v1/courier";
    public static final String BASE_URI = "https://qa-scooter.praktikum-services.ru/";

    private int courierId;
    private Courier courier;

    @Before
    public void setUp() {
        Faker faker = new Faker();
        courier = new Courier(faker.name().firstName(), "123456");
        RestAssured.baseURI = BASE_URI;
    }

    @Test
    @DisplayName("Успешное создание курьера")
    @Description("Тест проверяет:\n" +
            "1. Курьера можно создать;\n" +
            "2. Чтобы создать курьера, нужно передать в ручку все обязательные поля;\n" +
            "3. Запрос возвращает правильный код ответа;\n" +
            "4. Успешный запрос возвращает ok: true.")
    public void createCourierSuccessfully() {
        Response response = sendPostRequest(courier, COURIER_URI);
        compareResponseResults(response, 201, "ok", true);
        courierId = loginCourier(courier).path("id");
    }

    @Test
    @DisplayName("Нельзя создать двух одинаковых курьеров")
    @Description("Тест проверяет:\n" +
            "1. Нельзя создать двух одинаковых курьеров;\n" +
            "2. Если создать пользователя с логином, который уже есть, возвращается ошибка.")
    public void createForTwoIdenticalCouriersFailed() {
        Response response = sendPostRequest(courier, COURIER_URI);
        compareResponseResults(response, 201, "ok", true);

        response = sendPostRequest(courier, COURIER_URI);
        compareResponseResults(response, 409, "message", "Этот логин уже используется. Попробуйте другой.");

        courierId = loginCourier(courier).path("id");
    }

    @Test
    @DisplayName("Нельзя создать курьера без логина")
    @Description("Тест проверяет:\n" +
            "1. Нельзя создать курьера без логина.")
    public void createWithoutLoginFailed() {
        courier.setLogin(null);
        Response response = sendPostRequest(courier, COURIER_URI);
        compareResponseResults(response, 400, "message", "Недостаточно данных для создания учетной записи");
    }

    @Step("Отправка POST запроса на ручку " + COURIER_URI)
    public Response sendPostRequest(Object object, String uri) {
        return given()
                .contentType(ContentType.JSON)
                .and()
                .body(object)
                .when()
                .post(uri);
    }

    @Step("Отправка DELETE запроса на ручку " + COURIER_URI)
    public Response sendDeleteRequest(Object object, String uri) {
        return given()
                .contentType(ContentType.JSON)
                .and()
                .body(object)
                .when()
                .delete(uri);
    }

    @Step("Сравниваем результат ответа с передаваемыми значениями для проверки")
    public void compareResponseResults(Response response, int statusCode, String message, Object object) {
        response.then()
                .assertThat().statusCode(statusCode)
                .and()
                .assertThat().body(message, equalTo(object));
    }

    private Response loginCourier(Courier courier) {
        return sendPostRequest(courier, COURIER_URI + "/login");
    }

    @After
    public void deleteCourier() {
        if (courierId > 0) {
            courier.setId(courierId);
            Response response = sendDeleteRequest(courier, COURIER_URI + "/" + courierId);
            compareResponseResults(response, 200, "ok", true);
        }
    }
}