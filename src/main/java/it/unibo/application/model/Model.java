package it.unibo.application.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;

import it.unibo.application.dto.Target;

public class Model {

    private final Connection connection;

    public Model(final Connection connection) {
        this.connection = connection;
    }

    public boolean isValid(String username, char[] password) {
        final String CHECK_USER = "SELECT * FROM UTENTE WHERE Username = ? AND Password = ?";
        try (PreparedStatement s = connection.prepareStatement(CHECK_USER)) {
            s.setString(1, username);
            s.setString(2, String.valueOf(password));
            return s.executeQuery().next();
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    public boolean registerUser(String username, char[] password) {
        final String ADD_USER = "INSERT INTO UTENTE (Username, Password) VALUES (?, ?)";
        try (PreparedStatement s = connection.prepareStatement(ADD_USER)) {
            s.setString(1, username);
            s.setString(2, String.valueOf(password));
            return s.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println(e);
            return false;
        }
    }

    public Optional<Integer> leggiObbiettivo(String username) {
        final String LEGGI_OBBIETTIVO = "SELECT Obbiettivo FROM UTENTE WHERE Username = ?";
        try (PreparedStatement s = connection.prepareStatement(LEGGI_OBBIETTIVO)) {
            s.setString(1, username);
            ResultSet res = s.executeQuery();
            if (!res.next()) {
                throw new IllegalArgumentException("No such user: \"" + username + "\"");
            } else {
                int val = res.getInt(1);
                return Optional.ofNullable(val > 0 ? val : null);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Target> leggiTarget(String username) {
        final String LEGGI_TARGET = "SELECT Kcal, PercentualeProteine, PercentualeGrassi, PercentualeCarboidrati FROM UTENTE WHERE Username = ?";
        try (PreparedStatement s = connection.prepareStatement(LEGGI_TARGET)) {
            s.setString(1, username);
            ResultSet res = s.executeQuery();
            if (!res.next()) {
                throw new IllegalArgumentException("No such user: \"" + username + "\"");
            } else {
                int kcal = res.getInt(1);
                return Optional.ofNullable(kcal > 0 ? new Target(
                        kcal,
                        strictlyPositive(res.getInt(2)),
                        strictlyPositive(res.getInt(3)),
                        strictlyPositive(res.getInt(4)))
                        : null);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<Integer> strictlyPositive(int x) {
        return Optional.ofNullable(x > 0 ? x : null);
    }

    public boolean impostaTarget(String username, Optional<Target> t) {
        final String IMPOSTA_TARGET = "UPDATE UTENTE SET Kcal = ?, PercentualeProteine = ?, PercentualeGrassi = ?, PercentualeCarboidrati = ? WHERE Username = ?";
        try (PreparedStatement s = connection.prepareStatement(IMPOSTA_TARGET)){

            if (t.isEmpty()){
                s.setNull(1, Types.INTEGER);
                s.setNull(2, Types.TINYINT);
                s.setNull(3, Types.TINYINT);
                s.setNull(4, Types.TINYINT);
            }else{
                var target = t.get();
                s.setInt(1, target.kcal());
                if(target.percentualeProteine().isPresent()){
                    s.setInt(2, target.percentualeProteine().get());
                } else {
                    s.setNull(2, Types.TINYINT);
                }
                if(target.percentualeGrassi().isPresent()){
                    s.setInt(3, target.percentualeGrassi().get());
                } else {
                    s.setNull(3, Types.TINYINT);
                }
                if(target.percentualeCarboidrati().isPresent()){
                    s.setInt(4, target.percentualeCarboidrati().get());
                } else {
                    s.setNull(4, Types.TINYINT);
                }
            }
            s.setString(5, username);
            return s.executeUpdate() > 0;
        } catch(SQLException e){
            System.err.println(e);
            return false;
        }
    }

    public boolean impostaObbiettivo(String username, Optional<Integer> obbiettivo){
        final String IMPOSTA_TARGET = "UPDATE UTENTE SET Obbiettivo = ? WHERE Username = ?";
        try (PreparedStatement s = connection.prepareStatement(IMPOSTA_TARGET)){
            if(obbiettivo.isPresent()){
                s.setInt(1, obbiettivo.get());
            } else {
                s.setNull(1, Types.DECIMAL);
            }
            s.setString(2, username);
            return s.executeUpdate() > 0;
        } catch(SQLException e){
            System.err.println(e);
            return false;
        }
    }
}
