package it.unibo.application.model;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.GroupLayout.Alignment;

import it.unibo.application.commons.Utilities;
import it.unibo.application.dto.Alimento;
import it.unibo.application.dto.Misurazione;
import it.unibo.application.dto.Tag;
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
                        Utilities.strictlyPositive(res.getInt(2)),
                        Utilities.strictlyPositive(res.getInt(3)),
                        Utilities.strictlyPositive(res.getInt(4)))
                        : null);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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

    public List<Misurazione> leggiMisurazioni(String username) {
        final String LEGGI_MISURAZIONI = "SELECT Data, Peso FROM MISURAZIONE WHERE Username = ? ORDER BY Data DESC";
        try (PreparedStatement s = connection.prepareStatement(LEGGI_MISURAZIONI)) {
            s.setString(1, username);
            ResultSet res = s.executeQuery();
            List<Misurazione> ret = new ArrayList<>();
            while (res.next()) {
                ret.add(new Misurazione(username, res.getDate(1).toLocalDate(), res.getBigDecimal(2)));
            }
            return ret;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean aggiungiMisurazione(Misurazione m) {
        final String AGGIUNGI_MISURAZIONE = "INSERT INTO MISURAZIONE (Data, Peso, Username) VALUES (?,?,?)";
        try(PreparedStatement s = connection.prepareStatement(AGGIUNGI_MISURAZIONE)){
            s.setDate(1, Date.valueOf(m.data()));
            s.setBigDecimal(2, m.peso());
            s.setString(3, m.username());
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println(e);
            return false;
        }
    }

    public boolean modificaMisurazione(Misurazione m, BigDecimal nuovoPeso) {
        final String MODIFICA_MISURAZIONE = "UPDATE MISURAZIONE SET Peso = ? WHERE Data = ? AND Username = ?";
        try (PreparedStatement s = connection.prepareStatement(MODIFICA_MISURAZIONE)){
            s.setBigDecimal(1, nuovoPeso);
            s.setDate(2, Date.valueOf(m.data()));
            s.setString(3, m.username());
            return s.executeUpdate() > 0;
        } catch(SQLException e){
            System.err.println(e);
            return false;
        }
    }

    public boolean eliminaMisurazione(Misurazione m){
        final String ELIMINA_MISURAZIONE = "DELETE FROM MISURAZIONE WHERE Data = ? AND Username = ?";
        try (PreparedStatement s = connection.prepareStatement(ELIMINA_MISURAZIONE)){
            s.setDate(1, Date.valueOf(m.data()));
            s.setString(2, m.username());
            return s.executeUpdate() > 0;
        } catch(SQLException e){
            System.err.println(e);
            return false;
        }
    }

    public List<Tag> leggiTag(){
        final String LEGGI_TAG = "SELECT ParolaChiave, Creatore FROM TAG ORDER BY ParolaChiave";
        try (PreparedStatement s = connection.prepareStatement(LEGGI_TAG)) {
            ResultSet res = s.executeQuery();
            List<Tag> ret = new ArrayList<>();
            while (res.next()) {
                ret.add(new Tag(res.getString(1), res.getString(2)));
            }
            return ret;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Tag> leggiTag(String username){
        final String LEGGI_TAG = "SELECT ParolaChiave, Creatore FROM TAG WHERE Creatore = ? ORDER BY ParolaChiave";
        try (PreparedStatement s = connection.prepareStatement(LEGGI_TAG)) {
            s.setString(1, username);
            ResultSet res = s.executeQuery();
            List<Tag> ret = new ArrayList<>();
            while (res.next()) {
                ret.add(new Tag(res.getString(1), res.getString(2)));
            }
            return ret;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean aggiungiTag(Tag tag){
        final String AGGIUNGI_TAG = "INSERT INTO TAG (ParolaChiave, Creatore) VALUES (?,?)";
        try(PreparedStatement s = connection.prepareStatement(AGGIUNGI_TAG)){
            s.setString(1, tag.parolaChiave());
            s.setString(2, tag.creatore());
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println(e);
            return false;
        }
    }

    public boolean eliminaTag(Tag tag){
        final String ELIMINA_TAG = "DELETE FROM TAG WHERE ParolaChiave = ? AND Creatore = ?";
        try (PreparedStatement s = connection.prepareStatement(ELIMINA_TAG)){
            s.setString(1, tag.parolaChiave());
            s.setString(2, tag.creatore());
            return s.executeUpdate() > 0;
        } catch(SQLException e){
            System.err.println(e);
            return false;
        }
    }

    public boolean aggiungiCibo(Alimento cibo){
        return false;
    }
}
