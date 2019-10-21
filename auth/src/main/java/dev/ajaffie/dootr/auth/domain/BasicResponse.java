package dev.ajaffie.dootr.auth.domain;

import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

public class BasicResponse {

    public final String status;

    public final String error;

    private BasicResponse(boolean isOk, String e) {
        if (isOk) {
            this.status = "OK";
            this.error = null;
        } else {
            this.status = "error";
            this.error = e;
        }
    }

    public static Response error(String msg) {
        return Response.status(500).entity(new BasicResponse(false, msg)).build();
    }

    public static Response ok() {
        return Response.ok(new BasicResponse(true, null)).build();
    }

    public static Response okWithAuth(String token) {
        return Response.ok(new BasicResponse(true, null))
                .cookie(new NewCookie("session", token))
                .build();
    }


}
