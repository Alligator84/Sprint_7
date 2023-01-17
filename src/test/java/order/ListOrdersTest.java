package order;

import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.hamcrest.CoreMatchers.notNullValue;

public class ListOrdersTest {

    public static final String ORDERS_URL = "/api/v1/orders";

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru/";
    }

    @Test
    @DisplayName("Успешное получение списка заказов")
    @Description("Когда запрашиваешь список заказов:\n" +
            "1. В тело ответа возвращается список заказов.")
    public void orderCreationSuccessfully() {
        given()
                .contentType(ContentType.JSON)
                .body(new Order())
                .when()
                .get(ORDERS_URL)
                .then()
                .assertThat().body("orders", notNullValue())
                .assertThat().statusCode(200)
                .and()
                .statusCode(HTTP_OK);
    }
}