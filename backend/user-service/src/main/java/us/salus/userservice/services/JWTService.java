package us.salus.userservice.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import us.salus.userservice.models.User;

public class JWTService {

  private RSAPrivateKey prv;
  private RSAPublicKey pub;

  public JWTService() {
    File file = new File("keyfile");
    if (!file.exists()) {
      try {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");

        KeyPair kp = kpg.generateKeyPair();
        kpg.initialize(2048);

        Key pub = kp.getPublic();
        Key pvt = kp.getPrivate();

        String outfile = "keyfile";
        OutputStream out;
        out = new FileOutputStream(outfile);
        out.write(pvt.getEncoded());
        out.close();

        out = new FileOutputStream(outfile + ".pub");
        out.write(pub.getEncoded());
        out.close();
      } catch (Exception e) {
        System.out.println(e);
      }
    }

    FileInputStream in;

    try {
      in = new FileInputStream("keyfile");
      prv = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(in.readAllBytes()));
      in.close();

      in = new FileInputStream("keyfile.pub");
      pub = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(in.readAllBytes()));
      in.close();
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  public String createJWT(User user) {
    Algorithm algo = Algorithm.RSA256(pub, prv);
    String jwtToken = JWT.create()
        .withClaim("user_id", user.getId())
        .withIssuedAt(new Date())
        .sign(algo);

    return jwtToken;
  }

  public DecodedJWT verifyJWT(String jwt) throws JWTVerificationException {
    Algorithm algo = Algorithm.RSA256(pub, prv);
    JWTVerifier verifier = JWT.require(algo).build();

    return verifier.verify(jwt);
  }

}
