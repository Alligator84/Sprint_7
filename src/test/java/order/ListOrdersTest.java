package order;

import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;

public class ListOrdersTest {

    public static final String ORDERS_URL = "/api/v1/orders";
    public static final String BASE_URI = "https://qa-scooter.praktikum-services.ru/";

    @Before
    public void setUp() {
        RestAssured.baseURI = BASE_URI;
    }

    @Test
    @DisplayName("Успешное получение списка заказов")
    @Description("Когда запрашиваешь список заказов:\n" +
            "1. В тело ответа возвращается список заказов.")
    public void orderCreationSuccessfully() {
        Response response = sendGetRequest(new Order(), ORDERS_URL);
        compareResponseResultsNotNull(response, 200, "orders");
    }

    @Step("Отправка GET запроса на ручку " + ORDERS_URL)
    public Response sendGetRequest(Object object, String uri) {
        return given()
                .contentType(ContentType.JSON)
                .body(object)
                .when()
                .get(uri);
    }

    @Step("Сравниваем результат ответа с передаваемыми значениями для проверки, в том числе, notNullValue")
    public void compareResponseResultsNotNull(Response response, int statusCode, String message) {
        response.then()
                .assertThat().statusCode(statusCode)
                .assertThat().body(message, notNullValue());
    }
}