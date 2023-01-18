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
import static org.hamcrest.CoreMatchers.notNullValue;

public class LoginCourierTest {

    public static final String COURIER_URI = "/api/v1/courier";
    public static final String BASE_URI = "https://qa-scooter.praktikum-services.ru/";

    private int courierId;
    private Courier courier;

    @Before
    public void setUp() {
        Faker faker = new Faker();
        RestAssured.baseURI = BASE_URI;
        courier = new Courier(faker.name().firstName(), "123456");
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
        Response response = sendPostRequest(courier, COURIER_URI + "/login");
        compareResponseResultsNotNull(response, 200, "id");
    }

    @Test
    @DisplayName("Ошибка при авторизации с неправильными данными")
    @Description("Тест проверяет:\n" +
            "1. Система вернёт ошибку, если неправильно указать логин;\n" +
            "2. Если авторизоваться под несуществующим пользователем, запрос возвращает ошибку.")
    public void loginCourierWithIncorrectValueFailed() {
        courier.setLogin("Incorrect");
        Response response = sendPostRequest(courier, COURIER_URI + "/login");
        compareResponseResults(response, 404, "message", "Учетная запись не найдена");
    }

    @Test
    @DisplayName("Ошибка при указании неверного пароля")
    @Description("Тест проверяет:\n" +
            "1. Система вернёт ошибку, если неправильно указать пароль.")
    public void loginCourierWithIncorrectPasswordFailed() {
        courier.setPassword("00000");
        Response response = sendPostRequest(courier, COURIER_URI + "/login");
        compareResponseResults(response, 404, "message", "Учетная запись не найдена");
     }

    @Test
    @DisplayName("Ошибка если не передать обязательное поле")
    @Description("Тест проверяет:\n" +
            "1. Если какого-то поля нет, запрос возвращает ошибку.")
    public void loginCourierWithoutPasswordFailed() {
        courier.setPassword("");
        Response response = sendPostRequest(courier, COURIER_URI + "/login");
        compareResponseResults(response, 400, "message", "Недостаточно данных для входа");
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

    @Step("Сравниваем результат ответа с передаваемыми значениями для проверки, в том числе, notNullValue")
    public void compareResponseResultsNotNull(Response response, int statusCode, String message) {
        response.then()
                .assertThat().statusCode(statusCode)
                .and()
                .assertThat().body(message, notNullValue());
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