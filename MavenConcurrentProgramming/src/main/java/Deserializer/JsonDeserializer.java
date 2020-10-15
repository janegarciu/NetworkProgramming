package Deserializer;

import HttpRequestor.HttpRequestor;
import Models.HomeResponse;
import Models.RegisterResponse;

public class JsonDeserializer {

    private final HttpRequestor httpRequestor = new HttpRequestor();

    public RegisterResponse parseRegisterResponse() {
        return httpRequestor.getRequest("/register").getBody().as(RegisterResponse.class);
    }

    public HomeResponse parseHomeResponse() {
        return httpRequestor.getRequest(parseRegisterResponse().getLink(), parseRegisterResponse().getAccess_token()).getBody().as(HomeResponse.class);
    }


}
