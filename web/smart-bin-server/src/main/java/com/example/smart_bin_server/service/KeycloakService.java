package com.example.smart_bin_server.service;

import com.example.smart_bin_server.dto.LoginRequest;
import com.example.smart_bin_server.dto.RegisterRequest;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CertificateRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class KeycloakService {
    private final Keycloak keycloak;
    private final String realm;
    private final OkHttpClient client = new OkHttpClient();

    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    public KeycloakService(Keycloak keycloak, String keycloakRealm) {
        this.keycloak = keycloak;
        this.realm = keycloakRealm;
    }

    public String createUser(RegisterRequest request){
        UserRepresentation user = new UserRepresentation();
        user.setEnabled(false);
        user.setUsername(request.email());
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmailVerified(false);

        CredentialRepresentation  credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(request.password());
        credential.setTemporary(false);

        user.setCredentials(Collections.singletonList(credential));

        try{
            var response = keycloak.realm(realm).users().create(user);

            if(response.getStatus() == 201){
                String locationHeader = response.getHeaderString("Location");
                String userId = locationHeader.substring(locationHeader.lastIndexOf('/') + 1);
                return userId;
            }else{
                throw new RuntimeException("Failed to create user in Keycloak");
            }
        }catch (Exception e){
            throw new RuntimeException("Error creating user in Keycloak: " + e.getMessage());
        }
    }

    public void enableUser(String userId){
        try{
            UserRepresentation user = keycloak.realm(realm).users().get(userId).toRepresentation();

            user.setEnabled(true);
            user.setEmailVerified(true);
            keycloak.realm(realm).users().get(userId).update(user);
        }catch (Exception e){
            throw new RuntimeException("Error enabling user in Keycloak: " + e.getMessage());
        }
    }

    public Map<String, Object> login(LoginRequest request){
        try{
            RequestBody body = new FormBody.Builder()
                    .add("grant_type", "password")
                    .add("client_id", clientId)
                    .add("client_secret", clientSecret)
                    .add("username", request.email())
                    .add("password", request.password())
                    .build();

            Request req = new Request.Builder()
                    .url(serverUrl + "/realms/" + realm + "/protocol/openid-connect/token")
                    .post(body)
                    .build();

            try (Response response = client.newCall(req).execute()){
                String responseBody = response.body().string();

                if(!response.isSuccessful()){
                    throw new RuntimeException("Invalid credentials");
                }

                JsonObject json = new Gson().fromJson(responseBody, JsonObject.class);

                Map<String, Object> result = new HashMap<>();
                result.put("access_token", json.get("access_token").getAsString());
                result.put("refresh_token", json.get("refresh_token").getAsString());
                result.put("expires_in", json.get("expires_in").getAsLong());

                return result;
            }
        }catch (Exception e){
            throw new RuntimeException("Error during login: " + e.getMessage());
        }
    }

    public UserRepresentation getUserByEmail(String email){
        try{
            List<UserRepresentation> users = keycloak.realm(realm).users().search(email, true);

            if(users.isEmpty()) return null;

            return users.getFirst();

        }catch (Exception e){
            throw new RuntimeException("Error fetching user from Keycloak: " + e.getMessage());
        }
    }

    public UserRepresentation getUserById(String userId){
        try {
            return keycloak.realm(realm).users().get(userId).toRepresentation();
        } catch (Exception e) {
            throw new RuntimeException("Error fetching user from Keycloak: " + e.getMessage());
        }
    }

    public void deleteUser(String userId){
        try {
            keycloak.realm(realm).users().get(userId).remove();
        } catch (Exception e) {
            throw new RuntimeException("Error deleting user from Keycloak: " + e.getMessage());
        }
    }
}
