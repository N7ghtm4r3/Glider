package com.tecknobit.glider.helpers;

import com.tecknobit.apimanager.apis.APIRequest;
import com.tecknobit.apimanager.apis.encryption.aes.AESServerCipher;
import kotlin.Pair;
import kotlin.Triple;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import static com.tecknobit.apimanager.apis.APIRequest.SHA256_ALGORITHM;
import static com.tecknobit.apimanager.apis.encryption.BaseCipher.Algorithm.CTR_ALGORITHM;
import static com.tecknobit.apimanager.apis.encryption.aes.AESServerCipher.AESKeySize.k128;
import static com.tecknobit.equinoxbackend.environment.services.builtin.service.EquinoxItemsHelper.COMMA;
import static com.tecknobit.equinoxbackend.resourcesutils.ResourcesManager.RESOURCES_PATH;

public class ServerVault {

    public static final String VAULT_FOLDER = "vault";

    private static final String VAULT_PATHNAME = RESOURCES_PATH + VAULT_FOLDER + "/";

    private static final String INVALID_PATHNAME_CHARACTERS_REGEX = "[^a-zA-Z0-9._-]";

    private static final String INVALID_PATHNAME_CHARACTER_REPLACER = "a";

    private static final ServerVault vault = new ServerVault();

    private ServerVault() {
    }

    public void createUserPrivateKey(String token) throws Exception {
        String secretKey = AESServerCipher.createBase64SecretKey(k128);
        String ivSpec = AESServerCipher.createBase64IvParameterSpec();
        storePrivateKey(token, secretKey, ivSpec);
    }

    private void storePrivateKey(String token, String secretKey, String ivSpec) throws Exception {
        String lockBoxPathname = computeLockBoxPathName(token);
        try (FileWriter lockBoxWriter = new FileWriter(lockBoxPathname)) {
            lockBoxWriter.write(secretKey);
            lockBoxWriter.write(COMMA);
            lockBoxWriter.write(ivSpec);
        }
    }

    public Triple<String, String, String> encryptPasswordData(String token, String tail, String password,
                                                              String scopes) throws Exception {
        AESServerCipher cipher = getCipherInstance(token);
        if (scopes == null)
            scopes = " ";
        String encryptedTail = cipher.encryptBase64(tail);
        String encryptedPassword = cipher.encryptBase64(password);
        String encryptedScopes = cipher.encryptBase64(scopes);
        return new Triple<>(encryptedTail, encryptedPassword, encryptedScopes);
    }

    public Map<String, String> decryptPasswordAndScopes(String token, Map<String, String> passwordsScopes) throws Exception {
        AESServerCipher decipher = getCipherInstance(token);
        HashMap<String, String> decryptedPasswordsScopes = new HashMap<>();
        for (String password : passwordsScopes.keySet()) {
            String encryptedScopes = passwordsScopes.get(password);
            String decryptedPassword = decipher.decryptBase64(password);
            String decryptedScopes = decipher.decryptBase64(encryptedScopes);
            decryptedPasswordsScopes.put(decryptedPassword, decryptedScopes);
        }
        return decryptedPasswordsScopes;
    }

    public Pair<String, String> decryptPasswordAndScopes(String token, String password, String scopes) throws Exception {
        AESServerCipher decipher = getCipherInstance(token);
        String decryptedPassword = decipher.decryptBase64(password);
        String decryptedScopes = decipher.decryptBase64(scopes);
        return new Pair<>(decryptedPassword, decryptedScopes);
    }

    private AESServerCipher getCipherInstance(String token) throws Exception {
        Pair<String, String> keySlices = retrievePrivateKey(token);
        return new AESServerCipher(keySlices.getFirst(), keySlices.getSecond(), CTR_ALGORITHM);
    }

    private Pair<String, String> retrievePrivateKey(String token) throws Exception {
        String lockBoxPathname = computeLockBoxPathName(token);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(lockBoxPathname));
        String[] rawData = bufferedReader.readLine().split(COMMA);
        bufferedReader.close();
        return new Pair<>(rawData[0], rawData[1]);
    }

    public boolean deleteLockBox(String token) {
        String lockBoxPathname;
        try {
            lockBoxPathname = computeLockBoxPathName(token);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return new File(lockBoxPathname).delete();
    }

    private String computeLockBoxPathName(String token) throws NoSuchAlgorithmException {
        return VAULT_PATHNAME + APIRequest.base64Digest(token, SHA256_ALGORITHM)
                .replaceAll(INVALID_PATHNAME_CHARACTERS_REGEX, INVALID_PATHNAME_CHARACTER_REPLACER);
    }

    public static ServerVault getInstance() {
        return vault;
    }

}
